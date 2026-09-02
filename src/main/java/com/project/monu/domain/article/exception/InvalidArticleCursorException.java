package com.project.monu.domain.article.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 기사 목록 조회에서 사용할 수 없는 커서가 들어왔을 때 발생하는 예외입니다.
 *
 * 잘못된 커서를 무시하면 같은 첫 페이지가 반복 조회될 수 있으므로,
 * 클라이언트가 요청 값을 고칠 수 있도록 400 Bad Request로 응답합니다.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidArticleCursorException extends RuntimeException {

    public InvalidArticleCursorException(String message) {
        super(message);
    }
}
