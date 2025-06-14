package com.example.shopapp.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokens")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "token", length = 255)
    String token;

    @Column(name = "refresh_token", length = 255)
    String refreshToken;

    @Column(name = "token_type", length = 50)
    String tokenType;

    @Column(name = "expiration_date", length = 50)
    LocalDateTime expirationDate;

    @Column(name = "refresh_expiration_date")
    LocalDateTime refreshExpirationDate;

    @Column(name = "is_mobile", columnDefinition = "TINYINT(1)")
    boolean isMobile;

    Boolean revoked;
    Boolean expired;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    User user;
}
