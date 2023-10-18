package space.myhomework.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import space.myhomework.android.api.APIClass;

public class ClassesAdapter extends ArrayAdapter<APIClass> {
    public ClassesAdapter(Context ctx, ArrayList<APIClass> classes) {
        super(ctx, 0, classes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        APIClass cls = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_class, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.className);
        TextView teacher = (TextView) convertView.findViewById(R.id.classTeacher);

        name.setText(cls.Name);
        teacher.setText(cls.Teacher);

        if (cls.Teacher.isEmpty()) {
            teacher.setVisibility(View.GONE);
            teacher.refreshDrawableState();
        }

        return convertView;
    }
}
