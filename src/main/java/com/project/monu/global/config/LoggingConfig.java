package com.project.monu.global.config;

import com.project.monu.global.logging.LogArchiveProperties;
import com.project.monu.global.logging.LogArchivePath;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LogArchiveProperties.class)
public class LoggingConfig {

    @Bean
    public LogArchivePath logArchivePath(LogArchiveProperties properties) {
        return new LogArchivePath(properties);
    }
}
