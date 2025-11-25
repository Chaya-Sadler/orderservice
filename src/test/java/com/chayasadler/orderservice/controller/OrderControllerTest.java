package com.chayasadler.orderservice.controller;

import com.chayasadler.orderservice.service.OrderService;
import com.chayasadler.orderservice.util.OrderRequest;
import com.chayasadler.orderservice.util.OrderResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    OrderService orderService;

    @Autowired
    ObjectMapper mapper;


    @Test
    void createOrderTest() throws Exception {
        // sample input date for the request
        List<OrderRequest> orderRequestList = List.of(new OrderRequest(
                        UUID.fromString("e19daa16-0d51-4781-836c-b7ec76d51148"), 2),
                new OrderRequest(UUID.fromString("d0c33e4f-940b-47f6-90fe-bde2342900fe"), 3));
        String customerId = UUID.randomUUID().toString();

        //sample data output from the service
        UUID orderId = UUID.fromString("a3f7be62-7f7c-44b4-88e7-fd5affab7b2b");
        OrderResponse orderResponse = new OrderResponse("CREATED", orderId, 850.0);

        when(orderService.createOrder(any(List.class), eq(customerId))).thenReturn(ResponseEntity.ok(orderResponse));

        //perform the post request using mockMvc
        mockMvc.perform(post("/app/orders")
                .header("X-Customer-Id", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(orderRequestList)))
                        .andExpect(status().isOk());


    }
}
