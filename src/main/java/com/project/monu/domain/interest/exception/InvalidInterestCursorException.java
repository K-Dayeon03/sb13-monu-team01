package com.project.monu.domain.interest.exception;

import com.project.monu.global.exception.BusinessException;
import com.project.monu.global.exception.ErrorCode;

public class InvalidInterestCursorException extends BusinessException {

    public InvalidInterestCursorException() {
        super(ErrorCode.INVALID_INTEREST_CURSOR);
    }
}
