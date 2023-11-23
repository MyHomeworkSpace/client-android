package space.myhomework.android.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

import space.myhomework.android.CalendarFragment;
import space.myhomework.android.api.APIEvent;
import space.myhomework.android.databinding.FragmentCalendarDayBinding;

public class CalendarDayFragment extends Fragment {
    private FragmentCalendarDayBinding binding;

    private EventAdapter eventAdapter;

    private Boolean queuedLoading;
    private ArrayList<APIEvent> queuedEvents;

    private CalendarFragment calendarFragment;

    public CalendarDayFragment(CalendarFragment f) {
        calendarFragment = f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalendarDayBinding.inflate(inflater, container, false);

        binding.calendarRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                calendarFragment.loadDay();
            }
        });

        if (queuedLoading != null) {
            setLoading(queuedLoading);
            queuedLoading = null;
        }

        if (queuedEvents != null) {
            setEvents(queuedEvents);
            queuedEvents = null;
        }

        return binding.getRoot();
    }

    public void dismissDialogs() {
        if (eventAdapter != null) {
            eventAdapter.dismissDialogs();
        }
    }

    public void setLoading(boolean loading) {
        if (binding == null) {
            queuedLoading = loading;
            return;
        }

        binding.calendarRefreshLayout.setRefreshing(loading);
    }

    public void setEvents(ArrayList<APIEvent> events) {
        if (binding == null) {
            queuedEvents = events;
            return;
        }

        eventAdapter = new EventAdapter(getActivity(), calendarFragment, events);
        binding.calendarRecyclerView.setAdapter(eventAdapter);
        binding.calendarRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // TODO: relying on the refreshing state is a bit of a hack, relies on the order of function calls
        if (events.isEmpty() && binding.calendarRefreshLayout.isRefreshing()) {
            binding.calendarRecyclerView.setVisibility(View.GONE);
            binding.calendarNoEvents.setVisibility(View.VISIBLE);
        } else {
            binding.calendarRecyclerView.setVisibility(View.VISIBLE);
            binding.calendarNoEvents.setVisibility(View.GONE);
        }
    }
}
