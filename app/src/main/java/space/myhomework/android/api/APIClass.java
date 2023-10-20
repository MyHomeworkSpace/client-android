package space.myhomework.android.api;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class APIClass implements Parcelable {
    public int ID;
    public String Name;
    public String Color;
    public String Teacher;

    public APIClass(Parcel in) {
        ID = in.readInt();
        Name = in.readString();
        Color = in.readString();
        Teacher = in.readString();
    }

    public APIClass(JSONObject o) throws JSONException {
        ID = o.getInt("id");
        Name = o.getString("name");
        Color = o.getString("color");
        Teacher = o.getString("teacher");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(ID);
        parcel.writeString(Name);
        parcel.writeString(Color);
        parcel.writeString(Teacher);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public APIClass createFromParcel(Parcel parcel) {
            return new APIClass(parcel);
        }

        @Override
        public APIClass[] newArray(int i) {
            return new APIClass[i];
        }
    };
}
