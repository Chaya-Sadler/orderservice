package com.chayasadler.orderservice.util;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderCancelledEvent(
        UUID messageId,
        String orderId,
        String reason,
        LocalDateTime cancelledAt,
        String customerId
        //corelationid -TODO
) {}
