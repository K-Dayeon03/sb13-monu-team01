package com.project.monu.domain.users.entity;

import com.project.monu.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {

  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @Column(nullable = false, length = 50)
  private String nickname;

  @Column(nullable = false, length = 255)
  private String password;

  @Column
  private Instant deletedAt;

  @Builder
  public User(String email, String nickname, String password) {
    this.email = email;
    this.nickname = nickname;
    this.password = password;
  }

  public void updateNickname(String nickname) {
    this.nickname = nickname;
  }
}