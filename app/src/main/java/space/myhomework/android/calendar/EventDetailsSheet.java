package space.myhomework.android.calendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import space.myhomework.android.EditEventActivity;
import space.myhomework.android.R;
import space.myhomework.android.api.APIEvent;
import space.myhomework.android.databinding.SheetEventDetailsBinding;

public class EventDetailsSheet extends BottomSheetDialog {
    private APIEvent event;
    private SheetEventDetailsBinding binding;

    private SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);

    public EventDetailsSheet(Activity a, APIEvent e) {
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

        binding.eventActionEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventIntent = new Intent(a, EditEventActivity.class);
                Bundle eventExtras = new Bundle();

                eventExtras.putBoolean("isNew", false);
                eventExtras.putParcelable("event", e);

                eventIntent.putExtras(eventExtras);
                a.startActivityForResult(eventIntent, 1);
            }
        });

        getBehavior().setPeekHeight(a.getResources().getDimensionPixelSize(R.dimen.calendar_event_sheet_peek_height));
    }
}
