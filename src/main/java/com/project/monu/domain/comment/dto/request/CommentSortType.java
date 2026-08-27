package com.project.monu.domain.comment.dto.request;

import com.project.monu.domain.comment.exception.InvalidCommentSortTypeException;

public enum CommentSortType {

    CREATED_AT("createdAt"),
    LIKE_COUNT("likeCount");

    private final String value;

    CommentSortType(String value) {
        this.value = value;
    }

    public static CommentSortType from(String value) {
        for (CommentSortType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }

        throw new InvalidCommentSortTypeException();
    }
}