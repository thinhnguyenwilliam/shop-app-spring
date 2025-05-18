package com.example.shopapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.shopapp.models.OrderDetail;
import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail,Integer>
{
    List<OrderDetail> findAllByOrderId(Integer orderId);
}
