package com.project.monu.domain.interest.dto.request;

public enum InterestSortType {
    SUBSCRIBER_COUNT,
    NAME;

    private static final String ORDER_BY_NAME = "name";
    private static final String ORDER_BY_SUBSCRIBER_COUNT = "subscriberCount";

    // API 명세의 orderBy 파라미터는 "name"/"subscriberCount" 소문자 값을 사용하므로
    // enum 상수(NAME/SUBSCRIBER_COUNT)와 이름이 달라 별도 변환이 필요합니다.
    public static InterestSortType from(String orderBy) {
        if (ORDER_BY_NAME.equals(orderBy)) {
            return NAME;
        }
        if (ORDER_BY_SUBSCRIBER_COUNT.equals(orderBy)) {
            return SUBSCRIBER_COUNT;
        }
        throw new IllegalArgumentException(
                "orderBy must be '" + ORDER_BY_NAME + "' or '" + ORDER_BY_SUBSCRIBER_COUNT + "'."
        );
    }
}
