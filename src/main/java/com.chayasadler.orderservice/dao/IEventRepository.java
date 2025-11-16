package com.chayasadler.orderservice.dao;

import com.chayasadler.orderservice.model.OutBoxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IEventRepository extends JpaRepository<OutBoxEvent, Integer> {
}
