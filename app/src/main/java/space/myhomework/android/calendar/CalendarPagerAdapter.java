package space.myhomework.android.calendar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

import space.myhomework.android.CalendarFragment;
import space.myhomework.android.api.APIEvent;

public class CalendarPagerAdapter extends FragmentStateAdapter {
    private CalendarDayFragment mainDay;
    private CalendarFragment calendarFragment;

    public CalendarPagerAdapter(FragmentActivity fa, CalendarFragment f) {
        super(fa);

        calendarFragment = f;
        mainDay = new CalendarDayFragment(calendarFragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return mainDay;
        }

        return new CalendarDayFragment(calendarFragment);
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public void dismissDialogs() {
        mainDay.dismissDialogs();
    }

    public void setLoading(boolean loading) {
        mainDay.setLoading(loading);
    }

    public void setEvents(ArrayList<APIEvent> events) {
        mainDay.setEvents(events);
    }
}
