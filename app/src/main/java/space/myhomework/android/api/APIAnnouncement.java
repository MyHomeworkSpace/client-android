package space.myhomework.android.api;

import org.json.JSONException;
import org.json.JSONObject;

public class APIAnnouncement {
    public int ID;
    public String Text;

    public APIAnnouncement(JSONObject o) throws JSONException {
        ID = o.getInt("id");
        Text = o.getString("text");
    }
}
