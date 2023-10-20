package space.myhomework.android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
        implements NavigationView.OnNavigationItemSelectedListener {

    public final Response.ErrorListener abandonHandler = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("MyHomeworkSpace");

        final Context ctx = this;
        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "Loading, please wait...", true);
        progressDialog.show();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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

                            View navHeaderMain = ((NavigationView)findViewById(R.id.nav_view)).getHeaderView(0);

                            ((TextView)navHeaderMain.findViewById(R.id.navName)).setText(c.account.Name);
                            ((TextView)navHeaderMain.findViewById(R.id.navUsername)).setText(c.account.Email);

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
                                        ((NavigationView)findViewById(R.id.nav_view)).setCheckedItem(R.id.nav_homework);
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
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
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
        //} else if (id == R.id.nav_planner) {

        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_log_out) {
            APIClient.clearInstance();
            File file = new File(this.getFilesDir(), "session_id");
            file.delete();
            setResult(1);
            finish();
        }

        if (id != R.id.nav_settings) {
            setTitle(item.getTitle());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
