package space.myhomework.android.planner;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;

import space.myhomework.android.EditHomeworkActivity;
import space.myhomework.android.MainActivity;
import space.myhomework.android.PrefixInfo;
import space.myhomework.android.api.APIClass;
import space.myhomework.android.api.APIClient;
import space.myhomework.android.api.APIHomework;
import space.myhomework.android.databinding.ItemPlannerClassHeaderBinding;
import space.myhomework.android.databinding.ItemPlannerEmptyBinding;
import space.myhomework.android.databinding.ItemPlannerHomeworkBinding;

public class PlannerDayAdapter extends RecyclerView.Adapter<PlannerDayAdapter.RowViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_HOMEWORK = 1;
    private static final int TYPE_EMPTY = 2;

    // one entry per row in the list, in order
    // a header row has a class, a homework row has homework, and an empty row has neither
    private static class Row {
        int type;
        APIClass apiClass;
        APIHomework homework;
        String emptyText;
    }

    public static class RowViewHolder extends RecyclerView.ViewHolder {
        // only one of these is set, depending on the view type
        ItemPlannerClassHeaderBinding headerBinding;
        ItemPlannerHomeworkBinding homeworkBinding;
        ItemPlannerEmptyBinding emptyBinding;

        public RowViewHolder(@NonNull View v) {
            super(v);
        }
    }

    private Activity activity;
    private ArrayList<Row> rows = new ArrayList<>();

    public PlannerDayAdapter(Activity a) {
        activity = a;
    }

    public void setWeek(PlannerWeek week, Date day) {
        rows = new ArrayList<>();

        ArrayList<APIClass> classes = APIClient.getInstance(activity, null).classes;

        if (classes.isEmpty()) {
            Row row = new Row();
            row.type = TYPE_EMPTY;
            row.emptyText = "No classes yet.";
            rows.add(row);
        }

        // every class gets a section, even if there's nothing in it
        for (APIClass apiClass : classes) {
            Row header = new Row();
            header.type = TYPE_HEADER;
            header.apiClass = apiClass;
            rows.add(header);

            ArrayList<APIHomework> homework = week.homeworkFor(day, apiClass.ID);
            if (homework.isEmpty()) {
                Row empty = new Row();
                empty.type = TYPE_EMPTY;
                empty.emptyText = "Nothing due";
                rows.add(empty);
                continue;
            }

            for (APIHomework hw : homework) {
                Row row = new Row();
                row.type = TYPE_HOMEWORK;
                row.homework = hw;
                rows.add(row);
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).type;
    }

    @NonNull
    @Override
    public RowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_HEADER) {
            ItemPlannerClassHeaderBinding binding = ItemPlannerClassHeaderBinding.inflate(inflater, parent, false);
            RowViewHolder holder = new RowViewHolder(binding.getRoot());
            holder.headerBinding = binding;
            return holder;
        } else if (viewType == TYPE_HOMEWORK) {
            ItemPlannerHomeworkBinding binding = ItemPlannerHomeworkBinding.inflate(inflater, parent, false);
            RowViewHolder holder = new RowViewHolder(binding.getRoot());
            holder.homeworkBinding = binding;
            return holder;
        } else {
            ItemPlannerEmptyBinding binding = ItemPlannerEmptyBinding.inflate(inflater, parent, false);
            RowViewHolder holder = new RowViewHolder(binding.getRoot());
            holder.emptyBinding = binding;
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RowViewHolder holder, int position) {
        Row row = rows.get(position);

        if (row.type == TYPE_HEADER) {
            bindHeader(holder.headerBinding, row.apiClass);
        } else if (row.type == TYPE_HOMEWORK) {
            bindHomework(holder.homeworkBinding, row.homework);
        } else {
            holder.emptyBinding.plannerEmptyText.setText(row.emptyText);
        }
    }

    private void bindHeader(ItemPlannerClassHeaderBinding binding, APIClass apiClass) {
        int color;
        try {
            color = Color.parseColor("#" + apiClass.Color);
        } catch (IllegalArgumentException e) {
            // the server has had some weird colors stored in the past
            color = Color.BLACK;
        }

        binding.plannerClassDot.getBackground().mutate().setTint(color);
        binding.plannerClassName.setText(apiClass.Name);
        binding.plannerClassName.setTextColor(color);
    }

    private void bindHomework(ItemPlannerHomeworkBinding binding, final APIHomework hw) {
        // same prefix highlighting as the homework tab
        APIClient c = APIClient.getInstance(activity, null);
        PrefixInfo prefixInfo = c.prefixes.getPrefixInfo(hw.Name);
        Spannable nameSpannable = new SpannableString(hw.Name);

        int prefixLength = hw.Name.split(" ")[0].length();
        nameSpannable.setSpan(new BackgroundColorSpan(prefixInfo.BackgroundColor), 0, prefixLength, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        nameSpannable.setSpan(new ForegroundColorSpan(prefixInfo.TextColor), 0, prefixLength, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

        binding.plannerHwName.setText(nameSpannable);

        if (hw.Description == null || hw.Description.isEmpty()) {
            binding.plannerHwDescription.setVisibility(View.GONE);
        } else {
            binding.plannerHwDescription.setVisibility(View.VISIBLE);
            binding.plannerHwDescription.setText(hw.Description);
        }

        binding.plannerHwCheckbox.setChecked(hw.Complete);

        // view holders get reused, so make sure to undo this too
        if (hw.Complete) {
            binding.plannerHwName.setTypeface(null, Typeface.ITALIC);
            binding.plannerHwName.setPaintFlags(binding.plannerHwName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            binding.plannerHwName.setTypeface(null, Typeface.NORMAL);
            binding.plannerHwName.setPaintFlags(binding.plannerHwName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }

        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent assignmentIntent = new Intent(activity, EditHomeworkActivity.class);
                Bundle assignmentExtras = new Bundle();

                assignmentExtras.putBoolean("isNew", false);
                assignmentExtras.putParcelableArrayList("classes", APIClient.getInstance(activity, null).classes);
                assignmentExtras.putParcelable("homework", hw);

                assignmentIntent.putExtras(assignmentExtras);
                // go through the activity so that MainActivity reloads us when it comes back
                activity.startActivityForResult(assignmentIntent, MainActivity.REQUEST_ADD_OR_EDIT_HOMEWORK);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }
}
