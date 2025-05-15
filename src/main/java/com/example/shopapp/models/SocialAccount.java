package com.example.shopapp.models;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "social_accounts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SocialAccount
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "provider", length = 100, nullable = false)
    String provider;

    @Column(name = "provider_id", length = 50, nullable = false)
    String providerId;

    @Column(name = "name", length = 150)
    String name;

    @Column(name = "email", length = 150)
    String email;
}
