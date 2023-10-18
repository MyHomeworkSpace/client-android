package space.myhomework.android;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import space.myhomework.android.api.APIClass;
import space.myhomework.android.api.APIClient;

public class ClassesFragment extends Fragment {

    public ClassesFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_classes, container, false);

        ClassesAdapter itemAdapter = new ClassesAdapter(this.getActivity(), APIClient.getInstance(getContext(), null).classes);
        ListView listView = ((ListView)view.findViewById(R.id.classList));
        listView.setAdapter(itemAdapter);

        return view;
    }
}
