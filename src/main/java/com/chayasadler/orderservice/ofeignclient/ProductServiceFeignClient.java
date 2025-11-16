package com.chayasadler.orderservice.ofeignclient;

import com.chayasadler.orderservice.util.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient("PRODUCTSERVICE")
public interface ProductServiceFeignClient {

    @GetMapping("/app/products/{id}")
    public ProductResponse getProductById(@PathVariable UUID id);
}
