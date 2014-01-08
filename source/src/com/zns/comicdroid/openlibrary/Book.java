package com.zns.comicdroid.openlibrary;

import com.google.api.client.util.Key;

public class Book {
	@Key
	public Name[] publishers;
	
	@Key
	public Identifier identifiers;
	
	@Key("subtitle")
	public String subTitle;
	
	@Key
	public String title;
	
	@Key("number_of_pages")
	public int numberOfPages;
	
	@Key("cover")
	public Cover covers;
	
	@Key
	public Name[] authors;
	
	@Key("publish_date")
	public String publishDate;	
}