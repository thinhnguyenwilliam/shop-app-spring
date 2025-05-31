package com.example.shopapp.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "name", nullable = false, length = 350)
    String name;

    Float price;
    //BigDecimal price;

    @Column(name = "thumbnail", length = 300)
    String thumbnail;

    @Column(name = "description")
    String description;

    @Column(name = "slug", nullable = false, unique = true, length = 100)
    String slug;

//    @CreationTimestamp
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    private LocalDateTime updatedAt;


    // Many products belong to one category
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    Category category;

    @OneToMany(mappedBy = "product",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<ProductImage> productImages;

}
