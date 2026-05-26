package com.event.ledger.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.event.ledger.entity.EventEntity;

public interface EventRepo extends JpaRepository<EventEntity, Long> {

    Optional<EventEntity> findByEventId(String eventId);

    boolean existsByEventId(String eventId);

    List<EventEntity> findByAccountIdOrderByEventTimestampAsc(
            String accountId
    );
}