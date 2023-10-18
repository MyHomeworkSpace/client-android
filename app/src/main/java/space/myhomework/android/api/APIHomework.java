package space.myhomework.android.api;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class APIHomework implements Parcelable {
    public int ID;
    public String Name;
    public Date Due;
    public String Description;
    public boolean Complete;
    public String Class;
    public int ClassID;
    public int UserID;

    public APIHomework() {

    }

    public APIHomework(Parcel in) {
        ID = in.readInt();
        Name = in.readString();
        try {
            Due = DateFormat.getDateInstance().parse(in.readString());
        } catch (ParseException e) {
            e.printStackTrace();
            Due = new Date();
        }
        Description = in.readString();
        Complete = (in.readByte() != 0);
        Class = in.readString();
        ClassID = in.readInt();
        UserID = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(ID);
        parcel.writeString(Name);
        parcel.writeString(DateFormat.getDateInstance().format(Due));
        parcel.writeString(Description);
        parcel.writeByte(Complete ? (byte)1 : (byte)0);
        parcel.writeString(Class);
        parcel.writeInt(ClassID);
        parcel.writeInt(UserID);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public APIHomework createFromParcel(Parcel parcel) {
            return new APIHomework(parcel);
        }

        @Override
        public APIHomework[] newArray(int i) {
            return new APIHomework[i];
        }
    };
}
