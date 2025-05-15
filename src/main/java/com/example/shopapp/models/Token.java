package com.example.shopapp.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokens")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "token", length = 255)
    String token;

    @Column(name = "token_type", length = 50)
    String tokenType;

    @Column(name = "expiration_date", length = 50)
    LocalDateTime expirationDate;

    Boolean revoked;
    Boolean expired;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;
}
