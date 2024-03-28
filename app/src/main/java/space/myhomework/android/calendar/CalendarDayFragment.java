package space.myhomework.android.calendar;

import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

import space.myhomework.android.CalendarFragment;
import space.myhomework.android.api.APIAnnouncement;
import space.myhomework.android.api.APIEvent;
import space.myhomework.android.databinding.FragmentCalendarDayBinding;

public class CalendarDayFragment extends Fragment {
    private FragmentCalendarDayBinding binding;

    private EventAdapter eventAdapter;

    private Boolean queuedLoading;
    private ArrayList<APIAnnouncement> queuedAnnouncements;
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

        if (queuedAnnouncements != null) {
            setAnnouncements(queuedAnnouncements);
            queuedAnnouncements = null;
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

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    public void setAnnouncements(ArrayList<APIAnnouncement> announcements) {
        if (binding == null) {
            queuedAnnouncements = announcements;
            return;
        }

        // TODO: we should probably use a RecyclerView or something like that here?
        binding.calendarAnnouncements.removeAllViews();
        for (APIAnnouncement announcement : announcements) {
            // TODO: this view should be, like, inflated from an XML file or something
            TextView announcementView = new TextView(getContext());

            announcementView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            announcementView.setGravity(Gravity.CENTER);

            // this is a big hack to work around what seems to be a bug?
            // see https://stackoverflow.com/q/10420077/2178519
            announcementView.setText(Html.fromHtml("<i>" + Html.escapeHtml(announcement.Text) + "</i>"));

            announcementView.setPadding(0, dpToPx(8), 0, 0);

            TypedValue value = new TypedValue();
            getContext().getTheme().resolveAttribute(android.R.attr.textAppearanceListItem, value, true);
            announcementView.setTextAppearance(value.resourceId);

            binding.calendarAnnouncements.addView(announcementView);
        }
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
