package space.myhomework.android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import space.myhomework.android.api.APIClient;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Log in");
        setContentView(R.layout.activity_login);

        ((EditText)findViewById(R.id.password)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    login(findViewById(R.id.login));
                    return true;
                }
                return false;
            }
        });
    }

    public void login(View view) {
        EditText email = (EditText)findViewById(R.id.email);
        EditText password = (EditText)findViewById(R.id.password);
        final String emailStr = email.getText().toString();
        final String passwordStr = password.getText().toString();

        boolean error = false;
        if (emailStr.isEmpty()) {
            email.setError("Email is required.");
            error = true;
        }
        if (passwordStr.isEmpty()) {
            password.setError("Password is required.");
            error = true;
        }
        if (error) {
            return;
        }

        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "Logging in, please wait...", true);

        final Context ctx = this;

        APIClient.getInstance(this, new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("email", emailStr);
                params.put("password", passwordStr);
                APIClient.getInstance(ctx, null).makeRequest(Request.Method.POST, "auth/login", params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // save the session id
                            FileOutputStream fos = ctx.openFileOutput("session_id", Context.MODE_PRIVATE);
                            String sessionID = APIClient.getInstance(ctx, null).getCookieValue("session");
                            byte[] sessionBytes = sessionID.getBytes();
                            fos.write(sessionBytes);
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        progressDialog.dismiss();
                        setResult(42);
                        finish();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                        if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                            builder.setTitle("Error").setMessage("The email or password was incorrect.");
                        } else {
                            builder.setTitle("Unable to connect").setMessage("Could not connect to the MyHomeworkSpace servers.");
                        }
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
            }
        });
    }
}
