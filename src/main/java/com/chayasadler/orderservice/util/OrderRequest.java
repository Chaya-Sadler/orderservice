package com.chayasadler.orderservice.util;

import java.util.UUID;

public record OrderRequest(UUID productId, Integer quantity) {
}
