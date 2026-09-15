package space.myhomework.android.planner;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PlannerDates {
    private static final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public static Date startOfDay(Date d) {
        Calendar c = Calendar.getInstance();

        c.setTime(d);

        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTime();
    }

    public static Date plusDays(Date d, int days) {
        Calendar c = Calendar.getInstance();

        c.setTime(d);

        c.add(Calendar.DAY_OF_MONTH, days);

        return c.getTime();
    }

    // the web client always asks the server for the week starting on monday
    public static Date mondayOf(Date d) {
        Calendar c = Calendar.getInstance();

        c.setTime(startOfDay(d));

        // don't use setFirstDayOfWeek, it depends on the locale
        // DAY_OF_WEEK is 1 (sunday) through 7 (saturday), so sunday goes back 6 days
        int daysSinceMonday = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        c.add(Calendar.DAY_OF_MONTH, -daysSinceMonday);

        return c.getTime();
    }

    public static String formatISO(Date d) {
        return isoFormat.format(d);
    }
}
