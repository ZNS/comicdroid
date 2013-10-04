package com.zns.comicdroid.amazon;

import android.os.Parcel;
import android.os.Parcelable;


public class Book implements Parcelable {
	public String Id;
	public String ImageUrl;
	public String Title;
	public String Author;
	public String PublicationDate;
	public String Price;
	public String Url;
	
	public Book(String id, String imageUrl, String title, String author, String date, String price, String url)
	{
		this.Id = id;
		this.ImageUrl = imageUrl;
		this.Title = title;
		this.Author = author;
		this.PublicationDate = date;
		this.Price = price;
		this.Url = url;
	}

	public Book (Parcel in) {
		this.Id = in.readString();
		this.ImageUrl = in.readString();
		this.Title = in.readString();
		this.Author = in.readString();
		this.PublicationDate = in.readString();
		this.Price = in.readString();
		this.Url = in.readString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) 
			return true;
		if (obj == null || obj.getClass() != this.getClass()) 
			return false;
		Book b = (Book)obj;
		return b.Id.equals(this.Id);
	}

	@Override
	public int hashCode() {
		return this.Id.hashCode();
	}
	
	@Override
	public int describeContents() {
		return hashCode();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.Id);
		dest.writeString(this.ImageUrl);
		dest.writeString(this.Title);
		dest.writeString(this.Author);
		dest.writeString(this.PublicationDate);
		dest.writeString(this.Price);
		dest.writeString(this.Url);
	}
	
	public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
		public Book createFromParcel(Parcel in) {
			return new Book(in);
		}

		public Book[] newArray(int size) {
			return new Book[size];
		}
	};	
}