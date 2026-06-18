package com.nikolay.kochev.eventdrivenruleengine.persistence.repository;

import com.nikolay.kochev.eventdrivenruleengine.persistence.entity.MessageEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageEventRepository extends JpaRepository<MessageEvent, UUID> {

    Optional<MessageEvent> findByMessageId(String messageId);

    boolean existsByMessageId(String messageId);
}

