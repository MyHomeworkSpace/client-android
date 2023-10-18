package space.myhomework.android;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();

        Date d = new Date();
        try {
            d = DateFormat.getDateInstance().parse(getArguments().getString("date"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        c.setTime(d);

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        // WHY IS IT SO COMPLICATED TO CREATE A DATE OBJECT?!?!?!
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.set(year, month, day);
        ((EditHomeworkActivity)this.getActivity()).setDate(calendar.getTime());
        ((TextView)((EditHomeworkActivity)this.getActivity()).findViewById(R.id.homeworkDueText)).setError(null);
    }
}