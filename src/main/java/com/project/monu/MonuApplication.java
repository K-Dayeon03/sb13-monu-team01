package com.project.monu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MonuApplication {
  public static void main(String[] args) {
    SpringApplication.run(MonuApplication.class, args);
  }
}