package com.nikolay.kochev.eventdrivenruleengine.persistence.repository;

import com.nikolay.kochev.eventdrivenruleengine.persistence.entity.MessageEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MessageEventRepository extends JpaRepository<MessageEvent, UUID> {

    boolean existsByMessageId(String messageId);
}

