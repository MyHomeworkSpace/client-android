package space.myhomework.android.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import space.myhomework.android.api.APIEvent;
import space.myhomework.android.databinding.ItemEventBinding;

public class EventAdapter extends RecyclerView.Adapter<EventViewHolder> {
    private Context context;
    private ArrayList<APIEvent> events;

    public EventAdapter(Context c, ArrayList<APIEvent> e) {
        context = c;
        events = e;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEventBinding binding = ItemEventBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new EventViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        APIEvent event = events.get(position);
        holder.binding.eventName.setText(event.Name);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}
