package com.project.monu.domain.article.collector.exception;

public class NaverApiException extends ArticleCollectException{

    private final int statusCode;
    private final String errorCode;
    private final boolean retryable;

    public NaverApiException(int statusCode, String errorCode, String message,
                             boolean retryable, Throwable cause) {
        super(message, cause);

        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.retryable = retryable;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
