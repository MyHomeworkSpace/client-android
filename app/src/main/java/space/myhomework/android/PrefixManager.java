package space.myhomework.android;

import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import space.myhomework.android.api.APIPrefix;

public class PrefixManager {
    ArrayList<APIPrefix> prefixArray;
    HashMap<String, APIPrefix> prefixMap;

    public void updatePrefixList(JSONArray prefixJSONArray) throws JSONException {
        prefixArray = new ArrayList<>();
        prefixMap = new HashMap<>();

        for (int i = 0; i < prefixJSONArray.length(); i++) {
            APIPrefix prefix = new APIPrefix(prefixJSONArray.getJSONObject(i));
            prefixArray.add(prefix);

            for (String word : prefix.Words) {
                prefixMap.put(word.toLowerCase(), prefix);
            }
        }
    }

    public PrefixInfo getPrefixInfo(String input) {
        String prefixWord = input.split(" ")[0].toLowerCase();
        APIPrefix prefix = prefixMap.get(prefixWord);
        if (prefix == null) {
            return new PrefixInfo(Color.parseColor("#FFD3BD"), Color.BLACK);
        }

        return new PrefixInfo(Color.parseColor("#" + prefix.Background), Color.parseColor("#" + prefix.Color));
    }
}
