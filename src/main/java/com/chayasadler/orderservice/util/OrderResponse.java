package com.chayasadler.orderservice.util;

import java.util.UUID;

public record OrderResponse(String orderStatus, UUID orderId, Double totalAmount) {
}
