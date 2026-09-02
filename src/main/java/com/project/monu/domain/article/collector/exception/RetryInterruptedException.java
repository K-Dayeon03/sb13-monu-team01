package com.project.monu.domain.article.collector.exception;

public class RetryInterruptedException extends RuntimeException {
    public RetryInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}
