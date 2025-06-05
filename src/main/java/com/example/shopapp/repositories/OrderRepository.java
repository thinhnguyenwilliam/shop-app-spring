package com.example.shopapp.repositories;

import com.example.shopapp.models.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findAllByUserId(Integer userId);

    @Query("SELECT o FROM Order o WHERE o.active = true AND " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(o.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(o.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(o.note) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(o.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Order> findAllByKeyword(@Param("keyword") String keyword, Pageable pageable);

}
