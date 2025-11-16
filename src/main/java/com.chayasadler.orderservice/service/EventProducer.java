package com.chayasadler.orderservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;


@Service
public class EventProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public CompletableFuture<Void> publish(String topic, String key, String payload) {
       return kafkaTemplate.send(topic, key, payload)
               //.toCompletableFuture()
               .thenAccept(SendResult -> {});
    }
}
