package com.zns.comicdroid.openlibrary;

import com.google.api.client.util.Key;

public class Identifier {
	@Key("isbn_13")
	public String[] isbn13;
	
	@Key("isbn_10")
	public String[] isbn10;
}
