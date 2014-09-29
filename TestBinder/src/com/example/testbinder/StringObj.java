package com.example.testbinder;

import android.os.Parcel;
import android.os.Parcelable;

public class StringObj implements Parcelable{

	String str;

	public StringObj(String str){
		this.str = str;
	}
	
	public StringObj(Parcel in){
		this.str = in.readString();
	}
	
	public static final Parcelable.Creator<Object> CREATOR = new Parcelable.Creator<Object>() {
		@Override
		public Object createFromParcel(Parcel arg0) {
			return new StringObj(arg0);
		}

		@Override
		public Object[] newArray(int arg0) {
			return null;
		}
	};
	
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		arg0.writeString(str);
	}
}
