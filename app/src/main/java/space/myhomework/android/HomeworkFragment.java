package space.myhomework.android;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

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

    public String findClassName(ArrayList<APIClass> classes, int id) {
        for (APIClass classObj : classes) {
            if (classObj.ID == id) {
                return classObj.Name;
            }
        }
        return "Error";
    }

    public void loadHomework(final View v) {
        final Activity activity = this.getActivity();
        final ArrayList<APIClass> classes = APIClient.getInstance(getContext(), null).classes;
        final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout)v.findViewById(R.id.homeworkRefreshLayout);
        APIClient.getInstance(getContext(), null).makeRequest(Request.Method.GET, "homework/getHWView", new HashMap<String, String>(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray homework = response.getJSONArray("homework");

                    ArrayList<APIHomework> tomorrowList = new ArrayList<APIHomework>();
                    ArrayList<APIHomework> soonList = new ArrayList<APIHomework>();
                    ArrayList<APIHomework> longtermList = new ArrayList<APIHomework>();

                    boolean isTomorrowViewMonday = false;
                    long tomorrowViewThreshold = (1000*60*60*24); // one day
                    Calendar c = Calendar.getInstance();
                    c.setTime(new Date());
                    int dow = c.get(Calendar.DAY_OF_WEEK);

                    if (dow == Calendar.FRIDAY || dow == Calendar.SATURDAY || dow == Calendar.SUNDAY) {
                        isTomorrowViewMonday = true;
                        if (dow == Calendar.FRIDAY) {
                            tomorrowViewThreshold = (1000*60*60*24*3); // three days
                        } else if (dow == Calendar.SATURDAY) {
                            tomorrowViewThreshold = (1000*60*60*24*2); // two days
                        }
                    }

                    for (int i = 0; i < homework.length(); i++) {
                        JSONObject homeworkItem = homework.getJSONObject(i);
                        APIHomework homeworkObj = new APIHomework();

                        homeworkObj.ID = homeworkItem.getInt("id");
                        homeworkObj.Name = homeworkItem.getString("name");
                        homeworkObj.Due = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(homeworkItem.getString("due"));
                        homeworkObj.Description = homeworkItem.getString("desc");
                        homeworkObj.Complete = (homeworkItem.getInt("complete") == 1);
                        homeworkObj.ClassID = homeworkItem.getInt("classId");
                        homeworkObj.UserID = homeworkItem.getInt("userId");

                        homeworkObj.Class = findClassName(classes, homeworkObj.ClassID);

                        // WHY DOES THIS CRAPPY LANGUAGE HAVE NO GOOD BUILTIN DATE API
                        long distanceToDue = (homeworkObj.Due.getTime() - new Date().getTime());

                        if (distanceToDue < tomorrowViewThreshold) { // one day
                            // special case - don't show overdue things if they're done
                            if (distanceToDue < 0 && homeworkObj.Complete) {
                                continue;
                            }
                            tomorrowList.add(homeworkObj);
                        } else if (distanceToDue < (1000*60*60*24*5)) { // five days
                            soonList.add(homeworkObj);
                        } else { // one day
                            longtermList.add(homeworkObj);
                        }
                    }

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