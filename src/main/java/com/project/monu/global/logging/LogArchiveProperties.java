package com.project.monu.global.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "log.archive")
public record LogArchiveProperties(
        String storage,
        String sourceDir,
        String localDestinationDir,
        String s3Prefix,
        String cron,
        String zone,
        Boolean manualEnabled
) {

    public LogArchiveProperties {
        storage = defaultIfBlank(storage, "local");
        sourceDir = defaultIfBlank(sourceDir, "logs");
        localDestinationDir = defaultIfBlank(localDestinationDir, "build/log-archives");
        s3Prefix = defaultIfBlank(s3Prefix, "logs");
        cron = defaultIfBlank(cron, "0 30 2 * * *");
        zone = defaultIfBlank(zone, "Asia/Seoul");
        manualEnabled = manualEnabled != null && manualEnabled;
    }

    private static String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
