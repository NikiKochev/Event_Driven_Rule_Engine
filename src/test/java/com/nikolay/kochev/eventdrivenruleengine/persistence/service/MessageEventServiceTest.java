package com.nikolay.kochev.eventdrivenruleengine.persistence.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikolay.kochev.eventdrivenruleengine.persistence.entity.MessageEvent;
import com.nikolay.kochev.eventdrivenruleengine.persistence.enums.MessageEventStatus;
import com.nikolay.kochev.eventdrivenruleengine.persistence.repository.MessageEventRepository;
import com.nikolay.kochev.eventdrivenruleengine.service.model.EventDrivenMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageEventService Tests")
class MessageEventServiceTest {

    @Mock
    private MessageEventRepository repository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MessageEventService messageEventService;

    private EventDrivenMessage eventDrivenMessage;
    private MessageEvent messageEvent;
    private UUID testEntityId;

    @BeforeEach
    void setUp() throws Exception {
        testEntityId = UUID.randomUUID();

        Map<String, String> metadata = new HashMap<>();
        metadata.put("businessRuleType", "ORDER_PROCESSING");
        metadata.put("sourceTopic", "input-topic");

        eventDrivenMessage = EventDrivenMessage.builder()
                .messageId("MSG-12345")
                .payload(objectMapper.readTree("{\"orderId\": 123}"))
                .metadata(metadata)
                .build();

        // Setup test MessageEvent
        messageEvent = MessageEvent.builder()
                .id(testEntityId)
                .messageId("MSG-12345")
                .messageType("ORDER_PROCESSING")
                .status(MessageEventStatus.RECEIVED)
                .build();
    }

    @Nested
    @DisplayName("createInitialRecord() Tests")
    class CreateInitialRecordTests {

        @Test
        @DisplayName("Should create initial record with correct fields")
        void shouldCreateInitialRecordWithCorrectFields() {
            // Arrange
            when(repository.save(any(MessageEvent.class))).thenReturn(messageEvent);

            // Act
            messageEventService.createInitialRecord(eventDrivenMessage, "ORDER_PROCESSING");

            // Assert
            ArgumentCaptor<MessageEvent> captor = ArgumentCaptor.forClass(MessageEvent.class);
            verify(repository).save(captor.capture());

            MessageEvent savedRecord = captor.getValue();
            assertThat(savedRecord.getMessageId()).isEqualTo("MSG-12345");
            assertThat(savedRecord.getMessageType()).isEqualTo("ORDER_PROCESSING");
            assertThat(savedRecord.getStatus()).isEqualTo(MessageEventStatus.RECEIVED);
        }

        @Test
        @DisplayName("Should return saved entity")
        void shouldReturnSavedEntity() {
            // Arrange
            when(repository.save(any(MessageEvent.class))).thenReturn(messageEvent);

            // Act
            MessageEvent result = messageEventService.createInitialRecord(eventDrivenMessage, "ORDER_PROCESSING");

            // Assert
            assertThat(result)
                    .isNotNull()
                    .isEqualTo(messageEvent);
        }

        @Test
        @DisplayName("Should call repository save once")
        void shouldCallRepositorySaveOnce() {
            // Arrange
            when(repository.save(any(MessageEvent.class))).thenReturn(messageEvent);

            // Act
            messageEventService.createInitialRecord(eventDrivenMessage, "ORDER_PROCESSING");

            // Assert
            verify(repository, times(1)).save(any(MessageEvent.class));
        }

        @Test
        @DisplayName("Should create record with different message types")
        void shouldCreateRecordWithDifferentMessageTypes() {
            // Arrange
            when(repository.save(any(MessageEvent.class))).thenReturn(messageEvent);

            // Act
            messageEventService.createInitialRecord(eventDrivenMessage, "PAYMENT_PROCESSING");

            // Assert
            ArgumentCaptor<MessageEvent> captor = ArgumentCaptor.forClass(MessageEvent.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getMessageType()).isEqualTo("PAYMENT_PROCESSING");
        }
    }

    @Nested
    @DisplayName("createRejectRecord() Tests")
    class CreateRejectRecordTests {

        @Test
        @DisplayName("Should create reject record with error message")
        void shouldCreateRejectRecordWithErrorMessage() {
            // Arrange
            when(repository.save(any(MessageEvent.class))).thenReturn(messageEvent);

            // Act
            messageEventService.createRejectRecord(eventDrivenMessage);

            // Assert
            ArgumentCaptor<MessageEvent> captor = ArgumentCaptor.forClass(MessageEvent.class);
            verify(repository).save(captor.capture());

            MessageEvent savedRecord = captor.getValue();
            assertThat(savedRecord.getMessageId()).isEqualTo("MSG-12345");
            assertThat(savedRecord.getStatus()).isEqualTo(MessageEventStatus.RECEIVED);
            assertThat(savedRecord.getErrorMessage()).isEqualTo("Message rejected due to invalid format or missing required fields");
        }

        @Test
        @DisplayName("Should create record without message type")
        void shouldCreateRecordWithoutMessageType() {
            // Arrange
            when(repository.save(any(MessageEvent.class))).thenReturn(messageEvent);

            // Act
            messageEventService.createRejectRecord(eventDrivenMessage);

            // Assert
            ArgumentCaptor<MessageEvent> captor = ArgumentCaptor.forClass(MessageEvent.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getMessageType()).isNull();
        }

        @Test
        @DisplayName("Should return saved entity")
        void shouldReturnSavedEntity() {
            // Arrange
            when(repository.save(any(MessageEvent.class))).thenReturn(messageEvent);

            // Act
            MessageEvent result = messageEventService.createRejectRecord(eventDrivenMessage);

            // Assert
            assertThat(result)
                    .isNotNull()
                    .isEqualTo(messageEvent);
        }
    }

    @Nested
    @DisplayName("updateToCompleted() Tests")
    class UpdateToCompletedTests {

        @Test
        @DisplayName("Should update status to COMPLETED when entity exists")
        void shouldUpdateStatusToCompletedWhenEntityExists() {
            // Arrange
            when(repository.findById(testEntityId)).thenReturn(Optional.of(messageEvent));
            when(repository.save(any(MessageEvent.class))).thenReturn(messageEvent);

            // Act
            messageEventService.updateToCompleted(testEntityId);

            // Assert
            verify(repository).findById(testEntityId);
            ArgumentCaptor<MessageEvent> captor = ArgumentCaptor.forClass(MessageEvent.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(MessageEventStatus.COMPLETED);
        }

        @Test
        @DisplayName("Should not throw exception when entity not found")
        void shouldNotThrowExceptionWhenEntityNotFound() {
            // Arrange
            when(repository.findById(testEntityId)).thenReturn(Optional.empty());

            // Act & Assert
            messageEventService.updateToCompleted(testEntityId);

            // Assert
            verify(repository).findById(testEntityId);
            verify(repository, never()).save(any(MessageEvent.class));
        }

        @Test
        @DisplayName("Should call repository save once when entity exists")
        void shouldCallRepositorySaveOnceWhenEntityExists() {
            // Arrange
            when(repository.findById(testEntityId)).thenReturn(Optional.of(messageEvent));

            // Act
            messageEventService.updateToCompleted(testEntityId);

            // Assert
            verify(repository, times(1)).save(any(MessageEvent.class));
        }
    }

    @Nested
    @DisplayName("updateToValidationFailed() Tests")
    class UpdateToValidationFailedTests {

        @Test
        @DisplayName("Should update status to VALIDATION_FAILED when entity exists")
        void shouldUpdateStatusToValidationFailedWhenEntityExists() {
            // Arrange
            when(repository.findById(testEntityId)).thenReturn(Optional.of(messageEvent));

            // Act
            messageEventService.updateToValidationFailed(testEntityId);

            // Assert
            ArgumentCaptor<MessageEvent> captor = ArgumentCaptor.forClass(MessageEvent.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(MessageEventStatus.VALIDATION_FAILED);
        }

        @Test
        @DisplayName("Should set error message")
        void shouldSetErrorMessage() {
            // Arrange
            when(repository.findById(testEntityId)).thenReturn(Optional.of(messageEvent));

            // Act
            messageEventService.updateToValidationFailed(testEntityId);

            // Assert
            ArgumentCaptor<MessageEvent> captor = ArgumentCaptor.forClass(MessageEvent.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getErrorMessage()).isEqualTo("Message did not pass business rule validation");
        }

        @Test
        @DisplayName("Should not save when entity not found")
        void shouldNotSaveWhenEntityNotFound() {
            // Arrange
            when(repository.findById(testEntityId)).thenReturn(Optional.empty());

            // Act
            messageEventService.updateToValidationFailed(testEntityId);

            // Assert
            verify(repository, never()).save(any(MessageEvent.class));
        }
    }

    @Nested
    @DisplayName("updateToTransformationFailed() Tests")
    class UpdateToTransformationFailedTests {

        @Test
        @DisplayName("Should update status to TRANSFORMATION_FAILED when entity exists")
        void shouldUpdateStatusToTransformationFailedWhenEntityExists() {
            // Arrange
            when(repository.findById(testEntityId)).thenReturn(Optional.of(messageEvent));

            // Act
            messageEventService.updateToTransformationFailed(testEntityId, "Transformation error");

            // Assert
            ArgumentCaptor<MessageEvent> captor = ArgumentCaptor.forClass(MessageEvent.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(MessageEventStatus.TRANSFORMATION_FAILED);
        }

        @Test
        @DisplayName("Should set custom error message")
        void shouldSetCustomErrorMessage() {
            // Arrange
            String errorMessage = "Field 'orderId' is missing";
            when(repository.findById(testEntityId)).thenReturn(Optional.of(messageEvent));

            // Act
            messageEventService.updateToTransformationFailed(testEntityId, errorMessage);

            // Assert
            ArgumentCaptor<MessageEvent> captor = ArgumentCaptor.forClass(MessageEvent.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getErrorMessage()).isEqualTo(errorMessage);
        }

        @Test
        @DisplayName("Should handle null error message")
        void shouldHandleNullErrorMessage() {
            // Arrange
            when(repository.findById(testEntityId)).thenReturn(Optional.of(messageEvent));

            // Act
            messageEventService.updateToTransformationFailed(testEntityId, null);

            // Assert
            ArgumentCaptor<MessageEvent> captor = ArgumentCaptor.forClass(MessageEvent.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getErrorMessage()).isNull();
        }

        @Test
        @DisplayName("Should not save when entity not found")
        void shouldNotSaveWhenEntityNotFound() {
            // Arrange
            when(repository.findById(testEntityId)).thenReturn(Optional.empty());

            // Act
            messageEventService.updateToTransformationFailed(testEntityId, "Error");

            // Assert
            verify(repository, never()).save(any(MessageEvent.class));
        }
    }

    @Nested
    @DisplayName("updateToSendingFailed() Tests")
    class UpdateToSendingFailedTests {

        @Test
        @DisplayName("Should update status to SENDING_FAILED when entity exists")
        void shouldUpdateStatusToSendingFailedWhenEntityExists() {
            // Arrange
            when(repository.findById(testEntityId)).thenReturn(Optional.of(messageEvent));

            // Act
            messageEventService.updateToSendingFailed(testEntityId, "Kafka connection failed");

            // Assert
            ArgumentCaptor<MessageEvent> captor = ArgumentCaptor.forClass(MessageEvent.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(MessageEventStatus.SENDING_FAILED);
        }

        @Test
        @DisplayName("Should set error message")
        void shouldSetErrorMessage() {
            // Arrange
            String errorMessage = "Topic not available";
            when(repository.findById(testEntityId)).thenReturn(Optional.of(messageEvent));

            // Act
            messageEventService.updateToSendingFailed(testEntityId, errorMessage);

            // Assert
            ArgumentCaptor<MessageEvent> captor = ArgumentCaptor.forClass(MessageEvent.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getErrorMessage()).isEqualTo(errorMessage);
        }

        @Test
        @DisplayName("Should not save when entity not found")
        void shouldNotSaveWhenEntityNotFound() {
            // Arrange
            when(repository.findById(testEntityId)).thenReturn(Optional.empty());

            // Act
            messageEventService.updateToSendingFailed(testEntityId, "Error");

            // Assert
            verify(repository, never()).save(any(MessageEvent.class));
        }
    }

    @Nested
    @DisplayName("updateToSystemError() Tests")
    class UpdateToSystemErrorTests {

        @Test
        @DisplayName("Should update status to SYSTEM_ERROR when entity exists")
        void shouldUpdateStatusToSystemErrorWhenEntityExists() {
            // Arrange
            when(repository.findById(testEntityId)).thenReturn(Optional.of(messageEvent));

            // Act
            messageEventService.updateToSystemError(testEntityId, "NullPointerException");

            // Assert
            ArgumentCaptor<MessageEvent> captor = ArgumentCaptor.forClass(MessageEvent.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(MessageEventStatus.SYSTEM_ERROR);
        }

        @Test
        @DisplayName("Should set error message")
        void shouldSetErrorMessage() {
            // Arrange
            String errorMessage = "Database connection failed";
            when(repository.findById(testEntityId)).thenReturn(Optional.of(messageEvent));

            // Act
            messageEventService.updateToSystemError(testEntityId, errorMessage);

            // Assert
            ArgumentCaptor<MessageEvent> captor = ArgumentCaptor.forClass(MessageEvent.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getErrorMessage()).isEqualTo(errorMessage);
        }

        @Test
        @DisplayName("Should handle long error messages")
        void shouldHandleLongErrorMessages() {
            // Arrange
            String longErrorMessage = "Very long error message ".repeat(100);
            when(repository.findById(testEntityId)).thenReturn(Optional.of(messageEvent));

            // Act
            messageEventService.updateToSystemError(testEntityId, longErrorMessage);

            // Assert
            ArgumentCaptor<MessageEvent> captor = ArgumentCaptor.forClass(MessageEvent.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getErrorMessage()).isEqualTo(longErrorMessage);
        }

        @Test
        @DisplayName("Should not save when entity not found")
        void shouldNotSaveWhenEntityNotFound() {
            // Arrange
            when(repository.findById(testEntityId)).thenReturn(Optional.empty());

            // Act
            messageEventService.updateToSystemError(testEntityId, "Error");

            // Assert
            verify(repository, never()).save(any(MessageEvent.class));
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle complete workflow: create -> update to completed")
        void shouldHandleCompleteWorkflow() {
            // Arrange
            when(repository.save(any(MessageEvent.class))).thenReturn(messageEvent);
            when(repository.findById(testEntityId)).thenReturn(Optional.of(messageEvent));

            // Act
            messageEventService.createInitialRecord(eventDrivenMessage, "ORDER_PROCESSING");
            messageEventService.updateToCompleted(testEntityId);

            // Assert
            verify(repository, times(2)).save(any(MessageEvent.class));
        }

        @Test
        @DisplayName("Should handle failure workflow: create -> validation failed")
        void shouldHandleFailureWorkflow() {
            // Arrange
            when(repository.save(any(MessageEvent.class))).thenReturn(messageEvent);
            when(repository.findById(testEntityId)).thenReturn(Optional.of(messageEvent));

            // Act
            messageEventService.createInitialRecord(eventDrivenMessage, "ORDER_PROCESSING");
            messageEventService.updateToValidationFailed(testEntityId);

            // Assert
            verify(repository, times(2)).save(any(MessageEvent.class));
        }

    }
}

