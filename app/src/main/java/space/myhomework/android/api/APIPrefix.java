package space.myhomework.android.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class APIPrefix {
    public int ID;
    public String Background;
    public String Color;
    public String[] Words;
    public boolean TimedEvent;
    public boolean Default;

    public APIPrefix(JSONObject o) throws JSONException {
        ID = o.getInt("id");
        Background = o.getString("background");
        Color = o.getString("color");

        JSONArray wordsJSONArray = o.getJSONArray("words");

        Words = new String[wordsJSONArray.length()];
        for (int i = 0; i < wordsJSONArray.length(); i++) {
            Words[i] = wordsJSONArray.getString(i);
        }

        TimedEvent = o.getBoolean("timedEvent");
        Default = o.getBoolean("default");
    }
}
