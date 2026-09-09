package com.recruit.delivery.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeliveryServiceTest {

    @Test
    void allowsOnlyForwardStatusTransitions() {
        assertTrue(DeliveryService.isValidTransition(0, 1));
        assertTrue(DeliveryService.isValidTransition(0, 4));
        assertTrue(DeliveryService.isValidTransition(1, 2));
        assertTrue(DeliveryService.isValidTransition(2, 3));

        assertFalse(DeliveryService.isValidTransition(1, 0));
        assertFalse(DeliveryService.isValidTransition(2, 1));
        assertFalse(DeliveryService.isValidTransition(3, 4));
        assertFalse(DeliveryService.isValidTransition(4, 2));
        assertFalse(DeliveryService.isValidTransition(null, 1));
    }
}
