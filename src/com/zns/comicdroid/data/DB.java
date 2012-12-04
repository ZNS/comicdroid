package com.zns.comicdroid.data;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DB 
{
	private DBHelper dbHelper;
	
	public DB(Context context)
	{
		dbHelper = new DBHelper(context);
	}
	
	public void storeComic(Comic comic)
	{
		ContentValues values = new ContentValues();
		values.put("GroupId", 0);
		values.put("Title", comic.getTitle());
		values.put("Subtitle", comic.getSubTitle());
		values.put("Publisher", comic.getPublisher());
		values.put("Author", comic.getAuthor());
		values.put("PublishDate", comic.getPublishDateTimestamp());
		values.put("AddedDate", (int)(System.currentTimeMillis() / 1000L));
		values.put("PageCount", comic.getPageCount());
		values.put("IsBorrowed", comic.getIsBorrowed() ? 1 : 0);
		values.put("Borrower", comic.getBorrower());
		values.put("Image", comic.getImage());
		values.put("ISBN", comic.getISBN());
		values.put("Issue", comic.getIssue());
		
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.insert("tblBooks", null, values);
		db.close();
	}
	
	public ArrayList<Comic> getComics(String order)
	{
		ArrayList<Comic> result = new ArrayList<Comic>();
		String orderBy = "Title";
		if (order.equalsIgnoreCase("forfattare"))
			orderBy = "Author";
		else if (order.equalsIgnoreCase("forlag"))
			orderBy = "Publisher";
		
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT Id, Title, Subtitle, Author, Publisher, PublishDate, " +
				"AddedDate, PageCount, IsBorrowed, Borrower, Image, ISBN, Issue FROM tblBooks ORDER BY " + orderBy, null);
		while (cursor.moveToNext())
		{
			Comic comic = new Comic(cursor.getInt(0),
					cursor.getString(1),
					cursor.getString(2),
					cursor.getString(3),
					cursor.getString(4),
					cursor.getInt(5),
					cursor.getInt(6),
					cursor.getInt(7),
					cursor.getInt(8),
					cursor.getString(9),
					cursor.getBlob(10),
					cursor.getString(11),
					cursor.getInt(12));
			result.add(comic);
		}
		cursor.close();
		db.close();
		
		return result;
	}
	
	public Comic getComic(int id)
	{
		Comic comic = null;
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT Id, Title, Subtitle, Author, Publisher, PublishDate, " +
				"AddedDate, PageCount, IsBorrowed, Borrower, Image, ISBN, Issue FROM tblBooks WHERE Id = ?", new String[] { Integer.toString(id) });
		if (cursor.moveToNext())
		{
			comic = new Comic(cursor.getInt(0),
					cursor.getString(1),
					cursor.getString(2),
					cursor.getString(3),
					cursor.getString(4),
					cursor.getInt(5),
					cursor.getInt(6),
					cursor.getInt(7),
					cursor.getInt(8),
					cursor.getString(9),
					cursor.getBlob(10),
					cursor.getString(11),
					cursor.getInt(12));					
		}
		cursor.close();
		db.close();
		
		return comic;
	}
	
	public void deleteComic(int id)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete("tblBooks", "Id=?", new String[] { Integer.toString(id) });
		db.close();
	}
	
	/*public ArrayList<Group> getGroups()
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT Id,  Name, Image FROM tblGroups ORDER BY Name", null);
	}*/
	
	public void getArchiveList()
	{
		String sql = "SELECT Id, Title AS Name, Image, 1 AS ItemType FROM tblBooks WHERE GroupId = 0" +
				"UNION" +
				"SELECT Id, Name, Image, 2 AS ItemType FROM tblGroups" +
				"ORDER BY Name";
	}
}