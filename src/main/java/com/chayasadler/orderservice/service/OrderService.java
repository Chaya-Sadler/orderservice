package com.chayasadler.orderservice.service;

import com.chayasadler.orderservice.model.OrderRequest;
import com.chayasadler.orderservice.model.SaleOrder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    public void createOrder(List<OrderRequest> orderRequestList) {

        for(OrderRequest orderRequest : orderRequestList) {
            SaleOrder saleOrder = new SaleOrder();

            //get product details from productservice
            //calculate total price

        }

    }
}
