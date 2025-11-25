package com.chayasadler.orderservice.controller;

import com.chayasadler.orderservice.util.OrderRequest;
import com.chayasadler.orderservice.util.OrderResponse;
import com.chayasadler.orderservice.service.OrderService;
import com.chayasadler.orderservice.util.OrderStatus;
import org.hibernate.query.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody List<OrderRequest> orderRequestList,
                                                     @RequestHeader("X-Customer-Id") String customerId) {

        return orderService.createOrder(orderRequestList, customerId);
    }
}
