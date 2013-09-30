package com.zns.comicdroid.amazon;


public class Book {
	public String Id;
	public String ImageUrl;
	public String Title;
	public String Author;
	public String PublicationDate;
	public String Price;
	
	public Book(String id, String imageUrl, String title, String author, String date, String price)
	{
		this.Id = id;
		this.ImageUrl = imageUrl;
		this.Title = title;
		this.Author = author;
		this.PublicationDate = date;
		this.Price = price;
	}
}
