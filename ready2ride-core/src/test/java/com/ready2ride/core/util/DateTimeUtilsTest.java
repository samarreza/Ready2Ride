package com.ready2ride.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateTimeUtilsTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2026, 8, 1, 10, 0);

    @Test
    @DisplayName("Should correctly calculate started hours with minimum of 1")
    void testCalculateStartedHours() {
        assertEquals(1, DateTimeUtils.calculateStartedHours(BASE, BASE.plusMinutes(1)));
        assertEquals(1, DateTimeUtils.calculateStartedHours(BASE, BASE.plusMinutes(59)));
        assertEquals(1, DateTimeUtils.calculateStartedHours(BASE, BASE.plusMinutes(60)));
        assertEquals(2, DateTimeUtils.calculateStartedHours(BASE, BASE.plusMinutes(61)));
        assertEquals(4, DateTimeUtils.calculateStartedHours(BASE, BASE.plusHours(4)));
    }

    @Test
    @DisplayName("Should correctly calculate started days with minimum of 1")
    void testCalculateStartedDays() {
        assertEquals(1, DateTimeUtils.calculateStartedDays(BASE, BASE.plusMinutes(10)));
        assertEquals(1, DateTimeUtils.calculateStartedDays(BASE, BASE.plusHours(24)));
        assertEquals(2, DateTimeUtils.calculateStartedDays(BASE, BASE.plusHours(24).plusMinutes(1)));
        assertEquals(3, DateTimeUtils.calculateStartedDays(BASE, BASE.plusDays(3)));
    }

    @Test
    @DisplayName("Should test interval overlap logic")
    void testIntervalOverlap() {
        LocalDateTime t1 = BASE;
        LocalDateTime t2 = BASE.plusHours(2);
        LocalDateTime t3 = BASE.plusHours(4);

        // Disjoint contiguous [t1, t2] and [t2, t3] -> no overlap
        assertFalse(DateTimeUtils.isOverlapping(t1, t2, t2, t3));

        // Overlapping [t1, t2.plusMinutes(30)] and [t2, t3] -> overlap
        assertTrue(DateTimeUtils.isOverlapping(t1, t2.plusMinutes(30), t2, t3));

        // Null handling
        assertFalse(DateTimeUtils.isOverlapping(null, t2, t2, t3));
    }
}
