package com.example.testbinder;

import android.os.Parcel;
import android.os.Parcelable;

public class AidlObj implements Parcelable{

	int id;
	String msg;

	public AidlObj(int id, String msg){
		this.id = id;
		this.msg = msg;
	}
	
	
	public AidlObj(Parcel in){
		this.id = in.readInt();
		this.msg = in.readString();
	}
	
	
	public static final Parcelable.Creator<AidlObj> CREATOR = new Parcelable.Creator<AidlObj>() {
		@Override
		public AidlObj createFromParcel(Parcel in) {
			return new AidlObj(in);
		}
		@Override
		public AidlObj[] newArray(int arg0) {
			return null;
		}
	};
	
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int arg1) {
		out.writeInt(id);
		out.writeString(msg);
	}
}
