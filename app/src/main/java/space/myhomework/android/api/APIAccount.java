package space.myhomework.android.api;

import org.json.JSONException;
import org.json.JSONObject;

public class APIAccount {
    public int ID;
    public String Name;
    public String Email;

    public APIAccount(JSONObject o) throws JSONException {
        ID = o.getInt("id");
        Name = o.getString("name");
        Email = o.getString("email");
    }
}
