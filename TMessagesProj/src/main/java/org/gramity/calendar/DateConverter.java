package org.gramity.calendar;

import org.gramity.calendar.dates.CivilDate;
import org.gramity.calendar.dates.PersianDate;

public final class DateConverter {

    public static long civilToJdn(CivilDate civil) {
        long lYear = civil.getYear();
        long lMonth = civil.getMonth();
        long lDay = civil.getDayOfMonth();

        if ((lYear > 1582)
                || ((lYear == 1582) && (lMonth > 10))
                || ((lYear == 1582) && (lMonth == 10) && (lDay > 14))) {

            return ((1461 * (lYear + 4800 + ((lMonth - 14) / 12))) / 4)
                    + ((367 * (lMonth - 2 - 12 * (((lMonth - 14) / 12)))) / 12)
                    - ((3 * (((lYear + 4900 + ((lMonth - 14) / 12)) / 100))) / 4)
                    + lDay - 32075;
        } else
            return julianToJdn(lYear, lMonth, lDay);

    }

    public static PersianDate civilToPersian(CivilDate civil) {
        return jdnToPersian(civilToJdn(civil));
    }

    private static long floor(double d) {
        return (long) Math.floor(d);
    }

    public static CivilDate jdnToCivil(long jdn) {

        if (jdn > 2299160) {
            long l = jdn + 68569;
            long n = ((4 * l) / 146097);
            l = l - ((146097 * n + 3) / 4);
            long i = ((4000 * (l + 1)) / 1461001);
            l = l - ((1461 * i) / 4) + 31;
            long j = ((80 * l) / 2447);
            int day = (int) (l - ((2447 * j) / 80));
            l = (j / 11);
            int month = (int) (j + 2 - 12 * l);
            int year = (int) (100 * (n - 49) + i + l);
            return new CivilDate(year, month, day);
        } else
            return jdnToJulian(jdn);
    }

    // TODO Is it correct to return a CivilDate as a JulianDate?
    public static CivilDate jdnToJulian(long jdn) {
        long j = jdn + 1402;
        long k = ((j - 1) / 1461);
        long l = j - 1461 * k;
        long n = ((l - 1) / 365) - (l / 1461);
        long i = l - 365 * n + 30;
        j = ((80 * i) / 2447);
        int day = (int) (i - ((2447 * j) / 80));
        i = (j / 11);
        int month = (int) (j + 2 - 12 * i);
        int year = (int) (4 * k + n + i - 4716);

        return new CivilDate(year, month, day);
    }

    public static PersianDate jdnToPersian(long jdn) {

        long depoch = jdn - persianToJdn(475, 1, 1);
        long cycle = depoch / 1029983;
        long cyear = depoch % 1029983;
        long ycycle;
        long aux1, aux2;

        if (cyear == 1029982)
            ycycle = 2820;
        else {
            aux1 = cyear / 366;
            aux2 = cyear % 366;
            ycycle = floor(((2134 * aux1) + (2816 * aux2) + 2815) / 1028522d)
                    + aux1 + 1;
        }

        int year, month, day;
        year = (int) (ycycle + (2820 * cycle) + 474);
        if (year <= 0)
            year = year - 1;

        long yday = (jdn - persianToJdn(year, 1, 1)) + 1;
        if (yday <= 186)
            month = (int) Math.ceil(yday / 31d);
        else
            month = (int) Math.ceil((yday - 6) / 30d);

        day = (int) (jdn - persianToJdn(year, month, 1)) + 1;
        return new PersianDate(year, month, day);
    }

    public static long julianToJdn(long lYear, long lMonth, long lDay) {

        return 367 * lYear - ((7 * (lYear + 5001 + ((lMonth - 9) / 7))) / 4)
                + ((275 * lMonth) / 9) + lDay + 1729777;

    }

    public static CivilDate persianToCivil(PersianDate persian) {
        return jdnToCivil(persianToJdn(persian));
    }

    public static long persianToJdn(int year, int month, int day) {
        final long PERSIAN_EPOCH = 1948321; // The JDN of 1 Farvardin 1

        long epbase;
        if (year >= 0)
            epbase = year - 474;
        else
            epbase = year - 473;

        long epyear = 474 + (epbase % 2820);

        long mdays;
        if (month <= 7)
            mdays = (month - 1) * 31;
        else
            mdays = (month - 1) * 30 + 6;

        return day + mdays + ((epyear * 682) - 110) / 2816 + (epyear - 1) * 365
                + epbase / 2820 * 1029983 + (PERSIAN_EPOCH - 1);
    }

    public static long persianToJdn(PersianDate persian) {
        int year = persian.getYear();
        int month = persian.getMonth();
        int day = persian.getDayOfMonth();

        final long PERSIAN_EPOCH = 1948321; // The JDN of 1 Farvardin 1

        long epbase;
        if (year >= 0)
            epbase = year - 474;
        else
            epbase = year - 473;

        long epyear = 474 + (epbase % 2820);

        long mdays;
        if (month <= 7) //def <= 7
            mdays = (month - 1) * 31; //def (month - 1)
        else
            mdays = (month - 1) * 30 + 6; //def (month - 1)

        return day + mdays + ((epyear * 682) - 110) / 2816 + (epyear - 1) * 365
                + epbase / 2820 * 1029983 + (PERSIAN_EPOCH - 1);
    }
}
