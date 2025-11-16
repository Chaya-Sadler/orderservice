package com.chayasadler.orderservice.service;

import com.chayasadler.orderservice.dao.IEventRepository;
import com.chayasadler.orderservice.dao.IOrderRepository;
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
import java.util.UUID;
import java.util.stream.Collectors;

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


        OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent(
                UUID.randomUUID(), //uniquely identiy the message
                saleOrderCreated.getId().toString(),
                customerId,
                saleOrderCreated.getTotalPrice(),
                orderItemEventList,
                saleOrderCreated.getStatus(),
                "PENDING",
                LocalDateTime.now()
                );
        String payload;
        try {
            payload = objectMapper.writeValueAsString(orderPlacedEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        //saving outbox event in db
        OutBoxEvent outBoxEvent = new OutBoxEvent();
        outBoxEvent.setEventType("order-events");
        outBoxEvent.setAggregateId(saleOrderCreated.getId().toString());
        outBoxEvent.setAggregateType("order");
        outBoxEvent.setPayload(payload);
        outBoxEvent.setStatus(EventStatus.UNSENT.name());
        outBoxEvent.setCreatedAt(LocalDateTime.now());
        iEventRepository.save(outBoxEvent);

        OrderResponse orderResponse = new OrderResponse(OrderStatus.CREATED.name(), saleOrderCreated.getId(),
                totalProductPrice);
        return new ResponseEntity<>(orderResponse, HttpStatus.OK);
    }
}
