package com.ween.exception;

public class SubscriptionLimitException extends RuntimeException {
    public SubscriptionLimitException(String message) {
        super(message);
    }

    public SubscriptionLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
