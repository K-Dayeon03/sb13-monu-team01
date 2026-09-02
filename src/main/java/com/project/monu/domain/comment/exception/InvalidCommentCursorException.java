package com.project.monu.domain.comment.exception;

import com.project.monu.global.exception.BusinessException;
import com.project.monu.global.exception.ErrorCode;

public class InvalidCommentCursorException extends BusinessException {

    public InvalidCommentCursorException() {
        super(ErrorCode.COMMENT_INVALID_CURSOR);
    }
}