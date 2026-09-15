package space.myhomework.android.planner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import space.myhomework.android.R;
import space.myhomework.android.api.APIClient;
import space.myhomework.android.api.APIHomework;
import space.myhomework.android.databinding.FragmentPlannerBinding;

// the planner tab: owns the selected day and fetching, and hosts a PlannerDayFragment for the day
public class PlannerFragment extends Fragment {
    private static final String DAY_FRAGMENT_TAG = "planner_day";

    private FragmentPlannerBinding binding;

    private Date selectedDay;

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

        requireActivity().setTitle("Planner");

        // the child fragment manager restores the day fragment for us if we're being recreated
        // its arguments survive that and our fields don't, so it's the source of truth for the day
        PlannerDayFragment restored = getDayFragment();
        if (restored != null) {
            selectedDay = restored.getDay();
        } else {
            selectedDay = PlannerDates.startOfDay(new Date());
            getChildFragmentManager().beginTransaction()
                    .add(R.id.planner_day_container, PlannerDayFragment.newInstance(selectedDay), DAY_FRAGMENT_TAG)
                    .commitNow();
        }

        reload();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private PlannerDayFragment getDayFragment() {
        return (PlannerDayFragment) getChildFragmentManager().findFragmentByTag(DAY_FRAGMENT_TAG);
    }

    public Date getSelectedDay() {
        return selectedDay;
    }

    // called by MainActivity after editing homework, and by pull-to-refresh
    public void reload() {
        final PlannerDayFragment dayFragment = getDayFragment();
        if (dayFragment == null) {
            return;
        }

        dayFragment.setLoading(true);

        loadWeek(PlannerDates.mondayOf(selectedDay), new Response.Listener<PlannerWeek>() {
            @Override
            public void onResponse(PlannerWeek week) {
                dayFragment.setWeek(week);
                dayFragment.setLoading(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // don't leave the spinner going forever
                dayFragment.setLoading(false);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Couldn't load planner", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
