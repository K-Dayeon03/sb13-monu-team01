package com.project.monu.domain.users.repository;

import com.project.monu.domain.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
  boolean existsByEmail(String email);

  Optional<User> findByEmail(String email);

  boolean existsByIdAndDeletedAtIsNull(UUID id);
}