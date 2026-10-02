package com.recruit.common.result;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResultTest {

    @Test
    void successCarriesData() {
        Result<Long> result = Result.success(100L);

        assertEquals(ResultCode.SUCCESS.getCode(), result.getCode());
        assertEquals(ResultCode.SUCCESS.getMessage(), result.getMessage());
        assertEquals(100L, result.getData());
    }

    @Test
    void successWithoutDataIsNull() {
        Result<Void> result = Result.success();

        assertEquals(200, result.getCode());
        assertNull(result.getData());
    }

    @Test
    void failWithCodeAndMessage() {
        Result<Void> result = Result.fail(400, "参数错误");

        assertEquals(400, result.getCode());
        assertEquals("参数错误", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void failWithResultCode() {
        Result<Void> result = Result.fail(ResultCode.NOT_FOUND);

        assertEquals(ResultCode.NOT_FOUND.getCode(), result.getCode());
        assertEquals(ResultCode.NOT_FOUND.getMessage(), result.getMessage());
    }
}
