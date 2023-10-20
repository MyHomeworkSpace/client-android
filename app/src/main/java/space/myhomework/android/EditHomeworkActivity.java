package space.myhomework.android;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import space.myhomework.android.api.APIClass;
import space.myhomework.android.api.APIClient;
import space.myhomework.android.api.APIHomework;

public class EditHomeworkActivity extends AppCompatActivity {

    private boolean isNew;
    private APIHomework hw;
    private ArrayList<APIClass> classes;

    private Date dueDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_homework);

        Bundle params = getIntent().getExtras();
        isNew = params.getBoolean("isNew");

        setTitle((isNew ? "Add homework" : "Edit homework"));

        classes = params.getParcelableArrayList("classes");
        Spinner classSpinner = (Spinner)findViewById(R.id.homeworkClass);

        ArrayList<Integer> classIDs = new ArrayList<Integer>();
        ArrayList<String> classStrings = new ArrayList<String>();

        classIDs.add(-1);
        classStrings.add("No class");
        for (APIClass apiClass : classes) {
            classIDs.add(apiClass.ID);
            classStrings.add(apiClass.Name);
        }

        classSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, classStrings));

        if (!isNew) {
            hw = params.getParcelable("homework");

            ((EditText)findViewById(R.id.homeworkName)).setText(hw.Name);
            setDate(hw.Due);
            for (int i = 0; i < classSpinner.getCount(); i++) {
                if (classIDs.get(i).equals(hw.Class.ID)) {
                    classSpinner.setSelection(i);
                    break;
                }
            }
            ((CheckBox)findViewById(R.id.homeworkDone)).setChecked(hw.Complete);
            ((EditText)findViewById(R.id.homeworkDesc)).setText(hw.Description);
        }
    }

    public void setDate(Date d) {
        dueDate = d;
        ((TextView)findViewById(R.id.homeworkDueText)).setText("due " + new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US).format(dueDate));
    }

    public void openDueDateDialog(View v) {
        Date dialogDate = dueDate;
        if (dialogDate == null) {
            dialogDate = new Date();
        }
        DatePickerFragment frag = new DatePickerFragment();
        Bundle args = new Bundle();
        args.putString("date", DateFormat.getDateInstance().format(dialogDate));
        frag.setArguments(args);
        frag.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_homework, menu);
        if (isNew) {
            menu.removeItem(R.id.action_delete);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return onBack();
            case R.id.action_discard_changes:
                new AlertDialog.Builder(this)
                    .setTitle("Leave without saving?")
                    .setMessage("Are you sure you want to discard your changes?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setResult(0);
                            finish();
                        }

                    })
                    .setNegativeButton("No", null)
                    .show();
                return true;
            case R.id.action_delete:
                final Context ctx = this;
                new AlertDialog.Builder(this)
                        .setTitle("Delete homework?")
                        .setMessage("Are you sure you want to delete this homework?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final ProgressDialog progressDialog = ProgressDialog.show(ctx, "", "Deleting homework, please wait...", true);

                                APIClient.getInstance(ctx, new Runnable() {
                                    @Override
                                    public void run() {
                                        final HashMap<String, String> delParams = new HashMap<String, String>();
                                        delParams.put("id", Integer.toString(hw.ID));
                                        APIClient.getInstance(ctx, null).makeRequest(Request.Method.POST, "homework/delete", delParams, new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                progressDialog.dismiss();

                                                setResult(0);
                                                finish();
                                            }
                                        }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                progressDialog.dismiss();

                                                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                                                builder.setMessage("Unable to delete homework. Check your Internet connection.").setTitle("Error");
                                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                                AlertDialog dialog = builder.create();
                                                dialog.show();
                                            }
                                        });
                                    }
                                });
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return onBack();
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onBack() {
        EditText homeworkName = (EditText) findViewById(R.id.homeworkName);
        TextView homeworkDue = (TextView) findViewById(R.id.homeworkDueText);
        EditText homeworkDesc = (EditText) findViewById(R.id.homeworkDesc);
        Spinner homeworkClass = (Spinner) findViewById(R.id.homeworkClass);
        CheckBox homeworkDone = (CheckBox) findViewById(R.id.homeworkDone);

        boolean error = false;

        if (homeworkName.getText().toString().isEmpty()) {
            homeworkName.setError("Name is required");
            error = true;
        } else {
            homeworkName.setError(null);
        }
        if (dueDate == null) {
            homeworkDue.setError("Due date is required");
            error = true;
        } else {
            homeworkDue.setError(null);
        }
        if (homeworkClass.getSelectedItemPosition() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("You must select a class!").setTitle("Error");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        }

        if (error) {
            return true;
        }

        int classId = -1;
        for (APIClass classObj : classes) {
            if (classObj.Name.equals(homeworkClass.getSelectedItem())) {
                classId = classObj.ID;
                break;
            }
        }

        final HashMap<String, String> saveParams = new HashMap<String, String>();

        saveParams.put("name", homeworkName.getText().toString());
        saveParams.put("due", new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(dueDate));
        saveParams.put("desc", homeworkDesc.getText().toString());
        saveParams.put("complete", (homeworkDone.isChecked() ? "1" : "0"));
        saveParams.put("classId", Integer.toString(classId));

        final String saveURL = (isNew ? "homework/add" : "homework/edit");

        if (!isNew) {
            saveParams.put("id", Integer.toString(hw.ID));
        }

        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "Saving homework, please wait...", true);
        final Context ctx = this;

        APIClient.getInstance(ctx, new Runnable() {
            @Override
            public void run() {
                APIClient.getInstance(ctx, null).makeRequest(Request.Method.POST, saveURL, saveParams, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialog.dismiss();

                        setResult(1);
                        finish();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();

                        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                        builder.setMessage("Unable to save homework. Check your Internet connection.").setTitle("Error");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
            }
        });

        return false;
    }
}