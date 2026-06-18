package com.nikolay.kochev.eventdrivenruleengine.messaging.producer;

import com.nikolay.kochev.eventdrivenruleengine.exception.ProcessingException;
import com.nikolay.kochev.eventdrivenruleengine.messaging.model.OutgoingKafkaMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class KafkaMessageProducer {

    private final KafkaTemplate<String, OutgoingKafkaMessage> kafkaTemplate;
    @Value(value = "${engine.kafka.output-topic}")
    private String topic;

    public void sendMessage(OutgoingKafkaMessage message) {
        kafkaTemplate.send(topic, message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        handleException(ex);
                    }
                });
    }

    private void handleException(Throwable ex) {
        throw new ProcessingException("Failed to send message: " + ex.getMessage());
    }
}

