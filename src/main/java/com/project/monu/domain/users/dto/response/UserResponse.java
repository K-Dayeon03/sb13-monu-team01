package com.project.monu.domain.users.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * 사용자 정보 응답 DTO
 * 회원가입, 로그인, 사용자 정보 수정 성공 모두 response가 같기에 하나로 합쳐뒀습니다.
 * 비밀번호는 보안상 응답에 포함하지 않습니다.
 *
 * 사용 API:
 * - POST /api/users : 회원가입 성공
 * - POST /api/users/login : 로그인 성공
 * - PATCH /api/users/{userId} : 사용자 정보 수정 성공
 */
public record UserResponse(

    UUID id,
    String email,
    String nickname,
    Instant createdAt
) {
}