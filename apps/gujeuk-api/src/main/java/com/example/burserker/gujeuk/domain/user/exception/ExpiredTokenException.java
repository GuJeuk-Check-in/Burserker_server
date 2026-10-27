package com.example.burserker.gujeuk.domain.user.exception;

import com.example.burserker.gujeuk.global.error.exception.ErrorCode;
import com.example.burserker.gujeuk.global.error.exception.GujeukException;

public class ExpiredTokenException extends GujeukException {
  public static final GujeukException EXCEPTION = new ExpiredTokenException();

  public ExpiredTokenException() {
    super(ErrorCode.EXPIRED_TOKEN);
  }
}