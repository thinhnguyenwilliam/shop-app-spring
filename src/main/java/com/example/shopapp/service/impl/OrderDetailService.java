package com.example.shopapp.service.impl;

import com.example.shopapp.dtos.request.OrderDetailDTO;
import com.example.shopapp.models.Order;
import com.example.shopapp.models.OrderDetail;
import com.example.shopapp.models.Product;
import com.example.shopapp.repositories.OrderDetailRepository;
import com.example.shopapp.repositories.OrderRepository;
import com.example.shopapp.repositories.ProductRepository;
import com.example.shopapp.service.IOrderDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderDetailService implements IOrderDetailService
{
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;

    @Override
    public OrderDetail createOrderDetail(OrderDetailDTO orderDetailDTO) {
        Order order=orderRepository.findById(orderDetailDTO.getOrderId())
                .orElseThrow(()->new RuntimeException("Order not found for id: "+orderDetailDTO.getOrderId()));

        Product product=productRepository.findById(orderDetailDTO.getProductId())
                .orElseThrow(()->new RuntimeException("Product not found for id: "+orderDetailDTO.getProductId()));

        OrderDetail orderDetail=OrderDetail.builder()
                .order(order)
                .product(product)
                .numberOfProducts(orderDetailDTO.getNumberOfProducts())
                .totalMoney(orderDetailDTO.getTotalMoney())
                .price(orderDetailDTO.getPrice())
                .color(orderDetailDTO.getColor() )
                .build();

        return orderDetailRepository.save(orderDetail);
    }

    @Override
    public OrderDetail getOrderDetailById(Integer id) {
        return orderDetailRepository.findById(id)
                .orElseThrow(()->new RuntimeException("OrderDetail not found for id: "+id));
    }

    @Override
    public OrderDetail updateOrderDetail(Integer id, OrderDetailDTO orderDetailDTO) {
        // Step 1: Retrieve existing OrderDetail
        OrderDetail existingOrderDetail = orderDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OrderDetail not found for id: " + id));

        // Step 2: Retrieve related Order
        Order order = orderRepository.findById(orderDetailDTO.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found for id: " + orderDetailDTO.getOrderId()));

        // Step 3: Retrieve related Product
        Product product = productRepository.findById(orderDetailDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found for id: " + orderDetailDTO.getProductId()));

        // Step 4: Update fields
        existingOrderDetail.setOrder(order);
        existingOrderDetail.setProduct(product);
        existingOrderDetail.setNumberOfProducts(orderDetailDTO.getNumberOfProducts());
        existingOrderDetail.setTotalMoney(orderDetailDTO.getTotalMoney());
        existingOrderDetail.setColor(orderDetailDTO.getColor());
        existingOrderDetail.setPrice(orderDetailDTO.getPrice());

        // Step 5: Save and return
        return orderDetailRepository.save(existingOrderDetail);
    }


    @Override
    public void deleteOrderDetailById(Integer id) {
        orderDetailRepository.deleteById(id);
    }

    @Override
    public List<OrderDetail> findAllByOrderId(Integer orderId) {
        return orderDetailRepository.findAllByOrderId(orderId);
    }
}
