package com.chayasadler.orderservice.dao;

import com.chayasadler.orderservice.model.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {

    @Query( " SELECT pe FROM ProcessedEvent WHERE pe.eventId =:messageID")
    public Optional<ProcessedEvent> findByEventId(UUID messageId);
}
