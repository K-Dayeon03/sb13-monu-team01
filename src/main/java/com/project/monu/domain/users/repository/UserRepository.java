package com.project.monu.domain.users.repository;

import com.project.monu.domain.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

  boolean existsByEmail(String email);

  Optional<User> findByEmail(String email);

  Optional<User> findByEmailAndDeletedAtIsNull(String email);

  boolean existsByIdAndDeletedAtIsNull(UUID id);

  Optional<User> findByIdAndDeletedAtIsNull(UUID id);

  List<User> findAllByDeletedAtLessThanEqual(Instant cutoff);
}