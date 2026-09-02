package com.project.monu.domain.users.service;

import com.project.monu.domain.users.entity.User;
import com.project.monu.domain.users.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPhysicalDeleteScheduler {

  private final UserRepository userRepository;
  private final UserPhysicalDeleteService userPhysicalDeleteService;

  public void deleteExpiredUsers() {
    Instant cutoff = Instant.now().minus(24, ChronoUnit.HOURS);

    List<User> users = userRepository.findAllByDeletedAtBefore(cutoff);

    for (User user : users) {
      userPhysicalDeleteService.hardDelete(user.getId());
    }
  }
}