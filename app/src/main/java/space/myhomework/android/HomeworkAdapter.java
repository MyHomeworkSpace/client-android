package space.myhomework.android;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import space.myhomework.android.api.APIClass;
import space.myhomework.android.api.APIClient;
import space.myhomework.android.api.APIHomework;

public class HomeworkAdapter extends ArrayAdapter<APIHomework> {
    public HomeworkAdapter(Context ctx, ArrayList<APIHomework> classes) {
        super(ctx, 0, classes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        APIHomework hw = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_homework, parent, false);
        }

        View item = convertView.findViewById(R.id.homeworkItem);

        TextView nameView = convertView.findViewById(R.id.homeworkName);
        TextView dueView = convertView.findViewById(R.id.homeworkDue);
        TextView classView = convertView.findViewById(R.id.homeworkClass);

        long distanceToDue = (hw.Due.getTime() - new Date().getTime());
        boolean overdue = false;
        if (distanceToDue < 0) { // overdue
            overdue = true;
            dueView.setTextColor(Color.RED);
            classView.setTextColor(Color.RED);
        }

        String hwNameStr = hw.Name;

        if (overdue) {
            hwNameStr = hwNameStr + " (late)";
        }

        APIClient c = APIClient.getInstance(getContext(), null);
        PrefixInfo prefixInfo = c.prefixes.getPrefixInfo(hwNameStr);
        Spannable nameSpannable = new SpannableString(hwNameStr);

        nameSpannable.setSpan(new BackgroundColorSpan(prefixInfo.BackgroundColor), 0, hwNameStr.split(" ")[0].length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        nameSpannable.setSpan(new ForegroundColorSpan(prefixInfo.TextColor), 0, hwNameStr.split(" ")[0].length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

        if (overdue) {
            nameSpannable.setSpan(new ForegroundColorSpan(Color.RED), hwNameStr.split(" ")[0].length(), hwNameStr.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        }

        nameView.setText(nameSpannable);
        dueView.setText("due " + hw.Due.toString().split(" 00:")[0]);
        classView.setText("in " + hw.Class.Name);

        if (hw.Complete) {
            nameView.setTypeface(null, Typeface.ITALIC);
            nameView.setPaintFlags(nameView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            dueView.setTypeface(null, Typeface.ITALIC);
            dueView.setPaintFlags(dueView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            classView.setTypeface(null, Typeface.ITALIC);
            classView.setPaintFlags(classView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            item.setBackgroundColor(Color.LTGRAY);
        }

        return convertView;
    }
}
