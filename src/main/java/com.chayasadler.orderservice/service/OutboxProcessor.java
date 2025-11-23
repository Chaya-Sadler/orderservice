package com.chayasadler.orderservice.service;

import com.chayasadler.orderservice.dao.IOutboxEventRepository;
import com.chayasadler.orderservice.model.OutBoxEvent;
import com.chayasadler.orderservice.util.EventStatus;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutboxProcessor {

    @Autowired
    IOutboxEventRepository iOutboxEventRepository;

    @Autowired
    EventProducer eventProducer;

    @Value("${spring.topic.order.name}")
    private String topicName;

    private final Pageable pageSize = PageRequest.of(0, 10);

    @Transactional
    @Scheduled(fixedDelay = 3000) //runs every 30s
    public void processOutBoxEvents(){

        //get all unsent order events
        List<OutBoxEvent> eventsList = iOutboxEventRepository.findUnsentEvents(pageSize);

        if(eventsList.isEmpty())
            return;

        for(OutBoxEvent event : eventsList) {
            try{
                eventProducer.publish(
                        topicName, //order.events - topic name
                        event.getAggregateId(), //order id - message key, all events related to same order goes into same partition
                        event.getPayload() // message
                ).join(); //wait for success, forces async call to complete before continuing

                //update event processed.
                event.setProcessedAt(LocalDateTime.now());
                event.setStatus(EventStatus.SENT.name());
                iOutboxEventRepository.save(event);

            } catch (Exception exe) {
                    System.out.println("Failed to publish the event : " + event.getId() + exe.getMessage());
            }
        }
    }
}
