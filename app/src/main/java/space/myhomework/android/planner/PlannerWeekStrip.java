package space.myhomework.android.planner;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.color.MaterialColors;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import space.myhomework.android.R;
import space.myhomework.android.databinding.ItemPlannerWeekDayBinding;
import space.myhomework.android.databinding.ViewPlannerWeekStripBinding;

// the row of mon-sun day cells with a week back/forward button on each side
// it only renders; PlannerFragment decides what happens when something is tapped
public class PlannerWeekStrip extends LinearLayout {
    public interface Listener {
        void onDayTapped(Date day);

        // direction is -1 for the previous week, +1 for the next one
        void onWeekTapped(int direction);
    }

    private static final SimpleDateFormat dowFormat = new SimpleDateFormat("EEE", Locale.US);
    private static final SimpleDateFormat numberFormat = new SimpleDateFormat("d", Locale.US);

    // matches the mockup's greyed out past days
    private static final int PAST_COLOR = Color.parseColor("#A5A4AE");

    private ViewPlannerWeekStripBinding binding;
    private ItemPlannerWeekDayBinding[] cells = new ItemPlannerWeekDayBinding[7];
    private Date[] cellDays = new Date[7];

    private Listener listener;

    public PlannerWeekStrip(Context context) {
        super(context);
        init();
    }

    public PlannerWeekStrip(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlannerWeekStrip(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        binding = ViewPlannerWeekStripBinding.inflate(inflater, this);

        for (int i = 0; i < 7; i++) {
            final int index = i;
            cells[i] = ItemPlannerWeekDayBinding.inflate(inflater, binding.plannerWeekDays, true);
            cells[i].getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null && cellDays[index] != null) {
                        listener.onDayTapped(cellDays[index]);
                    }
                }
            });
        }

        binding.plannerWeekPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onWeekTapped(-1);
                }
            }
        });

        binding.plannerWeekNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onWeekTapped(1);
                }
            }
        });
    }

    public void setListener(Listener l) {
        listener = l;
    }

    // renders the week containing selectedDay, with that day filled in
    public void setSelectedDay(Date selectedDay) {
        Date monday = PlannerDates.mondayOf(selectedDay);
        Date today = PlannerDates.startOfDay(new Date());

        String selectedString = PlannerDates.formatISO(selectedDay);
        String todayString = PlannerDates.formatISO(today);

        for (int i = 0; i < 7; i++) {
            Date day = PlannerDates.plusDays(monday, i);
            cellDays[i] = day;

            String dayString = PlannerDates.formatISO(day);
            bindCell(cells[i], day, dayString.equals(todayString), dayString.equals(selectedString), day.before(today));
        }
    }

    private void bindCell(ItemPlannerWeekDayBinding cell, Date day, boolean isToday, boolean isSelected, boolean isPast) {
        cell.plannerWeekDayLabel.setText(dowFormat.format(day));
        cell.plannerWeekDayNumber.setText(numberFormat.format(day));

        int primary = MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary);
        int onPrimary = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnPrimary);
        int onSurfaceVariant = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceVariant);

        // the label only cares about today and past, the circle is what shows the selection
        if (isToday) {
            cell.plannerWeekDayLabel.setTextColor(primary);
        } else if (isPast) {
            cell.plannerWeekDayLabel.setTextColor(PAST_COLOR);
        } else {
            cell.plannerWeekDayLabel.setTextColor(onSurfaceVariant);
        }

        // selected wins over today
        if (isSelected) {
            cell.plannerWeekDayNumber.setBackgroundResource(R.drawable.planner_day_selected_bg);
            cell.plannerWeekDayNumber.setTextColor(onPrimary);
            cell.plannerWeekDayNumber.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        } else if (isToday) {
            cell.plannerWeekDayNumber.setBackgroundResource(R.drawable.planner_day_today_bg);
            cell.plannerWeekDayNumber.setTextColor(primary);
            cell.plannerWeekDayNumber.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            cell.plannerWeekDayNumber.setBackground(null);
            cell.plannerWeekDayNumber.setTextColor(isPast ? PAST_COLOR : onSurfaceVariant);
            cell.plannerWeekDayNumber.setTypeface(Typeface.DEFAULT);
        }
    }
}
