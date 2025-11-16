package com.chayasadler.orderservice.util;

public record OrderItemEvent(String productId,
                             Integer quantity) {}
