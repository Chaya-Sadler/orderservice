package com.chayasadler.orderservice.service;

import com.chayasadler.orderservice.dao.IOrderRepository;
import com.chayasadler.orderservice.model.*;
import com.chayasadler.orderservice.ofeignclient.ProductServiceFeignClient;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    IOrderRepository iOrderRepository;

    @Autowired
    ProductServiceFeignClient productServiceFeignClient;

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

        SaleOrder saleOrderCreated = iOrderRepository.save(saleOrder);

        OrderResponse orderResponse = new OrderResponse(OrderStatus.CREATED.name(), saleOrderCreated.getId(),
                totalProductPrice);
        return new ResponseEntity<>(orderResponse, HttpStatus.OK);
    }
}
