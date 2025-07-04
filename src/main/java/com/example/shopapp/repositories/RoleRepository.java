package com.example.shopapp.repositories;

import com.example.shopapp.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Integer>
{
    Optional<Role> findByName(String name);
}

