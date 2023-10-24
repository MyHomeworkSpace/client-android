package space.myhomework.android.calendar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import space.myhomework.android.CalendarFragment;
import space.myhomework.android.EditEventActivity;
import space.myhomework.android.MainActivity;
import space.myhomework.android.R;
import space.myhomework.android.api.APIClient;
import space.myhomework.android.api.APIEvent;
import space.myhomework.android.databinding.SheetEventDetailsBinding;

public class EventDetailsSheet extends BottomSheetDialog {
    private APIEvent event;
    private SheetEventDetailsBinding binding;

    private SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);

    public EventDetailsSheet(Activity a, CalendarFragment f, APIEvent e) {
        super(a);
        event = e;

        binding = SheetEventDetailsBinding.inflate(LayoutInflater.from(a));
        setContentView(binding.getRoot());

        setDismissWithAnimation(true);

        binding.dragHandle.setEnabled(true);

        binding.eventName.setText(e.Name);

        StringBuilder timeText = new StringBuilder();
        timeText.append(timeFormat.format(new Date(event.Start * 1000L)));
        timeText.append(" to ");
        timeText.append(timeFormat.format(new Date(event.End * 1000L)));
        binding.eventTime.setText(timeText.toString());

        String location = (String) event.Tags.get(EventTag.LOCATION);
        if (location != null && !location.isEmpty()) {
            binding.eventLocation.setText(location);
            binding.eventLocation.setVisibility(View.VISIBLE);
        } else {
            binding.eventLocation.setText("");
            binding.eventLocation.setVisibility(View.GONE);
        }

        String description = (String) event.Tags.get(EventTag.DESCRIPTION);
        if (description != null && !description.isEmpty()) {
            binding.eventDescription.setText(description);
            binding.eventDescription.setVisibility(View.VISIBLE);
        } else {
            binding.eventDescription.setText("");
            binding.eventDescription.setVisibility(View.GONE);
        }

        Boolean readOnly = (Boolean) e.Tags.get(EventTag.READ_ONLY);
        if (readOnly == null) {
            readOnly = false;
        }

        Boolean cancelled = (Boolean) e.Tags.get(EventTag.CANCELLED);
        if (cancelled == null) {
            cancelled = false;
        }

        Boolean cancelable = (Boolean) e.Tags.get(EventTag.CANCELABLE);
        if (cancelable == null) {
            cancelable = false;
        }

        binding.eventActionEdit.setVisibility((readOnly || e.ID == -1) ? View.GONE : View.VISIBLE);

        binding.eventActionEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventIntent = new Intent(a, EditEventActivity.class);
                Bundle eventExtras = new Bundle();

                eventExtras.putBoolean("isNew", false);
                eventExtras.putParcelable("event", e);

                eventIntent.putExtras(eventExtras);
                a.startActivityForResult(eventIntent, MainActivity.REQUEST_ADD_OR_EDIT_EVENT);
            }
        });

        binding.eventActionCancel.setVisibility(cancelable ? View.VISIBLE : View.GONE);
        binding.eventActionCancel.setText(cancelled ? "Uncancel" : "Cancel");

        Boolean finalCancelled = cancelled;
        binding.eventActionCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // do the thing

                final ProgressDialog progressDialog = ProgressDialog.show(a, "", "Updating event, please wait...", true);
                final Context ctx = a;

                final HashMap<String, String> changeParams = new HashMap<String, String>();

                changeParams.put("eventID", e.UniqueID);
                changeParams.put("cancel", Boolean.toString(!finalCancelled));

                APIClient.getInstance(ctx, null).makeRequest(Request.Method.POST, "calendar/eventChanges/set", changeParams, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialog.dismiss();
                        dismiss();

                        f.loadDay();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();

                        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                        builder.setMessage("Unable to update event. Check your Internet connection.").setTitle("Error");
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

        getBehavior().setPeekHeight(a.getResources().getDimensionPixelSize(R.dimen.calendar_event_sheet_peek_height));
    }
}
