package com.project.monu.domain.interest.exception;

public class InterestDuplicateException extends RuntimeException {

    public InterestDuplicateException(String name) {
        super("이미 유사한 관심사가 존재합니다: " + name);
    }
}