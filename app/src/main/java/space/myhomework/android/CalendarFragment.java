package space.myhomework.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import space.myhomework.android.api.APIClient;
import space.myhomework.android.databinding.FragmentCalendarBinding;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private SimpleDateFormat iso8601DateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private Date activeDay;

    public CalendarFragment() {

    }

    private void loadDay() {
        // TODO: loading indicator

        HashMap<String, String> params = new HashMap<>();
        params.put("start", iso8601DateFormat.format(activeDay));
        params.put("end", iso8601DateFormat.format(calculateEnd()));
        APIClient.getInstance(getContext(), null).makeRequest(Request.Method.GET, "calendar/getView", params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                android.util.Log.i("mhscal", response.toString());
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

        setDate(new Date());
        loadDay();

        return binding.getRoot();
    }
}
