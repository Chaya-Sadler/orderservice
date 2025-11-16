package com.chayasadler.orderservice.dao;

import com.chayasadler.orderservice.model.OutBoxEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IOutboxEventRepository extends JpaRepository<OutBoxEvent, UUID> {

    @Query("Select e from OutBoxEvent e where e.processedAt is null and e.status ='UNSENT' ORDER by e.createdAt")
    List<OutBoxEvent> findUnsentEvents(Pageable pageable);
}
