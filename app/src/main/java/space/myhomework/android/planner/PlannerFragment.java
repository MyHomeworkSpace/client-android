package space.myhomework.android.planner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import space.myhomework.android.R;
import space.myhomework.android.api.APIClient;
import space.myhomework.android.api.APIHomework;
import space.myhomework.android.databinding.FragmentPlannerBinding;

// the planner tab: a pager with one PlannerDayFragment per day, plus the week strip above it
// this owns the selected day and the per-week cache, and the day fragments ask it for their data
public class PlannerFragment extends Fragment {
    private static final String STATE_SELECTED_DAY = "selectedDay";

    private static final SimpleDateFormat titleFormat = new SimpleDateFormat("MMM d", Locale.US);

    private FragmentPlannerBinding binding;

    private Date selectedDay;
    private PlannerPagerAdapter adapter;

    // both keyed by the ISO date of the week's monday
    private HashMap<String, PlannerWeek> weeks = new HashMap<>();
    // the value is a counter so that a reload can make an older in-flight request's result get ignored
    private HashMap<String, Integer> loading = new HashMap<>();
    private int nextRequestId = 0;

    public PlannerFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPlannerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_SELECTED_DAY)) {
            selectedDay = new Date(savedInstanceState.getLong(STATE_SELECTED_DAY));
        } else {
            selectedDay = PlannerDates.startOfDay(new Date());
        }

        // the day fragments go in our child fragment manager, so deliverWeek can find them
        adapter = new PlannerPagerAdapter(getChildFragmentManager(), getLifecycle());
        binding.plannerPager.setAdapter(adapter);
        // keep the neighbours around so a swipe doesn't have to wait for a fragment to be built
        binding.plannerPager.setOffscreenPageLimit(1);

        // move to the day before listening for page changes, then do what the callback would have done
        // otherwise we'd get a spurious callback for position 0
        binding.plannerPager.setCurrentItem(PlannerPagerAdapter.dateToPosition(selectedDay), false);
        binding.plannerPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                onDaySelected(PlannerPagerAdapter.positionToDate(position));
            }
        });
        onDaySelected(selectedDay);
    }

    @Override
    public void onResume() {
        super.onResume();

        // MainActivity sets the title to "Planner" when the tab is picked, so make sure ours wins
        updateTitle();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (selectedDay != null) {
            outState.putLong(STATE_SELECTED_DAY, selectedDay.getTime());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        adapter = null;
    }

    public Date getSelectedDay() {
        return selectedDay;
    }

    // called for every page change, including the initial one
    private void onDaySelected(Date day) {
        selectedDay = day;

        ensureWeekLoaded(day);
        // so that crossing into the next or previous week doesn't show a spinner
        ensureWeekLoaded(PlannerDates.plusDays(day, -1));
        ensureWeekLoaded(PlannerDates.plusDays(day, 1));

        updateTitle();
    }

    private void updateTitle() {
        if (selectedDay == null || getActivity() == null) {
            return;
        }

        Date monday = PlannerDates.mondayOf(selectedDay);
        getActivity().setTitle("Week of " + titleFormat.format(monday));
    }

    private void jumpToDay(Date day, boolean smooth) {
        binding.plannerPager.setCurrentItem(PlannerPagerAdapter.dateToPosition(day), smooth);
    }

    // called by MainActivity after editing homework, and by pull-to-refresh
    // refetches without moving the pager
    public void reload() {
        if (selectedDay == null) {
            return;
        }

        // throw away everything, and ignore any request that's already in flight
        // the edit could have touched any week, so evicting just the selected one isn't enough
        weeks.clear();
        loading.clear();

        // every day fragment that exists (visible, offscreen, or cached by the pager) gets refetched,
        // so nothing that's already been built can be left showing stale data
        // fragments created later pick fresh data up through getWeekIfLoaded like usual
        if (isAdded()) {
            for (Fragment fragment : getChildFragmentManager().getFragments()) {
                if (fragment instanceof PlannerDayFragment) {
                    PlannerDayFragment dayFragment = (PlannerDayFragment) fragment;
                    dayFragment.setLoading(true);
                    ensureWeekLoaded(dayFragment.getDay());
                }
            }
        }

        // and the selected day itself, in case its fragment doesn't exist yet
        ensureWeekLoaded(selectedDay);
    }

    // day fragments call this when their view is created; null means it's not cached (yet)
    public PlannerWeek getWeekIfLoaded(Date day) {
        return weeks.get(PlannerDates.formatISO(PlannerDates.mondayOf(day)));
    }

    // starts fetching the week containing the given day, unless it's already cached or being fetched
    // once it lands, every day fragment in that week gets it through setWeek
    public void ensureWeekLoaded(Date day) {
        final Date monday = PlannerDates.mondayOf(day);
        final String key = PlannerDates.formatISO(monday);

        if (weeks.containsKey(key) || loading.containsKey(key)) {
            return;
        }

        final int requestId = nextRequestId++;
        loading.put(key, requestId);

        loadWeek(monday, new Response.Listener<PlannerWeek>() {
            @Override
            public void onResponse(PlannerWeek week) {
                // a reload might have started a newer request for this week since
                if (!isCurrentRequest(key, requestId)) {
                    return;
                }
                loading.remove(key);

                weeks.put(key, week);
                deliverWeek(monday, week);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!isCurrentRequest(key, requestId)) {
                    return;
                }
                loading.remove(key);

                // don't leave the spinners going forever
                for (PlannerDayFragment dayFragment : dayFragmentsInWeek(monday)) {
                    dayFragment.setLoading(false);
                }
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Couldn't load planner", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isCurrentRequest(String key, int requestId) {
        Integer current = loading.get(key);
        return current != null && current == requestId;
    }

    private void deliverWeek(Date monday, PlannerWeek week) {
        for (PlannerDayFragment dayFragment : dayFragmentsInWeek(monday)) {
            dayFragment.setWeek(week);
            dayFragment.setLoading(false);
        }
    }

    // the day fragments that currently exist (visible or kept offscreen) and fall in the given week
    private ArrayList<PlannerDayFragment> dayFragmentsInWeek(Date monday) {
        ArrayList<PlannerDayFragment> result = new ArrayList<>();

        // a response can come back after we've been torn down (e.g. the user switched tabs)
        if (!isAdded()) {
            return result;
        }

        String key = PlannerDates.formatISO(monday);

        for (Fragment fragment : getChildFragmentManager().getFragments()) {
            if (!(fragment instanceof PlannerDayFragment)) {
                continue;
            }

            PlannerDayFragment dayFragment = (PlannerDayFragment) fragment;
            if (PlannerDates.formatISO(PlannerDates.mondayOf(dayFragment.getDay())).equals(key)) {
                result.add(dayFragment);
            }
        }

        return result;
    }

    // fetches the 7 days starting at the given monday
    private void loadWeek(final Date monday, final Response.Listener<PlannerWeek> listener, final Response.ErrorListener errorListener) {
        String url = "homework/getWeek/" + PlannerDates.formatISO(monday);
        APIClient.getInstance(getContext(), null).makeRequest(Request.Method.GET, url, new HashMap<String, String>(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    listener.onResponse(parseWeek(monday, response));
                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                    errorListener.onErrorResponse(new VolleyError(e));
                }
            }
        }, errorListener);
    }

    private PlannerWeek parseWeek(Date monday, JSONObject response) throws JSONException, ParseException {
        ArrayList<APIHomework> homework = new ArrayList<>();

        JSONArray homeworkJSONArray = response.getJSONArray("homework");
        for (int i = 0; i < homeworkJSONArray.length(); i++) {
            homework.add(new APIHomework(homeworkJSONArray.getJSONObject(i), APIClient.getInstance(getContext(), null).classes));
        }

        return new PlannerWeek(monday, homework);
    }
}
