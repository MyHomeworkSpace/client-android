package space.myhomework.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import space.myhomework.android.api.APIClient;
import space.myhomework.android.api.APIHomework;

public class HomeworkListFragment extends Fragment {

    private View _v;

    public HomeworkListFragment() {

    }

    public boolean hasBeenCreated() {
        return (_v != null);
    }

    public void updateHomework(ArrayList<APIHomework> hw, final Activity activity) {
        HomeworkAdapter itemAdapter = new HomeworkAdapter(activity, hw);
        if (_v == null) {
            Log.i("MyHomeworkSpace", "Tried to updateHomework on list fragment before onCreateView!");
            return;
        }
        ListView listView = (ListView)_v.findViewById(R.id.homeworkList);
        listView.setAdapter(itemAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HomeworkAdapter adapter = (HomeworkAdapter)(parent.getAdapter());
                APIHomework hw = adapter.getItem(position);

                Intent assignmentIntent = new Intent(activity, EditHomeworkActivity.class);
                Bundle assignmentExtras = new Bundle();

                assignmentExtras.putBoolean("isNew", false);
                assignmentExtras.putParcelableArrayList("classes", APIClient.getInstance(getContext(), null).classes);
                assignmentExtras.putParcelable("homework", hw);

                assignmentIntent.putExtras(assignmentExtras);
                getActivity().startActivityForResult(assignmentIntent, 1);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_homework_list, container, false);
        _v = v;
        ((ListView)_v.findViewById(R.id.homeworkList)).setEmptyView(_v.findViewById(R.id.homeworkListEmpty));

        Bundle b = getArguments();

        if (b != null) {
            ArrayList<APIHomework> homework = b.getParcelableArrayList("homework");
            updateHomework(homework, this.getActivity());
        }

        return v;
    }
}
