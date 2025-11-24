package com.chayasadler.orderservice.service;

import com.chayasadler.orderservice.dao.IOrderRepository;
import com.chayasadler.orderservice.dao.IProcessedEventRepository;
import com.chayasadler.orderservice.model.*;
import com.chayasadler.orderservice.ofeignclient.ProductServiceFeignClient;
import com.chayasadler.orderservice.util.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    IOrderRepository iOrderRepository;

    @Autowired
    ProductServiceFeignClient productServiceFeignClient;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    OutboxService outboxService;

    @Autowired
    private IProcessedEventRepository iProcessedEventRepository;

    @Transactional
    public ResponseEntity<OrderResponse> createOrder(List<OrderRequest> orderRequestList, String customerId) {

        double totalProductPrice = 0.0;
        List<OrderItem> orderItemList = new ArrayList<>();
        SaleOrder saleOrder = new SaleOrder();

        for (OrderRequest orderRequest : orderRequestList) {

            UUID productId = orderRequest.productId();
            ProductResponse productResponse = productServiceFeignClient.getProductById(productId);
            if (productResponse != null) {
                if (orderRequest.quantity() > productResponse.unit()) {
                    return new ResponseEntity<>(null, HttpStatus.CONFLICT);
                }
                totalProductPrice += productResponse.price() * orderRequest.quantity();

                OrderItem orderItem = new OrderItem();
                orderItem.setProductId(orderRequest.productId());
                orderItem.setQuantity(orderRequest.quantity());

                saleOrder.addOrderItems(orderItem);
            }
        }


        saleOrder.setCustomerId(UUID.fromString(customerId));
        saleOrder.setTotalPrice(totalProductPrice);
        saleOrder.setStatus(OrderStatus.CREATED.name());
        saleOrder.setCreatedAt(LocalDateTime.now());

        //save the order in db
        SaleOrder saleOrderCreated = iOrderRepository.save(saleOrder);

        /* 1.creating payload for kafka message
           2. setting up order-items for the order*/
        List<OrderItem> orderItems = saleOrderCreated.getItems();
        List<OrderItemEvent> orderItemEventList = orderItems.stream()
                .map(item -> new OrderItemEvent(
                        item.getProductId().toString(),
                        item.getQuantity()
                ))
                .toList();


        OrderEvent orderPlacedEvent = new OrderEvent(
                UUID.randomUUID(), //uniquely identiy the message
                "OrderPlaced", //eventType
                saleOrderCreated.getId().toString(),
                customerId,
                saleOrderCreated.getTotalPrice(),
                orderItemEventList,
                "OrderPlaced",
                "PENDING",
                LocalDateTime.now()
        );
        try {
            String payload = objectMapper.writeValueAsString(orderPlacedEvent);
            //saving outbox event in db
            outboxService.saveEvent("OrderPlaced", saleOrderCreated.getId().toString(), payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        OrderResponse orderResponse = new OrderResponse(OrderStatus.CREATED.name(), saleOrderCreated.getId(),
                totalProductPrice);
        return new ResponseEntity<>(orderResponse, HttpStatus.OK);
    }

    @Transactional
    public void cancelOrder(OrderEvent orderEvent, String cancelReason) {

        //check if the incoming message was already processed - Idempotency( Consumer part)
        Optional<ProcessedEvent> findProcessedEvent = iProcessedEventRepository.
                findByEventId((orderEvent.messageId()));
        if(findProcessedEvent.isEmpty()){
            SaleOrder saleOrder = iOrderRepository.findById(UUID.fromString(orderEvent.orderId()))
                    .orElseThrow();
            saleOrder.setStatus(OrderStatus.CANCELLED.name());
            saleOrder.setCancelReason(cancelReason);

            OrderEvent orderCancelledEvent = new OrderEvent(
                    UUID.randomUUID(), //uniquely identiy the message
                    "OrderCancelled", //eventType
                    orderEvent.orderId(),
                    orderEvent.customerId(),
                    orderEvent.totalAmt(),
                    orderEvent.orderItemEventList(),
                    OrderStatus.CANCELLED.name(),
                    "NONE",
                    LocalDateTime.now());

            try {
                String payload = objectMapper.writeValueAsString(orderCancelledEvent);
                outboxService.saveEvent("OrderCancelled", orderEvent.orderId(), payload);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            ProcessedEvent processedEvent = new ProcessedEvent();
            processedEvent.setEventId(orderEvent.messageId());
            processedEvent.setEventType(orderEvent.orderStatus());
            processedEvent.setProcessedAt(LocalDateTime.now());

            iProcessedEventRepository.save(processedEvent);
        }


    }

    @Transactional
    public void completeOrder(OrderEvent orderEvent) {

        //check if the incoming message was already processed - Idempotency( Consumer part)
        Optional<ProcessedEvent> findProcessedEvent = iProcessedEventRepository.
                findByEventId((orderEvent.messageId()));
        if(findProcessedEvent.isEmpty()){
            SaleOrder saleOrder = iOrderRepository.findById(UUID.fromString(orderEvent.orderId()))
                    .orElseThrow();
            saleOrder.setStatus(OrderStatus.CONFIRMED.name());

            OrderEvent orderCompletedEvent = new OrderEvent(
                    UUID.randomUUID(), //uniquely identiy the message
                    "OrderCompleted", //eventType
                    orderEvent.orderId(),
                    orderEvent.customerId(),
                    orderEvent.totalAmt(),
                    orderEvent.orderItemEventList(),
                    OrderStatus.CONFIRMED.name(),
                    "PaymentCompleted",
                    LocalDateTime.now());
            try {
                String payload = objectMapper.writeValueAsString(orderCompletedEvent);
                outboxService.saveEvent("OrderCompleted", orderEvent.orderId(), payload);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            ProcessedEvent processedEvent = new ProcessedEvent();
            processedEvent.setEventId(orderEvent.messageId());
            processedEvent.setEventType(orderEvent.orderStatus());
            processedEvent.setProcessedAt(LocalDateTime.now());

            iProcessedEventRepository.save(processedEvent);
        }
    }
}
