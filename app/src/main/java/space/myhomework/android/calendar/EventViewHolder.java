package space.myhomework.android.calendar;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import space.myhomework.android.databinding.ItemEventBinding;

public class EventViewHolder extends RecyclerView.ViewHolder {
    public ItemEventBinding binding;

    public EventViewHolder(@NonNull ItemEventBinding b, @NonNull View v) {
        super(v);
        binding = b;
    }
}
