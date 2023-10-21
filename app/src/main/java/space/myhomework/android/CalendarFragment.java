package space.myhomework.android;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import space.myhomework.android.calendar.EventAdapter;
import space.myhomework.android.databinding.FragmentCalendarBinding;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;

    private SimpleDateFormat iso8601DateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private Date activeDay;

    private ArrayList<APIEvent> events = new ArrayList<>();

    public CalendarFragment() {

    }

    private void loadDay() {
        binding.calendarRefreshLayout.setRefreshing(true);

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

                    // TODO: actually show these events
                    binding.calendarRecyclerView.setAdapter(new EventAdapter(getContext(), events));
                    binding.calendarRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

                    binding.calendarRefreshLayout.setRefreshing(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, ((MainActivity)getActivity()).abandonHandler);
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

    private Date calculateEnd() {
        Calendar c = Calendar.getInstance();

        c.setTime(activeDay);

        c.add(Calendar.DAY_OF_MONTH, 1);

        return c.getTime();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);

        binding.calendarRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadDay();
            }
        });

        setDate(new Date());
        loadDay();

        return binding.getRoot();
    }
}
