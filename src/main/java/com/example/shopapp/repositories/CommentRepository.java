package com.example.shopapp.repositories;


import com.example.shopapp.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByUserIdAndProductId(@Param("userId") Integer userId,
                                           @Param("productId") Integer productId);


    List<Comment> findByProductId(@Param("productId") Integer productId);
}