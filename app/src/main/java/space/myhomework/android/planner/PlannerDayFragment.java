package space.myhomework.android.planner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Date;

import space.myhomework.android.databinding.FragmentPlannerDayBinding;

// shows one day's worth of the planner, as a list of classes with their homework
// PlannerFragment is responsible for creating these and feeding them data
public class PlannerDayFragment extends Fragment {
    private FragmentPlannerDayBinding binding;

    private Date day;
    private PlannerDayAdapter adapter;

    private Boolean queuedLoading;
    private PlannerWeek queuedWeek;

    // arguments rather than a constructor, since the fragment manager might have to recreate us
    public static PlannerDayFragment newInstance(Date day) {
        PlannerDayFragment fragment = new PlannerDayFragment();

        Bundle args = new Bundle();
        args.putLong("day", day.getTime());
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        day = new Date(requireArguments().getLong("day"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPlannerDayBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new PlannerDayAdapter(requireActivity());
        binding.plannerRecyclerView.setAdapter(adapter);
        binding.plannerRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.plannerRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // we're always added through the child fragment manager, so this is our coordinator
                ((PlannerFragment) getParentFragment()).reload();
            }
        });

        if (queuedLoading != null) {
            setLoading(queuedLoading);
            queuedLoading = null;
        }

        if (queuedWeek != null) {
            setWeek(queuedWeek);
            queuedWeek = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        adapter = null;
    }

    public Date getDay() {
        return day;
    }

    public void setLoading(boolean loading) {
        if (binding == null) {
            queuedLoading = loading;
            return;
        }

        binding.plannerRefreshLayout.setRefreshing(loading);
    }

    public void setWeek(PlannerWeek week) {
        if (binding == null) {
            queuedWeek = week;
            return;
        }

        adapter.setWeek(week, day);
    }
}
