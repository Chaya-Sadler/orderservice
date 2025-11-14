package com.chayasadler.orderservice.model;

import java.util.UUID;

public record OrderRequest(UUID productId, Integer quantity) {
}
