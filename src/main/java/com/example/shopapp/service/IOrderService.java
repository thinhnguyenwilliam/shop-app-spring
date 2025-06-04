package com.example.shopapp.service;

import com.example.shopapp.dtos.request.OrderDTO;
import com.example.shopapp.dtos.responses.OrderResponse;
import com.example.shopapp.models.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IOrderService {
    OrderResponse createOrder(OrderDTO orderDTO);
    Order getOrderById(Integer id);
    Order updateOrder(Integer id, OrderDTO orderDTO);
    void deleteOrderById(Integer id);
    List<Order> findByUserId(Integer userId);
    Page<Order> getOrdersByKeyword(String keyword, Pageable pageable);
}
