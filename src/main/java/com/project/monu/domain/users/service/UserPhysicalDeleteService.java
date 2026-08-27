package com.project.monu.domain.users.service;

import com.project.monu.domain.article.repository.ArticleViewRepository;
import com.project.monu.domain.users.repository.UserRepository;

import java.util.UUID;

public class UserPhysicalDeleteService {

  private final UserRepository userRepository;
  private final ArticleViewRepository articleViewRepository;

  public UserPhysicalDeleteService(
      UserRepository userRepository,
      ArticleViewRepository articleViewRepository
  ) {
    this.userRepository = userRepository;
    this.articleViewRepository = articleViewRepository;
  }

  public void hardDelete(UUID userId) {
  }
}