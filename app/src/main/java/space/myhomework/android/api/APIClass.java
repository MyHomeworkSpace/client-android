package space.myhomework.android.api;

import android.os.Parcel;
import android.os.Parcelable;

public class APIClass implements Parcelable {
    public int ID;
    public String Name;
    public String Teacher;

    public APIClass() {

    }

    public APIClass(String name, String teacher) {
        Name = name;
        Teacher = teacher;
    }

    public APIClass(Parcel in) {
        ID = in.readInt();
        Name = in.readString();
        Teacher = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(ID);
        parcel.writeString(Name);
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
