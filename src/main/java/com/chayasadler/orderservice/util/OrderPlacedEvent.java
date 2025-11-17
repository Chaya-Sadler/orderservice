package com.chayasadler.orderservice.util;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderPlacedEvent(UUID messageId,
                               String orderId,
                               String customerId,
                               Double totalAmt,
                               List<OrderItemEvent> orderItemEventList,
                               String orderStatus,
                               String paymentStatus,
                               LocalDateTime timestamp) {
}
