package space.myhomework.android.api;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

import space.myhomework.android.calendar.EventTag;

public class APIEvent implements Parcelable {
    public int ID;
    public String UniqueID;
    public String SeriesID;
    public String SeriesName;
    public String Name;
    public int Start;
    public String StartTimezone;
    public int End;
    public String EndTimezone;

    public APIRecurRule RecurRule;

    public HashMap<EventTag, Object> Tags = new HashMap<>();

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

        if (!o.isNull("recurRule")) {
            RecurRule = new APIRecurRule(o.getJSONObject("recurRule"));
        } else {
            RecurRule = null;
        }

        JSONObject tagsObject = o.getJSONObject("tags");
        for (Iterator<String> it = tagsObject.keys(); it.hasNext(); ) {
            String tagString = it.next();
            EventTag tag = EventTag.fromInteger(Integer.parseInt(tagString));
            Tags.put(tag, tagsObject.get(tagString));
        }
    }

    public APIEvent(Parcel in) {
        ID = in.readInt();
        UniqueID = in.readString();
        SeriesID = in.readString();
        SeriesName = in.readString();
        Name = in.readString();
        Start = in.readInt();
        StartTimezone = in.readString();
        End = in.readInt();
        EndTimezone = in.readString();

        boolean haveRecurRule = in.readInt() > 0;
        if (haveRecurRule) {
            RecurRule = new APIRecurRule(in);
        } else {
            RecurRule = null;
        }

        int tagsSize = in.readInt();
        for (int j = 0; j < tagsSize; j++) {
            EventTag tag = EventTag.fromInteger(in.readInt());

            // TODO: this is kinda sus? what about untrusted input (is that a thing that can even happen here?)
            Tags.put(tag, in.readValue(ClassLoader.getSystemClassLoader()));
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(ID);
        parcel.writeString(UniqueID);
        parcel.writeString(SeriesID);
        parcel.writeString(SeriesName);
        parcel.writeString(Name);
        parcel.writeInt(Start);
        parcel.writeString(StartTimezone);
        parcel.writeInt(End);
        parcel.writeString(EndTimezone);

        boolean haveRecurRule = RecurRule != null;
        parcel.writeInt(haveRecurRule ? 1 : 0);
        if (haveRecurRule) {
            RecurRule.writeToParcel(parcel, i);
        }

        parcel.writeInt(Tags.size());
        for (EventTag tag : Tags.keySet()) {
            parcel.writeInt(tag.ordinal());

            parcel.writeValue(Tags.get(tag));
        }
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public APIEvent createFromParcel(Parcel parcel) {
            return new APIEvent(parcel);
        }

        @Override
        public APIEvent[] newArray(int i) {
            return new APIEvent[i];
        }
    };
}
