package space.myhomework.android.planner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

// one page per day, forever
// rather than shuffling pages around to fake infinite scrolling, every position maps to a fixed
// date: position 0 is EPOCH_MONDAY and each position after that is one day later
public class PlannerPagerAdapter extends FragmentStateAdapter {
    // about a hundred years of days, which is plenty
    public static final int ITEM_COUNT = 36_500;

    private static final Date EPOCH_MONDAY;

    static {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(2000, Calendar.JANUARY, 3);
        EPOCH_MONDAY = c.getTime();
    }

    public PlannerPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    public static Date positionToDate(int position) {
        return PlannerDates.plusDays(EPOCH_MONDAY, position);
    }

    public static int dateToPosition(Date date) {
        long millis = PlannerDates.startOfDay(date).getTime() - EPOCH_MONDAY.getTime();
        // DST means a day isn't always 24 hours, so round instead of truncating
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        return (int) Math.round(hours / 24.0);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return PlannerDayFragment.newInstance(positionToDate(position));
    }

    @Override
    public int getItemCount() {
        return ITEM_COUNT;
    }
}
