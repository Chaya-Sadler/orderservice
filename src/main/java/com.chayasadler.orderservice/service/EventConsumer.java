package com.chayasadler.orderservice.service;

import com.chayasadler.orderservice.util.OrderEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.query.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class EventConsumer {

    @Autowired
    OrderService orderService;

    @Autowired
    ObjectMapper mapper;

    @KafkaListener(topics = "payment.events", groupId = "order-service-payment")
    public void onPaymentEvent(String payload, Acknowledgment acknowledgment) {

        try{
            OrderEvent event = mapper.readValue(payload, OrderEvent.class);
            switch (event.eventType()) {
                case "PaymentCompleted" -> {
                    orderService.completeOrder(event);
                    acknowledgment.acknowledge();
                }
                case "PaymentFailed" -> {
                    orderService.cancelOrder(event, "PAYMENT_FAILED");
                    acknowledgment.acknowledge();
                }
                default -> {
                    System.err.println("Received wrong event type in Payment : " + event.eventType());
                }
            }

        }catch (Exception exe) {
            System.err.println(" Failed to consumer Payment Service payload " + exe.getMessage());
        }
    }

    @KafkaListener(topics = "inventory.events", groupId = "order-service-inventory")
    public void onInventoryEvent(String payload, Acknowledgment acknowledgment) {
        try {
            OrderEvent event = mapper.readValue(payload, OrderEvent.class);
            if(event.eventType().equals("InventoryFailed")){
                orderService.cancelOrder(event, "INVENTORY_FAILED");
                acknowledgment.acknowledge();
            }

        } catch (Exception exe) {
            System.err.println(" Failed to process OrderReserved event from order service, offset is not committed");
        }
    }
}
