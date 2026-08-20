package com.project.monu.domain.users.repository;

import com.project.monu.domain.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
  boolean existsByEmail(String email);
}