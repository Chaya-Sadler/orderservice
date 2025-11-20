package com.chayasadler.orderservice.service;

import com.chayasadler.orderservice.dao.IEventRepository;
import com.chayasadler.orderservice.dao.IOrderRepository;
import com.chayasadler.orderservice.model.*;
import com.chayasadler.orderservice.ofeignclient.ProductServiceFeignClient;
import com.chayasadler.orderservice.util.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.discovery.converters.Auto;
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
    IEventRepository iEventRepository;

    @Autowired
    ProductServiceFeignClient productServiceFeignClient;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    OutboxService outboxService;

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
                "OrderPlaced",
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
    public void cancelOrder(String orderId, String customerId, String cancelReason) {
        SaleOrder saleOrder = iOrderRepository.findById(UUID.fromString(orderId))
                .orElseThrow();
        saleOrder.setStatus(OrderStatus.CANCELLED.name());
        saleOrder.setCancelReason(cancelReason);

        OrderCancelledEvent cancelledEvent = new OrderCancelledEvent(
                UUID.randomUUID(), //unique messageId for idempotency
                orderId,
                cancelReason,
                LocalDateTime.now(),
                customerId
                        );
        try {
            String payload = objectMapper.writeValueAsString(cancelledEvent);
            outboxService.saveEvent("OrderCancelled", orderId, payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void completeOrder(String orderId, String customerId) {

        SaleOrder saleOrder = iOrderRepository.findById(UUID.fromString(orderId))
                .orElseThrow();
        saleOrder.setStatus(OrderStatus.CONFIRMED.name());

        OrderCompletedEvent completedEvent = new OrderCompletedEvent(
                UUID.randomUUID(),
                orderId,
                LocalDateTime.now(),
                customerId,
                OrderStatus.CONFIRMED.name()
        );
        try {
            String payload = objectMapper.writeValueAsString(completedEvent);
            outboxService.saveEvent("OrderCompleted", orderId, payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
