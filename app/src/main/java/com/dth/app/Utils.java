package com.dth.app;

import java.util.concurrent.TimeUnit;

public class Utils {
    public static String timeMillisToString(long timeLeftMs) {
        long days = TimeUnit.MILLISECONDS.toDays(timeLeftMs);
        timeLeftMs -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(timeLeftMs);
        timeLeftMs -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeftMs);
        timeLeftMs -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeftMs);
        if (days > 0) {
            return days + " days";
        }
        if (hours > 0) {
            return hours + " hours";
        }
        if (minutes > 0) {
            return minutes + " minutes";
        }
        return seconds + " seconds";
    }
}
