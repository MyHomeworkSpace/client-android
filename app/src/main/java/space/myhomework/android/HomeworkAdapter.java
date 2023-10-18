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

        TextView name = (TextView) convertView.findViewById(R.id.homeworkName);
        TextView subtext = (TextView) convertView.findViewById(R.id.homeworkSubtext);

        long distanceToDue = (hw.Due.getTime() - new Date().getTime());
        boolean overdue = false;
        if (distanceToDue < 0) { // overdue
            overdue = true;
            subtext.setTextColor(Color.RED);
        }

        String hwNameStr = hw.Name;

        if (overdue) {
            hwNameStr = hwNameStr + " (late)";
        }

        PrefixInfo prefixInfo = PrefixManager.getPrefixInfo(hwNameStr);
        Spannable nameSpannable = new SpannableString(hwNameStr);

        nameSpannable.setSpan(new BackgroundColorSpan(prefixInfo.BackgroundColor), 0, hwNameStr.split(" ")[0].length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        nameSpannable.setSpan(new ForegroundColorSpan(prefixInfo.TextColor), 0, hwNameStr.split(" ")[0].length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

        if (overdue) {
            nameSpannable.setSpan(new ForegroundColorSpan(Color.RED), hwNameStr.split(" ")[0].length(), hwNameStr.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        }

        name.setText(nameSpannable);
        subtext.setText("due " + hw.Due.toString().split(" 00:")[0] + " in " + hw.Class);

        if (hw.Complete) {
            name.setTypeface(null, Typeface.ITALIC);
            name.setPaintFlags(name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            subtext.setTypeface(null, Typeface.ITALIC);
            subtext.setPaintFlags(subtext.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            item.setBackgroundColor(Color.LTGRAY);
        }

        return convertView;
    }
}
