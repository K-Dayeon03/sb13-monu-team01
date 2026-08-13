package com.project.monu.domain.users.entity;

import com.project.monu.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class User extends BaseEntity {

  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @Column(nullable = false, length = 50)
  private String nickname;

  @Column(nullable = false, length = 255)
  private String password;
}