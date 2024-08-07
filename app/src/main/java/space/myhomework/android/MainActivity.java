package space.myhomework.android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Handler;
import android.view.View;

import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import space.myhomework.android.api.APIAccount;
import space.myhomework.android.api.APIClass;
import space.myhomework.android.api.APIClient;

public class MainActivity extends AppCompatActivity
        implements NavigationBarView.OnItemSelectedListener {

    public static final int REQUEST_ADD_OR_EDIT_HOMEWORK = 100;
    public static final int REQUEST_ADD_OR_EDIT_EVENT = 200;

    private Fragment activeFragment;

    public final Response.ErrorListener abandonHandler = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setTitle("MyHomeworkSpace");

        // TODO: for some reason, I cannot find a good way of handling this
        // ("this" meaning showing the top navbar under the system status bar, and showing the bottom tabbar under the system nav bar)
        // it works for the bottom but not the top
        // so, manually hack the top into working
        // I think maybe related: https://stackoverflow.com/a/73938386/2178519
        int statusBarHeight = 0;
        int statusBarHeightID = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (statusBarHeightID != -1) {
            statusBarHeight = getResources().getDimensionPixelSize(statusBarHeightID);
        }
        findViewById(R.id.main_frame_layout).setPadding(0, statusBarHeight, 0, 0);

        final Context ctx = this;
        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "Loading, please wait...", true);
        progressDialog.show();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
                int activeNav = bottomNavigationView.getSelectedItemId();

                if (activeNav == R.id.nav_homework) {
                    Intent assignmentIntent = new Intent(ctx, EditHomeworkActivity.class);
                    Bundle assignmentExtras = new Bundle();
                    assignmentExtras.putBoolean("isNew", true);
                    assignmentExtras.putParcelableArrayList("classes", APIClient.getInstance(ctx, null).classes);
                    assignmentIntent.putExtras(assignmentExtras);
                    startActivityForResult(assignmentIntent, REQUEST_ADD_OR_EDIT_HOMEWORK);
                } else if (activeNav == R.id.nav_classes) {
                    Toast.makeText(ctx, "Adding classes not supported yet!", Toast.LENGTH_SHORT).show();
                } else if (activeNav == R.id.nav_calendar) {
                    Intent eventIntent = new Intent(ctx, EditEventActivity.class);
                    Bundle eventExtras = new Bundle();
                    eventExtras.putBoolean("isNew", true);
                    if (activeFragment instanceof CalendarFragment) {
                        eventExtras.putLong("activeDayTimestamp", ((CalendarFragment) activeFragment).getDate().getTime());
                    }
                    eventIntent.putExtras(eventExtras);
                    startActivityForResult(eventIntent, REQUEST_ADD_OR_EDIT_EVENT);
                }
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this);

        APIClient.getInstance(this, new Runnable() {
            @Override
            public void run() {
                APIClient.getInstance(ctx, null).makeRequest(Request.Method.GET, "auth/context", new HashMap<String, String>(), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            APIClient c = APIClient.getInstance(ctx, null);
                            c.account = new APIAccount(response.getJSONObject("user"));
                            c.prefixes.updatePrefixList(response.getJSONArray("prefixes"));

                            APIClient.getInstance(ctx, null).makeRequest(Request.Method.GET, "classes/get", new HashMap<String, String>(), new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        APIClient c = APIClient.getInstance(ctx, null);
                                        c.classes = new ArrayList<APIClass>();
                                        JSONArray classesObj = response.getJSONArray("classes");

                                        for (int i = 0; i < classesObj.length(); i++) {
                                            JSONObject classObj = classesObj.getJSONObject(i);
                                            APIClass apiClass = new APIClass(classObj);
                                            c.classes.add(apiClass);
                                        }

                                        setTitle("Homework");
                                        renderViewForNavID(R.id.nav_homework);

                                        progressDialog.dismiss();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, abandonHandler);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, abandonHandler);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_log_out) {
            APIClient.clearInstance();
            File file = new File(this.getFilesDir(), "session_id");
            file.delete();
            setResult(1);
            finish();
        }

        return true;
    }

    private void renderViewForNavID(int id) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (id == R.id.nav_homework) {
            activeFragment = new HomeworkFragment();
        } else if (id == R.id.nav_classes) {
            activeFragment = new ClassesFragment();
        } else if (id == R.id.nav_calendar) {
            activeFragment = new CalendarFragment();
        }
        fragmentManager.beginTransaction().replace(R.id.content_frame, activeFragment).commit();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        renderViewForNavID(id);

        setTitle(item.getTitle());

        return true;
    }

    private void reloadContents() {
        // TODO: a bit hacky, probably could improve
        if (activeFragment instanceof CalendarFragment) {
            ((CalendarFragment) activeFragment).dismissDialogs();

            // we don't want to remake the fragment because that would reset it to today
            // so instead we just ask it directly to reload
            // TODO: this is hacky and should be done in a more generic way
            ((CalendarFragment) activeFragment).loadDay();
            return;
        }

        // TODO: can't really reload classes page this way
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        int id = bottomNavigationView.getSelectedItemId();
        renderViewForNavID(id);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            new Handler().post(new Runnable() {
                public void run() {
                    reloadContents();
                }
            });
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
