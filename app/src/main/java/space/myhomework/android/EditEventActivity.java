package space.myhomework.android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.HashMap;

import space.myhomework.android.api.APIClient;
import space.myhomework.android.api.APIEvent;
import space.myhomework.android.calendar.EventTag;
import space.myhomework.android.databinding.ActivityEditEventBinding;

public class EditEventActivity extends AppCompatActivity {
    private ActivityEditEventBinding binding;

    private boolean isNew;
    private APIEvent event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEditEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle params = getIntent().getExtras();
        isNew = params.getBoolean("isNew");

        setTitle(isNew ? "Add event" : "Edit event");

        if (!isNew) {
            event = params.getParcelable("event");

            binding.eventName.setText(event.Name);

            String location = (String) event.Tags.get(EventTag.LOCATION);
            if (location == null) {
                location = "";
            }
            binding.eventLocation.setText(location);

            String description = (String) event.Tags.get(EventTag.DESCRIPTION);
            if (description == null) {
                description = "";
            }
            binding.eventDescription.setText(description);
        }

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });
    }

    private void save() {
        boolean error = false;

        String name = binding.eventName.getText().toString();
        String location = binding.eventLocation.getText().toString();
        String description = binding.eventDescription.getText().toString();

        if (name.isEmpty()) {
            binding.eventName.setError("Name is required");
            error = true;
        }

        if (error) {
            return;
        }

        final HashMap<String, String> saveParams = new HashMap<String, String>();

        saveParams.put("name", name);
        saveParams.put("start", Integer.toString(event.Start));
        saveParams.put("end", Integer.toString(event.End));
        saveParams.put("location", location);
        saveParams.put("desc", description);

        // TODO: this is bad
        saveParams.put("recur", "false");

        final String saveURL = (isNew ? "calendar/events/add" : "calendar/events/edit");

        if (!isNew) {
            saveParams.put("id", Integer.toString(event.ID));
        }

        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "Saving event, please wait...", true);
        final Context ctx = this;

        APIClient.getInstance(ctx, new Runnable() {
            @Override
            public void run() {
                APIClient.getInstance(ctx, null).makeRequest(Request.Method.POST, saveURL, saveParams, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialog.dismiss();

                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();

                        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                        builder.setMessage("Unable to save event. Check your Internet connection.").setTitle("Error");
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