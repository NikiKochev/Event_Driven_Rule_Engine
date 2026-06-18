package com.nikolay.kochev.eventdrivenruleengine.persistence.entity;

import com.nikolay.kochev.eventdrivenruleengine.persistence.enums.MessageEventStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;


@Entity
@Table(name = "message_processing_records", indexes = {
        @Index(name = "idx_message_id", columnList = "messageId")
})
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageEvent {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "message_id", updatable = false, nullable = false)
    private String messageId;

    @Column(name = "message_type")
    private String messageType;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false)
    private MessageEventStatus status = MessageEventStatus.RECEIVED;

    @Column(name = "error_message")
    private String errorMessage;

    @Column
    private Integer transformationCount;


    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;


    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (status == MessageEventStatus.COMPLETED
                || status == MessageEventStatus.VALIDATION_FAILED
                || status == MessageEventStatus.TRANSFORMATION_FAILED
                || status == MessageEventStatus.SENDING_FAILED
                || status == MessageEventStatus.SYSTEM_ERROR) {
                completedAt = OffsetDateTime.now();

        }
    }
}

