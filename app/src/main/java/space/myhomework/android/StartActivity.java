package space.myhomework.android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.HashMap;

import space.myhomework.android.api.APIClient;

public class StartActivity extends AppCompatActivity {

    ProgressDialog loadDialog;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 0) {
            finish();
        } else if (resultCode == 1) {
            // logged out, open login page
            startLoginActivity();
        } else if (resultCode == 42) {
            // login was a success, open main ui
            startMainActivity();
        }
    }

    public void startLoginActivity() {
        Intent loginIntent = new Intent(StartActivity.this, LoginActivity.class);
        loginIntent.setAction(Intent.ACTION_VIEW);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityForResult(loginIntent, 1);
    }

    public void startMainActivity() {
        Intent mainIntent = new Intent(StartActivity.this, MainActivity.class);
        mainIntent.setAction(Intent.ACTION_VIEW);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityForResult(mainIntent, 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        loadDialog = ProgressDialog.show(StartActivity.this, "", "Loading, please wait...", true);

        final Context ctx = this;
        APIClient.getInstance(this, new Runnable() {
            @Override
            public void run() {
                APIClient.getInstance(ctx, null).makeRequest(Request.Method.GET, "auth/me", new HashMap<String, String>(), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loadDialog.dismiss();
                        startMainActivity();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // not logged in
                        loadDialog.dismiss();
                        startLoginActivity();
                    }
                });
            }
        });
    }
}
