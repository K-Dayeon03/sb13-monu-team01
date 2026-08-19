package com.project.monu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MonuApplication {
  public static void main(String[] args) {
    SpringApplication.run(MonuApplication.class, args);
  }
}