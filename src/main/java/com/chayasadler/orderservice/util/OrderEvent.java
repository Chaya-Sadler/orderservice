package com.chayasadler.orderservice.util;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderEvent(UUID messageId,
                         String eventType,
                         String orderId,
                         String customerId,
                         Double totalAmt,
                         List<OrderItemEvent> orderItemEventList,
                         String orderStatus,
                         String paymentStatus,
                         LocalDateTime timestamp) {
}
