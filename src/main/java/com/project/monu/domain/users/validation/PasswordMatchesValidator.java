package com.project.monu.domain.users.validation;

import com.project.monu.domain.users.dto.request.UserCreateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator
    implements ConstraintValidator<PasswordMatches, UserCreateRequest> {

  @Override
  public boolean isValid(
      UserCreateRequest request,
      ConstraintValidatorContext context
  ) {
    if (request == null) {
      return true;
    }

    if (request.password() == null || request.passwordConfirm() == null) {
      return false;
    }

    return request.password().equals(request.passwordConfirm());
  }
}