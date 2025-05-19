package com.example.shopapp.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "product_images")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductImage {
    public static final int MAXIMUM_IMAGES_PER_PRODUCT = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "image_url", length = 300)
    String imageUrl;

    @ManyToOne
    @JoinColumn(name = "product_id")
    Product product;
}
