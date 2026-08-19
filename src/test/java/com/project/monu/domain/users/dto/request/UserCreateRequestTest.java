package com.project.monu.domain.users.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class UserCreateRequestTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  // nickname test
  @Test
  void 닉네임이_없으면_검증에_실패한다() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    UserCreateRequest request = new UserCreateRequest(
        "test@test.com",
        "",
        "password123!",
        "password123!"
    );

    Set<ConstraintViolation<UserCreateRequest>> violations =
        validator.validate(request);

    assertThat(violations).isNotEmpty();
  }

  @Test
  void 닉네임이_10자를_초과하면_검증에_실패한다() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    UserCreateRequest request = new UserCreateRequest(
        "test@test.com",
        "12345678901",
        "password123!",
        "password123!"
    );

    Set<ConstraintViolation<UserCreateRequest>> violations =
        validator.validate(request);

    assertThat(violations).isNotEmpty();
  }

  // email test
  @Test
  void 잘못된_이메일_형식은_검증에_실패한다() {
    UserCreateRequest request = new UserCreateRequest(
        "잘못된이메일",
        "테스트",
        "password123!",
        "password123!"
    );

    var violations = validator.validate(request);

    assertThat(violations).isNotEmpty();
  }

  // password test
  @Test
  void 비밀번호가_없으면_검증에_실패한다() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    UserCreateRequest request = new UserCreateRequest(
        "test@test.com",
        "테스트",
        "",
        ""
    );

    Set<ConstraintViolation<UserCreateRequest>> violations =
        validator.validate(request);

    assertThat(violations).isNotEmpty();
  }

  @Test
  void 비밀번호가_6자_미만이면_검증에_실패한다() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    UserCreateRequest request = new UserCreateRequest(
        "test@test.com",
        "테스트",
        "Ab1!2",
        "Ab1!2"
    );

    Set<ConstraintViolation<UserCreateRequest>> violations =
        validator.validate(request);

    assertThat(violations).isNotEmpty();
  }

  @Test
  void 비밀번호가_정확히_6자이면_검증에_성공한다() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    UserCreateRequest request = new UserCreateRequest(
        "test@test.com",
        "테스트",
        "Ab1!23",
        "Ab1!23"
    );

    Set<ConstraintViolation<UserCreateRequest>> violations =
        validator.validate(request);

    assertThat(violations).isEmpty();
  }

  @Test
  void 비밀번호가_20자를_초과하면_검증에_실패한다() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    UserCreateRequest request = new UserCreateRequest(
        "test@test.com",
        "테스트",
        "Abcdef1234567890!@#$%",
        "Abcdef1234567890!@#$%"
    );

    Set<ConstraintViolation<UserCreateRequest>> violations =
        validator.validate(request);

    assertThat(violations).isNotEmpty();
  }

  @Test
  void 비밀번호가_정확히_20자이면_검증에_성공한다() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    UserCreateRequest request = new UserCreateRequest(
        "test@test.com",
        "테스트",
        "Abcdef1234567890!@#$",
        "Abcdef1234567890!@#$"
    );

    Set<ConstraintViolation<UserCreateRequest>> violations =
        validator.validate(request);

    assertThat(violations).isEmpty();
  }

  @ParameterizedTest
  @CsvSource({
      "'abcdef!', '숫자가 없다'",
      "'123456!', '영문이 없다'",
      "'Abc1234', '특수문자가 없다'"
  })
  void 영문_숫자_특수문자가_모두_포함되지_않으면_검증에_실패한다(
      String password,
      String description
  ) {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    UserCreateRequest request = new UserCreateRequest(
        "test@test.com",
        "테스트",
        password,
        password
    );

    Set<ConstraintViolation<UserCreateRequest>> violations =
        validator.validate(request);

    assertThat(violations)
        .as(description)
        .isNotEmpty();
  }

  @Test
  void 영문_숫자_특수문자가_모두_포함되면_검증에_성공한다() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    UserCreateRequest request = new UserCreateRequest(
        "test@test.com",
        "테스트",
        "Abc123!",
        "Abc123!"
    );

    Set<ConstraintViolation<UserCreateRequest>> violations =
        validator.validate(request);

    assertThat(violations).isEmpty();
  }

  // password confirm test
  @Test
  void 비밀번호_확인이_없으면_검증에_실패한다() {
    UserCreateRequest request = new UserCreateRequest(
        "test@test.com",
        "테스트",
        "Abc123!",
        ""
    );

    Set<ConstraintViolation<UserCreateRequest>> violations =
        validator.validate(request);

    assertThat(violations).isNotEmpty();
  }

  @Test
  void 비밀번호와_비밀번호_확인이_다르면_검증에_실패한다() {
    UserCreateRequest request = new UserCreateRequest(
        "test@test.com",
        "테스트",
        "Abc123!",
        "Abc124!"
    );

    Set<ConstraintViolation<UserCreateRequest>> violations =
        validator.validate(request);

    assertThat(violations).isNotEmpty();
  }

  @Test
  void 비밀번호와_비밀번호_확인이_같으면_검증에_성공한다() {
    UserCreateRequest request = new UserCreateRequest(
        "test@test.com",
        "테스트",
        "Abc123!",
        "Abc123!"
    );

    Set<ConstraintViolation<UserCreateRequest>> violations =
        validator.validate(request);

    assertThat(violations).isEmpty();
  }
}