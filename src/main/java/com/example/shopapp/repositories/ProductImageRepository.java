package com.example.shopapp.repositories;

import com.example.shopapp.models.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage,Integer>{
    List<ProductImage> findAllByProductId(Integer productId);

}
