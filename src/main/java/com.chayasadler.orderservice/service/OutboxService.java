package com.chayasadler.orderservice.service;

import com.chayasadler.orderservice.dao.IOutboxEventRepository;
import com.chayasadler.orderservice.model.OutBoxEvent;
import com.chayasadler.orderservice.util.EventStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OutboxService {

    @Autowired
    IOutboxEventRepository iOutboxEventRepository;

    public void saveEvent(String eventType, String orderId, String payload) {

        OutBoxEvent outBoxEvent = new OutBoxEvent();

        outBoxEvent.setEventType(eventType);
        outBoxEvent.setAggregateId(orderId);
        outBoxEvent.setAggregateType("order");
        outBoxEvent.setPayload(payload);
        outBoxEvent.setStatus(EventStatus.UNSENT.name());
        outBoxEvent.setCreatedAt(LocalDateTime.now());

        iOutboxEventRepository.save(outBoxEvent);
    }
}
