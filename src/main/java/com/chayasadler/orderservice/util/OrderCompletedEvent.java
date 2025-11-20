package com.chayasadler.orderservice.util;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderCompletedEvent(
        UUID messageId,
        String orderId,
        LocalDateTime completeddAt,
        String customerId,
        //corelationid -TODO
        //causationId - message id of the event that caused this event
        String status
) {
}
