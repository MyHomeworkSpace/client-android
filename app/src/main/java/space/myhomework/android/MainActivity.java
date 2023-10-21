package space.myhomework.android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.view.WindowCompat;
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

        final Context ctx = this;
        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "Loading, please wait...", true);
        progressDialog.show();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getTitle().equals("Homework")) {
                    Intent assignmentIntent = new Intent(ctx, EditHomeworkActivity.class);
                    Bundle assignmentExtras = new Bundle();
                    assignmentExtras.putBoolean("isNew", true);
                    assignmentExtras.putParcelableArrayList("classes", APIClient.getInstance(ctx, null).classes);
                    assignmentIntent.putExtras(assignmentExtras);
                    startActivityForResult(assignmentIntent, 1);
                } else if (getTitle().equals("Classes")) {
                    Toast.makeText(ctx, "Adding classes not supported yet!", Toast.LENGTH_SHORT).show();
                } else {

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
                                        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new HomeworkFragment()).commit();

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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (id == R.id.nav_homework) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new HomeworkFragment()).commit();
        } else if (id == R.id.nav_classes) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new ClassesFragment()).commit();
        } else if (id == R.id.nav_calendar) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new CalendarFragment()).commit();
        }

        setTitle(item.getTitle());

        return true;
    }

    private void reloadContents() {
        // TODO: what if we're on classes page?
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new HomeworkFragment()).commit();
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
