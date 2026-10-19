package com.ready2ride.core.util;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Utility helper methods for rental date and duration calculations.
 */
public final class DateTimeUtils {

    public static final long MINUTES_PER_HOUR = 60L;
    public static final long MINUTES_PER_DAY = 1440L;

    private DateTimeUtils() {
        // Private constructor to prevent instantiation of utility class
    }

    /**
     * Calculates the number of started hours between two date-times (ceiling division).
     * Enforces a minimum of 1 billable hour for any positive duration.
     *
     * @param start start date/time
     * @param end   end date/time
     * @return billable started hours (at least 1)
     */
    public static long calculateStartedHours(LocalDateTime start, LocalDateTime end) {
        long totalMinutes = Duration.between(start, end).toMinutes();
        long hours = (totalMinutes + (MINUTES_PER_HOUR - 1)) / MINUTES_PER_HOUR;
        return Math.max(1, hours);
    }

    /**
     * Calculates the number of started 24-hour periods (days) between two date-times (ceiling division).
     * Enforces a minimum of 1 billable day for any positive duration.
     *
     * @param start start date/time
     * @param end   end date/time
     * @return billable started days (at least 1)
     */
    public static long calculateStartedDays(LocalDateTime start, LocalDateTime end) {
        long totalMinutes = Duration.between(start, end).toMinutes();
        long days = (totalMinutes + (MINUTES_PER_DAY - 1)) / MINUTES_PER_DAY;
        return Math.max(1, days);
    }

    /**
     * Checks if two closed-open / open intervals overlap according to:
     * {@code requestedStart < existingEnd AND requestedEnd > existingStart}
     *
     * @param start1 start of first interval
     * @param end1   end of first interval
     * @param start2 start of second interval
     * @param end2   end of second interval
     * @return true if intervals overlap
     */
    public static boolean isOverlapping(LocalDateTime start1, LocalDateTime end1,
                                         LocalDateTime start2, LocalDateTime end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }
        return start1.isBefore(end2) && end1.isAfter(start2);
    }
}
