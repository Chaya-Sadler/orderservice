package com.chayasadler.orderservice.controller;

import com.chayasadler.orderservice.model.OrderRequest;
import com.chayasadler.orderservice.model.OrderResponse;
import com.chayasadler.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody List<OrderRequest> orderRequestList,
                                                     @RequestHeader("X-Customer-Id") String customerId) {

        return orderService.createOrder(orderRequestList, customerId);
    }
}
