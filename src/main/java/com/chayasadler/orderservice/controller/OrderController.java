package com.chayasadler.orderservice.controller;

import com.chayasadler.orderservice.model.OrderRequest;
import com.chayasadler.orderservice.service.OrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    OrderService orderService;

    OrderController(OrderService orderService){
        this.orderService = orderService;
    }

   @PostMapping("/app/orders")
    public void createOrder(@RequestBody List<OrderRequest> orderRequestList){

        orderService.createOrder(orderRequestList);
   }
}
