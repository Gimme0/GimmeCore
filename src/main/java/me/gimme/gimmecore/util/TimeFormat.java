package me.gimme.gimmecore.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeFormat {

    /**
     * Format in order:
     * hhh:mm:ss
     * h::mm:ss
     * mm:ss
     *
     * @param seconds the time in seconds to format
     * @return the formatted time
     */
    public static String digitalTime(long seconds) {
        long h = TimeUnit.SECONDS.toHours(seconds);
        return h > 0 ? h + ":" : "" + String.format("%02d:%02d",
                TimeUnit.SECONDS.toMinutes(seconds) % TimeUnit.HOURS.toMinutes(1),
                seconds % TimeUnit.MINUTES.toSeconds(1));
    }

    /**
     * Format in order:
     * hhh:mm:ss
     * h::mm:ss
     * m:ss
     * s
     *
     * @param seconds the time in seconds to format
     * @return the formatted time
     */
    public static String digitalTimeMinimalized(long seconds) {
        long h = TimeUnit.SECONDS.toHours(seconds);
        long m = TimeUnit.SECONDS.toMinutes(seconds) % TimeUnit.HOURS.toMinutes(1);
        long s = seconds % TimeUnit.MINUTES.toSeconds(1);

        if (h > 0) {
            return h + ":" + String.format("%02d:%02d", m, s);
        } else if (m > 0) {
            return m + ":" + String.format("%02d", s);
        } else {
            return String.valueOf(seconds);
        }
    }

    /**
     * Format in order:
     * hhh:mm:ss
     * h::mm:ss
     * m:ss
     * s.xx
     *
     * @param seconds the time in seconds to format
     * @param decimals the amount of decimals to display
     * @return the formatted time
     * @throws IllegalArgumentException if {@code decimals} is less than one
     */
    public static String digitalTimeMinimalizedThenSecondsWithDecimals(double seconds, int decimals) {
        if (decimals < 1) throw new IllegalArgumentException("Amount of decimals cannot be less than one");

        if (seconds > 60) return digitalTimeMinimalized((long) seconds);

        DecimalFormat df = new DecimalFormat("#." + "#".repeat(decimals),
                new DecimalFormatSymbols(Locale.ENGLISH));
        df.setMaximumFractionDigits(decimals);
        df.setMinimumFractionDigits(decimals);
        df.setRoundingMode(RoundingMode.HALF_UP);

        return df.format(seconds);
    }

    /**
     * Format in order:
     * h hours m minutes s seconds
     * m minutes s seconds
     * s seconds
     *
     * @param seconds the time in seconds to format
     * @return the formatted time
     */
    public static String wordsTime(long seconds) {
        long h = TimeUnit.SECONDS.toHours(seconds);
        long m = TimeUnit.SECONDS.toMinutes(seconds) % TimeUnit.HOURS.toMinutes(1);
        long s = seconds % TimeUnit.MINUTES.toSeconds(1);
        return (h > 0 ? h + " hours " : "") +
                (h > 0 || m > 0 ? m + " minutes " : "") +
                s + " seconds";
    }

}
