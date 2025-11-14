package com.chayasadler.orderservice.model;

import java.util.UUID;

public record OrderResponse(String orderStatus, UUID orderId, Double totalAmount) {
}
