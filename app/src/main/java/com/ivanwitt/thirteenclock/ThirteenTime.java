package com.ivanwitt.thirteenclock;

import java.time.ZonedDateTime;
import java.util.Locale;

public final class ThirteenTime {
    private ThirteenTime() {}

    public static Result from(ZonedDateTime now) {
        double seconds = now.getHour() * 3600.0
                + now.getMinute() * 60.0
                + now.getSecond()
                + now.getNano() / 1_000_000_000.0;

        double dayFraction = seconds / 86400.0;
        double totalAltMinutes = dayFraction * 13.0 * 20.0;
        int hourIndex = (int) Math.floor(totalAltMinutes / 20.0);
        double minuteValue = totalAltMinutes - hourIndex * 20.0;
        int minute = (int) Math.floor(minuteValue);

        int displayHour = hourIndex == 0 ? 13 : hourIndex;
        double hourAngle = dayFraction * 360.0;
        double minuteAngle = (minuteValue / 20.0) * 360.0;

        return new Result(hourIndex, displayHour, minute, minuteValue, hourAngle, minuteAngle);
    }

    public static final class Result {
        public final int hourIndex;
        public final int displayHour;
        public final int minute;
        public final double minuteValue;
        public final double hourAngle;
        public final double minuteAngle;

        Result(int hourIndex, int displayHour, int minute, double minuteValue,
               double hourAngle, double minuteAngle) {
            this.hourIndex = hourIndex;
            this.displayHour = displayHour;
            this.minute = minute;
            this.minuteValue = minuteValue;
            this.hourAngle = hourAngle;
            this.minuteAngle = minuteAngle;
        }

        public String digital() {
            return String.format(Locale.US, "%02d:%02d", displayHour, minute);
        }
    }
}
