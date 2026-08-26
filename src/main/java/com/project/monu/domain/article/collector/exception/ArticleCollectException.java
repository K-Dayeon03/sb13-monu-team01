package com.project.monu.domain.article.collector.exception;

public class ArticleCollectException extends RuntimeException {

    public ArticleCollectException(String message) {
        super(message);
    }

    public ArticleCollectException(String message, Throwable cause) {
        super(message, cause);
    }
}
