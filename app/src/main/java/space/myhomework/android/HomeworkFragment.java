package space.myhomework.android;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import space.myhomework.android.api.APIClass;
import space.myhomework.android.api.APIClient;
import space.myhomework.android.api.APIHomework;

public class HomeworkFragment extends Fragment {

    public HomeworkFragment() {

    }

    public void sendHomeworkList(Activity activity, HomeworkListFragment frag, ArrayList<APIHomework> list) {
        if (frag.hasBeenCreated()) {
            frag.updateHomework(list, activity);
        } else {
            Bundle b = new Bundle();
            b.putParcelableArrayList("homework", list);
            frag.setArguments(b);
        }
    }

    public void loadHomework(final View v) {
        final Activity activity = this.getActivity();
        final ArrayList<APIClass> classes = APIClient.getInstance(getContext(), null).classes;
        final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout)v.findViewById(R.id.homeworkRefreshLayout);
        APIClient.getInstance(getContext(), null).makeRequest(Request.Method.GET, "homework/getHWViewSorted", new HashMap<String, String>(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    ArrayList<APIHomework> tomorrowList = new ArrayList<APIHomework>();
                    ArrayList<APIHomework> soonList = new ArrayList<APIHomework>();
                    ArrayList<APIHomework> longtermList = new ArrayList<APIHomework>();

                    JSONArray overdueJSONArray = response.getJSONArray("overdue");
                    JSONArray tomorrowJSONArray = response.getJSONArray("tomorrow");
                    JSONArray soonJSONArray = response.getJSONArray("soon");
                    JSONArray longtermJSONArray = response.getJSONArray("longterm");

                    for (int i = 0; i < overdueJSONArray.length(); i++) {
                        tomorrowList.add(new APIHomework(overdueJSONArray.getJSONObject(i), classes));
                    }
                    for (int i = 0; i < tomorrowJSONArray.length(); i++) {
                        tomorrowList.add(new APIHomework(tomorrowJSONArray.getJSONObject(i), classes));
                    }
                    for (int i = 0; i < soonJSONArray.length(); i++) {
                        soonList.add(new APIHomework(soonJSONArray.getJSONObject(i), classes));
                    }
                    for (int i = 0; i < longtermJSONArray.length(); i++) {
                        longtermList.add(new APIHomework(longtermJSONArray.getJSONObject(i), classes));
                    }

                    // TODO: determine if we should show or tomorrow based on the response

                    ViewPager pager = (ViewPager)v.findViewById(R.id.homeworkPager);

                    HomeworkListFragment tomorrowFrag = (HomeworkListFragment)((FragmentPagerAdapter)pager.getAdapter()).getItem(0);
                    HomeworkListFragment soonFrag = (HomeworkListFragment)((FragmentPagerAdapter)pager.getAdapter()).getItem(1);
                    HomeworkListFragment longtermFrag = (HomeworkListFragment)((FragmentPagerAdapter)pager.getAdapter()).getItem(2);

                    sendHomeworkList(activity, tomorrowFrag, tomorrowList);
                    sendHomeworkList(activity, soonFrag, soonList);
                    sendHomeworkList(activity, longtermFrag, longtermList);

                    refreshLayout.setRefreshing(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }, ((MainActivity)getActivity()).abandonHandler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_homework, container, false);

        HomeworkPagerAdapter adapter = new HomeworkPagerAdapter(getChildFragmentManager());
        ViewPager pager = (ViewPager)view.findViewById(R.id.homeworkPager);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(3);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.homeworkTabs);
        tabLayout.setupWithViewPager(pager);

        SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.homeworkRefreshLayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadHomework(view);
            }
        });

        refreshLayout.setRefreshing(true);
        loadHomework(view);

        return view;
    }
}

class HomeworkPagerAdapter extends FragmentPagerAdapter {
    public HomeworkListFragment[] frags;

    public HomeworkPagerAdapter(FragmentManager fm) {
        super(fm);
        frags = new HomeworkListFragment[3];
    }

    @Override
    public Fragment getItem(int i) {
        if (frags[i] == null) {
            frags[i] = new HomeworkListFragment();
        }
        return frags[i];
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            int dow = c.get(Calendar.DAY_OF_WEEK);
            boolean isTomorrowViewMonday = (dow == Calendar.FRIDAY || dow == Calendar.SATURDAY || dow == Calendar.SUNDAY);
            return (isTomorrowViewMonday ? "Monday" : "Tomorrow");
        } else if (position == 1) {
            return "Soon";
        } else if (position == 2) {
            return "Long-term";
        }
        return "Error";
    }
}