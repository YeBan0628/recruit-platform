package com.recruit.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {

    @Test
    void encodesToBcryptHash() {
        String encoded = PasswordUtil.encode("123456");

        assertTrue(encoded.startsWith("$2a$"));
        assertNotEquals("123456", encoded);
    }

    @Test
    void generatesDifferentSaltEachTime() {
        String first = PasswordUtil.encode("123456");
        String second = PasswordUtil.encode("123456");

        assertNotEquals(first, second);
        assertTrue(PasswordUtil.matches("123456", first));
        assertTrue(PasswordUtil.matches("123456", second));
    }

    @Test
    void rejectsWrongPassword() {
        String encoded = PasswordUtil.encode("123456");

        assertFalse(PasswordUtil.matches("654321", encoded));
        assertFalse(PasswordUtil.matches("", encoded));
    }

    @Test
    void rejectsInvalidEncodedPassword() {
        assertFalse(PasswordUtil.matches("123456", "not-a-bcrypt-hash"));
    }
}
