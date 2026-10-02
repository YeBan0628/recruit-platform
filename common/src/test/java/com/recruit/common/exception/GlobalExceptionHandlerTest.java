package com.recruit.common.exception;

import com.recruit.common.result.Result;
import com.recruit.common.result.ResultCode;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlesBusinessExceptionWithCustomCode() {
        Result<Void> result = handler.handleBusinessException(
                new BusinessException(ResultCode.FORBIDDEN));

        assertEquals(ResultCode.FORBIDDEN.getCode(), result.getCode());
        assertEquals(ResultCode.FORBIDDEN.getMessage(), result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void handlesBusinessExceptionWithMessageOnly() {
        Result<Void> result = handler.handleBusinessException(
                new BusinessException("验证码错误或已过期"));

        assertEquals(ResultCode.ERROR.getCode(), result.getCode());
        assertEquals("验证码错误或已过期", result.getMessage());
    }

    @Test
    void handlesValidationExceptionWithFieldError() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldError())
                .thenReturn(new FieldError("request", "title", "职位名称不能为空"));

        Result<Void> result = handler.handleMethodArgumentNotValid(exception);

        assertEquals(ResultCode.BAD_REQUEST.getCode(), result.getCode());
        assertEquals("职位名称不能为空", result.getMessage());
    }

    @Test
    void handlesValidationExceptionWithoutFieldError() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldError()).thenReturn(null);

        Result<Void> result = handler.handleMethodArgumentNotValid(exception);

        assertEquals(ResultCode.BAD_REQUEST.getCode(), result.getCode());
        assertEquals(ResultCode.BAD_REQUEST.getMessage(), result.getMessage());
    }

    @Test
    void handlesUnexpectedExceptionAsSystemError() {
        Result<Void> result = handler.handleException(new IllegalStateException("boom"));

        assertEquals(ResultCode.ERROR.getCode(), result.getCode());
        assertEquals(ResultCode.ERROR.getMessage(), result.getMessage());
    }
}
