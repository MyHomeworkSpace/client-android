package space.myhomework.android.planner;

import java.util.ArrayList;
import java.util.Date;

import space.myhomework.android.api.APIHomework;

public class PlannerWeek {
    public Date monday;
    public ArrayList<APIHomework> homework;

    public PlannerWeek(Date m, ArrayList<APIHomework> hw) {
        monday = m;
        homework = hw;
    }

    // keeps the server's order
    public ArrayList<APIHomework> homeworkFor(Date day, int classId) {
        ArrayList<APIHomework> result = new ArrayList<>();

        // compare as strings rather than as Dates, since we don't care about the time of day
        String dayString = PlannerDates.formatISO(day);

        for (APIHomework hw : homework) {
            // the class might have been deleted, in which case there's no row to put it in
            if (hw.Class == null) {
                continue;
            }

            if (hw.Class.ID == classId && PlannerDates.formatISO(hw.Due).equals(dayString)) {
                result.add(hw);
            }
        }

        return result;
    }
}
