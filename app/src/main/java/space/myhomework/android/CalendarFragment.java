package space.myhomework.android;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import space.myhomework.android.api.APIClient;
import space.myhomework.android.api.APIEvent;
import space.myhomework.android.calendar.CalendarPagerAdapter;
import space.myhomework.android.calendar.EventAdapter;
import space.myhomework.android.databinding.FragmentCalendarBinding;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;

    private SimpleDateFormat iso8601DateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private Date activeDay;

    private ArrayList<APIEvent> events = new ArrayList<>();

    private CalendarPagerAdapter pagerAdapter;

    public CalendarFragment() {

    }

    public void loadDay() {
        pagerAdapter.setLoading(true);

        HashMap<String, String> params = new HashMap<>();
        params.put("start", iso8601DateFormat.format(activeDay));
        params.put("end", iso8601DateFormat.format(calculateEnd()));
        APIClient.getInstance(getContext(), null).makeRequest(Request.Method.GET, "calendar/getView", params, new Response.Listener<JSONObject>() {
            // TODO: lint doesn't know about desugaring for .sort()?
            // TODO: test this on android 7
            @SuppressLint("NewApi")
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray days = response.getJSONObject("view").getJSONArray("days");
                    JSONObject day = days.getJSONObject(0);
                    JSONArray eventsJSONArray = day.getJSONArray("events");

                    events.clear();
                    for (int i = 0; i < eventsJSONArray.length(); i++) {
                        events.add(new APIEvent(eventsJSONArray.getJSONObject(i)));
                    }

                    events.sort(new Comparator<APIEvent>() {
                        @Override
                        public int compare(APIEvent o1, APIEvent o2) {
                            return o1.Start - o2.Start;
                        }
                    });

                    pagerAdapter.setEvents(events);
                    pagerAdapter.setLoading(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, ((MainActivity)getActivity()).abandonHandler);
    }

    public void dismissDialogs() {
        pagerAdapter.dismissDialogs();
    }

    public void setDate(Date d) {
        Calendar c = Calendar.getInstance();

        c.setTime(d);

        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        activeDay = c.getTime();
    }

    public void setDateByOffset(int dayOffset) {
        Calendar c = Calendar.getInstance();

        c.setTime(activeDay);

        c.add(Calendar.DAY_OF_MONTH, dayOffset);

        activeDay = c.getTime();
    }

    private Date calculateEnd() {
        Calendar c = Calendar.getInstance();

        c.setTime(activeDay);

        c.add(Calendar.DAY_OF_MONTH, 1);

        return c.getTime();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);

        pagerAdapter = new CalendarPagerAdapter(getActivity(), this);
        binding.calendarPager.setAdapter(pagerAdapter);
        binding.calendarPager.setCurrentItem(1, false);

        // HACK: this is how we maintain the illusion of infinite scroll
        // we let the user scroll from page 1 to either 0 or 2, then reset it back to page 1 without animation
        // (if you scroll fast this kinda falls apart - but doing it properly seems complicated)
        binding.calendarPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    int curr = binding.calendarPager.getCurrentItem();

                    // first check that we actually moved
                    if (curr != 1) {
                        int direction = (curr == 0 ? -1 : 1);

                        pagerAdapter.setEvents(new ArrayList<APIEvent>());
                        setDateByOffset(direction);
                        loadDay();

                        binding.calendarPager.setCurrentItem(1, false);
                    }
                }
                super.onPageScrollStateChanged(state);
            }
        });

        setDate(new Date());
        loadDay();

        return binding.getRoot();
    }
}
