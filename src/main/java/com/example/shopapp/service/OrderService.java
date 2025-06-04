package com.example.shopapp.service;

import com.example.shopapp.dtos.request.CartItemDTO;
import com.example.shopapp.dtos.request.OrderDTO;
import com.example.shopapp.dtos.responses.OrderResponse;
import com.example.shopapp.models.*;
import com.example.shopapp.repositories.OrderRepository;
import com.example.shopapp.repositories.ProductRepository;
import com.example.shopapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService
{
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;


    @Override
    public OrderResponse createOrder(OrderDTO orderDTO) {
        User user = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found for id: " + orderDTO.getUserId()));

        // Map OrderDTO to Order entity
        Order order = modelMapper.map(orderDTO, Order.class);

        order.setUser(user);
        order.setOrderDate(new Date());
        order.setStatus(OrderStatus.PENDING);

        Date now = new Date();
        Date shippingDate = orderDTO.getShippingDate() == null ? now : orderDTO.getShippingDate();

        if (shippingDate.before(now)) {
            throw new RuntimeException("Shipping date must be in the future");
        }

        order.setShippingDate(shippingDate);
        order.setActive(true);

        // Initialize orderDetails list
        List<OrderDetail> orderDetails = new ArrayList<>();

        for (CartItemDTO cartItemDTO : orderDTO.getCartItems()) {
            Product product = productRepository.findById(cartItemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + cartItemDTO.getProductId()));

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setProduct(product);
            orderDetail.setNumberOfProducts(cartItemDTO.getQuantity());
            orderDetail.setPrice(product.getPrice());

            orderDetails.add(orderDetail);
        }

        // Set order details and save order
        order.setOrderDetails(orderDetails);
        Order savedOrder = orderRepository.save(order); // Cascade will save orderDetails too

        // Map to response
        return modelMapper.map(savedOrder, OrderResponse.class);
    }


    @Override
    public Order getOrderById(Integer id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found for id: " + id));
    }

    @Override
    @Transactional
    public Order updateOrder(Integer id, OrderDTO orderDTO) {
        // Find existing order
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found for id: " + id));

        // Find the user to update
        User existingUser = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found for id: " + orderDTO.getUserId()));

        // Update fields from DTO into the existing order
        modelMapper.map(orderDTO, existingOrder);

        // Set the user manually (ModelMapper can't resolve userId -> User)
        existingOrder.setUser(existingUser);

        // Save and return updated order
        return orderRepository.save(existingOrder);
    }



    @Override
    @Transactional
    public void deleteOrderById(Integer id) {
        Order order = getOrderById(id); // should already throw if not found

        if (Boolean.FALSE.equals(order.getActive())) {
            // Optional: avoid saving if already inactive
            return;
        }

        order.setActive(false);
        orderRepository.save(order);
    }


    @Override
    public List<Order> findByUserId(Integer userId) {
        return orderRepository.findAllByUserId(userId);
    }

    @Override
    public Page<Order> getOrdersByKeyword(String keyword, Pageable pageable) {
        return orderRepository.findAllByKeyword(keyword, pageable);
    }
}
