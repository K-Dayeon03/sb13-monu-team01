package com.project.monu.domain.users.service;

import com.project.monu.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPhysicalDeleteScheduler {

  private final UserRepository userRepository;
  private final UserPhysicalDeleteService userPhysicalDeleteService;

  public void deleteExpiredUsers() {
  }
}