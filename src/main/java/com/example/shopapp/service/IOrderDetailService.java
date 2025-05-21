package com.example.shopapp.service;

import com.example.shopapp.dtos.request.OrderDetailDTO;
import com.example.shopapp.models.OrderDetail;

import java.util.List;

public interface IOrderDetailService
{
    OrderDetail createOrderDetail(OrderDetailDTO orderDetailDTO);
    OrderDetail getOrderDetailById(Integer id);
    OrderDetail updateOrderDetail(Integer id, OrderDetailDTO orderDetailDTO);
    void deleteOrderDetailById(Integer id);
    List<OrderDetail> findAllByOrderId(Integer orderId);
}
