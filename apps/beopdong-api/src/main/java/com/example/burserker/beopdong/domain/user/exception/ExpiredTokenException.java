package com.example.burserker.beopdong.domain.user.exception;

import com.example.burserker.beopdong.global.error.exception.BeopdongException;
import com.example.burserker.beopdong.global.error.exception.ErrorCode;

public class ExpiredTokenException extends BeopdongException {
  public static final BeopdongException EXCEPTION = new ExpiredTokenException();

  public ExpiredTokenException() { super(ErrorCode.EXPIRED_TOKEN); }
}
