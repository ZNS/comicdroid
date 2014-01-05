package com.zns.comicdroid.gcd;

import com.google.api.client.util.Key;

public class Issue {
	@Key
	public int id;
	@Key
	public String number;
	@Key
	public String volume;
	@Key
	public String publication_date;
	@Key
	public int page_count;
	@Key("valid_isbn")
	public String isbn;
	@Key
	public String title;
	@Key("publisher_name")
	public String publisher;
	@Key("brand_name")
	public String brand;
	@Key
	public String script;
	@Key
	public String pencils;
	@Key
	public String inks;
	@Key
	public String colors;
	@Key
	public String letters;
	@Key
	public String feature;
	@Key
	public String[] images;
}
