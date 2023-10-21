package space.myhomework.android.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import space.myhomework.android.api.APIEvent;
import space.myhomework.android.databinding.ItemEventBinding;

public class EventAdapter extends RecyclerView.Adapter<EventViewHolder> {
    private Context context;
    private ArrayList<APIEvent> events;

    private SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);

    public EventAdapter(Context c, ArrayList<APIEvent> e) {
        context = c;
        events = e;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEventBinding binding = ItemEventBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        View view = binding.getRoot();
        return new EventViewHolder(binding, view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        APIEvent event = events.get(position);

        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new EventDetailsSheet(context, event).show();
            }
        });

        holder.binding.eventName.setText(event.Name);

        StringBuilder subtext = new StringBuilder();

        subtext.append(timeFormat.format(new Date(event.Start * 1000L)));
        subtext.append(" to ");
        subtext.append(timeFormat.format(new Date(event.End * 1000L)));

        String location = (String) event.Tags.get(EventTag.LOCATION);
        if (location != null && !location.isEmpty()) {
            subtext.append(" at ");
            subtext.append(location);
        }

        holder.binding.eventSubtext.setText(subtext.toString());
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}
