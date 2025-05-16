package com.example.shopapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDetail extends JpaRepository<OrderDetail,Integer>
{
    List<OrderDetail> findAllByOrderId(Integer orderId);
}
