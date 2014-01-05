package com.zns.comicdroid.gcd;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.client.util.Key;

public class Series implements Parcelable {
	@Key
	public int id;
	@Key
	public String name;
	@Key
	public int issue_count;
	@Key("publisher_name")
	public String publisher;
	@Key
	public String[] images;
	
	public Series() {}
	
	private Series(Parcel in) {
		this.id = in.readInt();
		this.name = in.readString();
		this.issue_count = in.readInt();
		this.publisher = in.readString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) 
			return true;
		if (obj == null || obj.getClass() != this.getClass()) 
			return false;
		Series s = (Series)obj;
		return s.id == this.id;		
	}
	
	@Override
	public int hashCode() {
		return this.id;
	}
	
	@Override
	public int describeContents() {
		return hashCode();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.id);
		dest.writeString(this.name);
		dest.writeInt(this.issue_count);
		dest.writeString(this.publisher);
	}
	
	public static final Parcelable.Creator<Series> CREATOR = new Parcelable.Creator<Series>() {
		public Series createFromParcel(Parcel in) {
			return new Series(in);
		}

		public Series[] newArray(int size) {
			return new Series[size];
		}
	};		
}