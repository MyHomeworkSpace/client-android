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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import space.myhomework.android.EditHomeworkActivity;
import space.myhomework.android.MainActivity;
import space.myhomework.android.PrefixInfo;
import space.myhomework.android.R;
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

    // passed with notifyItemRangeChanged so that RecyclerView rebinds the existing view holders in place
    // without a payload, the default item animator cross-fades in new ones, which cuts off the checkbox animation
    private static final Object PAYLOAD_TOGGLE = new Object();

    // one entry per row in the list, in order
    // a header row has a class, a homework row has homework, and an empty row has neither
    private static class Row {
        int type;
        APIClass apiClass;
        APIHomework homework;
        String emptyText;
        // true for the header and homework rows of a section where everything's been done
        boolean allDone;
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
    private Date day;
    private ArrayList<Row> rows = new ArrayList<>();

    public PlannerDayAdapter(Activity a) {
        activity = a;
    }

    public void setWeek(PlannerWeek week, Date d) {
        day = d;
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

            header.allDone = isAllDone(homework);

            for (APIHomework hw : homework) {
                Row row = new Row();
                row.type = TYPE_HOMEWORK;
                row.homework = hw;
                row.allDone = header.allDone;
                rows.add(row);
            }
        }

        notifyDataSetChanged();
    }

    // same rule as the web client: at least one thing due, and all of it done
    private static boolean isAllDone(ArrayList<APIHomework> homework) {
        if (homework.isEmpty()) {
            return false;
        }

        for (APIHomework hw : homework) {
            if (!hw.Complete) {
                return false;
            }
        }

        return true;
    }

    // recomputes the tint for the section containing the given row and rebinds just that section
    // a toggle doesn't add or remove rows, so the row list itself can stay as it is
    private void refreshSection(int position) {
        int start = position;
        while (start > 0 && rows.get(start).type != TYPE_HEADER) {
            start--;
        }

        int end = start + 1;
        while (end < rows.size() && rows.get(end).type != TYPE_HEADER) {
            end++;
        }

        ArrayList<APIHomework> homework = new ArrayList<>();
        for (int i = start + 1; i < end; i++) {
            if (rows.get(i).type == TYPE_HOMEWORK) {
                homework.add(rows.get(i).homework);
            }
        }

        boolean allDone = isAllDone(homework);
        for (int i = start; i < end; i++) {
            rows.get(i).allDone = allDone;
        }

        notifyItemRangeChanged(start, end - start, PAYLOAD_TOGGLE);
    }

    // flips the homework's done state straight away, then tells the server
    // the APIHomework object lives in the cached PlannerWeek, so every page showing it sees the change
    private void toggleComplete(final APIHomework hw, final boolean complete) {
        hw.Complete = complete;
        int position = positionOf(hw);
        if (position != -1) {
            refreshSection(position);
        }

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", Integer.toString(hw.ID));
        params.put("name", hw.Name);
        params.put("due", PlannerDates.formatISO(hw.Due));
        params.put("desc", hw.Description == null ? "" : hw.Description);
        params.put("complete", (complete ? "1" : "0"));
        params.put("classId", Integer.toString(hw.ClassID));

        APIClient.getInstance(activity, null).makeRequest(Request.Method.POST, "homework/edit", params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // nothing to do: the optimistic state is already right, and a reload would flash the spinner
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hw.Complete = !complete;

                // the list might have been rebuilt (or thrown away) by the time this comes back
                int position = positionOf(hw);
                if (position != -1) {
                    refreshSection(position);
                }

                Toast.makeText(activity, "Couldn't update homework", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int positionOf(APIHomework hw) {
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).homework == hw) {
                return i;
            }
        }
        return -1;
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
            bindHeader(holder.headerBinding, row.apiClass, row.allDone);
        } else if (row.type == TYPE_HOMEWORK) {
            bindHomework(holder.homeworkBinding, row.homework, row.allDone);
        } else {
            holder.emptyBinding.plannerEmptyText.setText(row.emptyText);
        }
    }

    private int sectionBackground(boolean allDone) {
        return allDone ? ContextCompat.getColor(activity, R.color.planner_done_bg) : Color.TRANSPARENT;
    }

    private void bindHeader(ItemPlannerClassHeaderBinding binding, final APIClass apiClass, boolean allDone) {
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

        binding.getRoot().setBackgroundColor(sectionBackground(allDone));

        binding.plannerClassAdd.setContentDescription("Add homework for " + apiClass.Name);
        binding.plannerClassAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent assignmentIntent = new Intent(activity, EditHomeworkActivity.class);
                Bundle assignmentExtras = new Bundle();

                assignmentExtras.putBoolean("isNew", true);
                assignmentExtras.putParcelableArrayList("classes", APIClient.getInstance(activity, null).classes);
                // pre-fill the page's day and this class
                assignmentExtras.putLong("dueTimestamp", PlannerDates.startOfDay(day).getTime());
                assignmentExtras.putInt("classId", apiClass.ID);

                assignmentIntent.putExtras(assignmentExtras);
                activity.startActivityForResult(assignmentIntent, MainActivity.REQUEST_ADD_OR_EDIT_HOMEWORK);
            }
        });
    }

    private void bindHomework(final ItemPlannerHomeworkBinding binding, final APIHomework hw, boolean allDone) {
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

        binding.getRoot().setBackgroundColor(sectionBackground(allDone));

        // a click listener rather than a checked-change listener, so setting the state here doesn't count as a toggle
        binding.plannerHwCheckbox.setChecked(hw.Complete);
        binding.plannerHwCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // the checkbox has already flipped itself by the time we hear about it
                toggleComplete(hw, binding.plannerHwCheckbox.isChecked());
            }
        });

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
