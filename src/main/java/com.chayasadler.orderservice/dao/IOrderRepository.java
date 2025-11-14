package com.chayasadler.orderservice.dao;

import com.chayasadler.orderservice.model.SaleOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IOrderRepository extends JpaRepository<SaleOrder, UUID> {
}
