package space.myhomework.android.api;

import org.json.JSONException;
import org.json.JSONObject;

public class APIEvent {
    public int ID;
    public String UniqueID;
    public String SeriesID;
    public String SeriesName;
    public String Name;
    public int Start;
    public String StartTimezone;
    public int End;
    public String EndTimezone;

    public APIEvent(JSONObject o) throws JSONException {
        ID = o.getInt("id");
        UniqueID = o.getString("uniqueId");
        SeriesID = o.getString("seriesId");
        SeriesName = o.getString("seriesName");
        Name = o.getString("name");
        Start = o.getInt("start");
        StartTimezone = o.getString("startTimezone");
        End = o.getInt("end");
        EndTimezone = o.getString("endTimezone");

        // TODO: recur rule
        // TODO: tags
    }
}
