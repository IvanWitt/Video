package com.ivanwitt.thirteenclock;

import java.time.LocalDate;

public final class MayaCalendar {
    public static final long GMT_CORRELATION = 584283L;

    private static final String[] TZOLKIN_NAMES = {
            "Imix", "Ik’", "Ak’bal", "K’an", "Chikchan",
            "Kimi", "Manik’", "Lamat", "Muluk", "Ok",
            "Chuwen", "Eb’", "B’en", "Ix", "Men",
            "K’ib’", "Kab’an", "Etz’nab’", "Kawak", "Ajaw"
    };

    private static final String[] HAAB_MONTHS = {
            "Pop", "Wo", "Sip", "Sotz’", "Sek", "Xul",
            "Yaxk’in", "Mol", "Ch’en", "Yax", "Sak’", "Keh",
            "Mak", "K’ank’in", "Muwan’", "Pax", "K’ayab", "Kumk’u",
            "Wayeb"
    };

    private MayaCalendar() {}

    public static Result forDate(LocalDate date) {
        long jdn = gregorianToJdn(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        long days = jdn - GMT_CORRELATION;

        int tzolkinNumber = floorMod(days + 3, 13) + 1;
        int tzolkinNameIndex = floorMod(days + 19, 20);
        String tzolkin = tzolkinNumber + " " + TZOLKIN_NAMES[tzolkinNameIndex];

        int haabIndex = floorMod(days + 348, 365);
        String haab;
        if (haabIndex < 360) {
            int month = haabIndex / 20;
            int day = haabIndex % 20;
            haab = day + " " + HAAB_MONTHS[month];
        } else {
            haab = (haabIndex - 360) + " " + HAAB_MONTHS[18];
        }

        long value = days;
        long baktun = Math.floorDiv(value, 144000L);
        long rem = Math.floorMod(value, 144000L);
        long katun = rem / 7200L;
        rem %= 7200L;
        long tun = rem / 360L;
        rem %= 360L;
        long uinal = rem / 20L;
        long kin = rem % 20L;

        String longCount = baktun + "." + katun + "." + tun + "." + uinal + "." + kin;
        return new Result(tzolkin, haab, longCount, days);
    }

    static long gregorianToJdn(int year, int month, int day) {
        int a = (14 - month) / 12;
        long y = (long) year + 4800L - a;
        int m = month + 12 * a - 3;
        return day
                + (153L * m + 2L) / 5L
                + 365L * y
                + y / 4L
                - y / 100L
                + y / 400L
                - 32045L;
    }

    private static int floorMod(long value, int modulus) {
        return (int) Math.floorMod(value, (long) modulus);
    }

    public static final class Result {
        public final String tzolkin;
        public final String haab;
        public final String longCount;
        public final long daysFromCreation;

        Result(String tzolkin, String haab, String longCount, long daysFromCreation) {
            this.tzolkin = tzolkin;
            this.haab = haab;
            this.longCount = longCount;
            this.daysFromCreation = daysFromCreation;
        }

        public String calendarRoundLine() {
            return tzolkin + "  +  " + haab;
        }
    }
}
