package com.project.monu.domain.comment.exception;

import com.project.monu.global.exception.BusinessException;
import com.project.monu.global.exception.ErrorCode;

public class InvalidCommentSortTypeException extends BusinessException {

    public InvalidCommentSortTypeException() {
        super(ErrorCode.COMMENT_INVALID_SORT_TYPE);
    }
}