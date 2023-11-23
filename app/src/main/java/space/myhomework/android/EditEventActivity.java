package space.myhomework.android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.timepicker.MaterialTimePicker;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import space.myhomework.android.api.APIClient;
import space.myhomework.android.api.APIEvent;
import space.myhomework.android.calendar.EventTag;
import space.myhomework.android.databinding.ActivityEditEventBinding;

public class EditEventActivity extends AppCompatActivity {
    private SimpleDateFormat dateDisplayFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.US);
    private SimpleDateFormat timeDisplayFormat = new SimpleDateFormat("h:mm a", Locale.US);

    private ActivityEditEventBinding binding;

    private boolean isNew;
    private APIEvent event;

    private int start;
    private int end;

    private int initialStart;
    private int initialEnd;
    private String initialLocation;
    private String initialDescription;

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

            Integer originalStart = (Integer) event.Tags.get(EventTag.ORIGINAL_START);
            Integer originalEnd = (Integer) event.Tags.get(EventTag.ORIGINAL_END);

            start = originalStart != null ? originalStart : event.Start;
            end = originalEnd != null ? originalEnd : event.End;

            String location = (String) event.Tags.get(EventTag.LOCATION);
            if (location == null) {
                location = "";
            }
            binding.eventLocation.setText(location);
            initialLocation = location;

            String description = (String) event.Tags.get(EventTag.DESCRIPTION);
            if (description == null) {
                description = "";
            }
            binding.eventDescription.setText(description);
            initialDescription = description;
        } else {
            // need to choose a sane default for start and
            Calendar now = GregorianCalendar.getInstance();
            now.setTime(new Date());

            Calendar localCalendar = GregorianCalendar.getInstance();
            long date = params.getLong("activeDayTimestamp", new Date().getTime());

            localCalendar.setTime(new Date(date));

            // TODO: this doesn't match what we do on the site - should probably round up, not down.
            // TODO: what about if it's not today? still do this?
            localCalendar.set(Calendar.SECOND, 0);
            localCalendar.set(Calendar.MILLISECOND, 0);
            localCalendar.set(Calendar.MINUTE, (now.get(Calendar.MINUTE) / 15) * 15);
            localCalendar.set(Calendar.HOUR, now.get(Calendar.HOUR));

            start = (int) (localCalendar.getTimeInMillis() / 1000L);

            localCalendar.add(Calendar.MINUTE, 30);
            end = (int) (localCalendar.getTimeInMillis() / 1000L);

            initialLocation = "";
            initialDescription = "";
        }

        initialStart = start;
        initialEnd = end;

        updateDateTime();

        binding.eventStartDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker(false);
            }
        });
        binding.eventStartTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTimePicker(false);
            }
        });
        binding.eventEndDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker(true);
            }
        });
        binding.eventEndTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTimePicker(true);
            }
        });

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });
    }

    private void openDatePicker(boolean changeEnd) {
        int targetTime = changeEnd ? end : start;

        Calendar localCalendar = GregorianCalendar.getInstance();
        localCalendar.setTimeInMillis(targetTime * 1000L);

        Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utcCalendar.set(Calendar.YEAR, localCalendar.get(Calendar.YEAR));
        utcCalendar.set(Calendar.MONTH, localCalendar.get(Calendar.MONTH));
        utcCalendar.set(Calendar.DAY_OF_MONTH, localCalendar.get(Calendar.DAY_OF_MONTH));

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder
                .datePicker()
                .setSelection(utcCalendar.getTimeInMillis())
                .build();
        picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
            @Override
            public void onPositiveButtonClick(Long selection) {
                utcCalendar.setTimeInMillis(selection);

                localCalendar.set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR));
                localCalendar.set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH));
                localCalendar.set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH));

                if (changeEnd) {
                    end = (int) (localCalendar.getTimeInMillis() / 1000L);
                } else {
                    int difference = end - start;
                    start = (int) (localCalendar.getTimeInMillis() / 1000L);
                    end = start + difference;
                }

                updateDateTime();
            }
        });
        picker.show(getSupportFragmentManager(), "datePicker");
    }

    private void openTimePicker(boolean changeEnd) {
        int targetTime = changeEnd ? end : start;

        Calendar localCalendar = GregorianCalendar.getInstance();
        localCalendar.setTimeInMillis(targetTime * 1000L);

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setHour(localCalendar.get(Calendar.HOUR))
                .setMinute(localCalendar.get(Calendar.MINUTE))
                .build();
        picker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                localCalendar.set(Calendar.HOUR, picker.getHour());
                localCalendar.set(Calendar.MINUTE, picker.getMinute());

                if (changeEnd) {
                    end = (int) (localCalendar.getTimeInMillis() / 1000L);
                } else {
                    int difference = end - start;
                    start = (int) (localCalendar.getTimeInMillis() / 1000L);
                    end = start + difference;
                }

                updateDateTime();
            }
        });
        picker.show(getSupportFragmentManager(), "timePicker");
    }

    private void updateDateTime() {
        Date startObject = new Date(start * 1000L);
        Date endObject = new Date(end * 1000L);

        binding.eventStartDateButton.setText(dateDisplayFormat.format(startObject));
        binding.eventStartTimeButton.setText(timeDisplayFormat.format(startObject));

        binding.eventEndDateButton.setText(dateDisplayFormat.format(endObject));
        binding.eventEndTimeButton.setText(timeDisplayFormat.format(endObject));

        if (start > end) {
            binding.eventEndTimeButton.setError("End time must be after start");
        } else {
            binding.eventEndTimeButton.setError(null);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return onBack();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_event, menu);
        if (isNew) {
            menu.removeItem(R.id.action_delete);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return onBack();
            case R.id.action_delete:
                final Context ctx = this;
                new AlertDialog.Builder(this)
                        .setTitle("Delete event?")
                        .setMessage("Are you sure you want to delete this event?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final ProgressDialog progressDialog = ProgressDialog.show(ctx, "", "Deleting event, please wait...", true);

                                APIClient.getInstance(ctx, new Runnable() {
                                    @Override
                                    public void run() {
                                        final HashMap<String, String> delParams = new HashMap<String, String>();
                                        delParams.put("id", Integer.toString(event.ID));
                                        APIClient.getInstance(ctx, null).makeRequest(Request.Method.POST, "calendar/events/delete", delParams, new Response.Listener<JSONObject>() {
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
                                                builder.setMessage("Unable to delete event. Check your Internet connection.").setTitle("Error");
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

                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean hasChangeBeenMade() {
        String name = binding.eventName.getText().toString();
        String location = binding.eventLocation.getText().toString();
        String description = binding.eventDescription.getText().toString();

        String initialName = event != null ? event.Name : "";

        if (!name.equals(initialName)) {
            return true;
        }

        if (start != initialStart) {
            return true;
        }

        if (end != initialEnd) {
            return true;
        }

        if (!location.equals(initialLocation)) {
            return true;
        }

        if (!description.equals(initialDescription)) {
            return true;
        }

        return false;
    }

    public boolean onBack() {
        if (!hasChangeBeenMade()) {
            setResult(Activity.RESULT_CANCELED);
            finish();
            return true;
        }

        new AlertDialog.Builder(this)
                .setTitle("Leave without saving?")
                .setMessage("Are you sure you want to discard your changes?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(Activity.RESULT_CANCELED);
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
        return true;
    }

    private void save() {
        boolean error = false;

        String name = binding.eventName.getText().toString();
        String location = binding.eventLocation.getText().toString();
        String description = binding.eventDescription.getText().toString();

        if (name.isEmpty()) {
            binding.eventName.setError("Name is required");
            error = true;
        } else {
            binding.eventName.setError(null);
        }

        if (start > end) {
            new AlertDialog.Builder(this)
                .setMessage("The event's end time must be after its start time.")
                .setTitle("Error")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();

            error = true;
        }

        if (error) {
            return;
        }

        final HashMap<String, String> saveParams = new HashMap<String, String>();

        saveParams.put("name", name);
        saveParams.put("start", Integer.toString(start));
        saveParams.put("end", Integer.toString(end));
        saveParams.put("location", location);
        saveParams.put("desc", description);

        if (event != null && event.RecurRule != null) {
            saveParams.put("recur", "true");
            saveParams.put("recurFrequency", Integer.toString(event.RecurRule.Frequency.ordinal()));
            saveParams.put("recurInterval", Integer.toString(event.RecurRule.Interval));
            if (!event.RecurRule.Until.isEmpty()) {
                saveParams.put("recurUntil", event.RecurRule.Until);
            }
        } else {
            saveParams.put("recur", "false");
        }

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