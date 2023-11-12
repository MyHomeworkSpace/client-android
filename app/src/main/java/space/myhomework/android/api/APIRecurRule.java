package space.myhomework.android.api;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import space.myhomework.android.calendar.EventTag;

public class APIRecurRule implements Parcelable {
    public int ID;
    public int EventID;
    public APIRecurFrequency Frequency;
    public int Interval;
    public String Until;

    public APIRecurRule(JSONObject o) throws JSONException {
        ID = o.getInt("id");
        EventID = o.getInt("eventId");
        Frequency = APIRecurFrequency.fromInteger(o.getInt("frequency"));
        Interval = o.getInt("interval");
        Until = o.getString("until");

        // TODO: we are ignoring some fields (ByDay, ByMonthDay, ByMonth)
    }

    public APIRecurRule(Parcel in) {
        ID = in.readInt();
        EventID = in.readInt();
        Frequency = APIRecurFrequency.fromInteger(in.readInt());
        Interval = in.readInt();
        Until = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(ID);
        parcel.writeInt(EventID);
        parcel.writeInt(Frequency.ordinal());
        parcel.writeInt(Interval);
        parcel.writeString(Until);
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
