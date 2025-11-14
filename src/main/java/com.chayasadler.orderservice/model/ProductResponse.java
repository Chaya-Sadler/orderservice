package com.chayasadler.orderservice.model;

public record ProductResponse(String name, String description, Double price, Integer unit,
                              String category, Boolean active) {
}
