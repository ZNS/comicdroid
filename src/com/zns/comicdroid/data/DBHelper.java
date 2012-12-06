package com.zns.comicdroid.data;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	public static final int DB_VERSION = 2;
	public static final String DB_NAME = "ComicDroid.db";
		
	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String tblBooks = "CREATE TABLE tblBooks (" +
				"Id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"GroupId INTEGER," +
				"Title TEXT," +
				"Subtitle TEXT," +
				"Publisher TEXT," +
				"Author TEXT," +
				"Image BLOB," +
				"PublishDate INTEGER," +
				"AddedDate INTEGER," +
				"PageCount INTEGER," +
				"IsBorrowed INTEGER," +
				"Borrower TEXT," +
				"BorrowedDate INTEGER," +
				"ISBN TEXT," +
				"Issue INTEGER" +
				")";
		
		String tblGroups = "CREATE TABLE tblGroups (" +
				"Id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"Name TEXT," +
				"Image BLOB" +
				")";
		
		db.execSQL(tblBooks);
		db.execSQL(tblGroups);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE tblBooks");
		db.execSQL("DROP TABLE tblGroups");
		onCreate(db);
	}
	
	/*@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		if (!db.isReadOnly()) {
			// Enable foreign key constraints
			db.execSQL("PRAGMA foreign_keys=ON;");
		}
	}*/ 
	
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
		
		SQLiteDatabase db = getWritableDatabase();
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
		
		SQLiteDatabase db = getReadableDatabase();
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
		SQLiteDatabase db = getReadableDatabase();
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
		SQLiteDatabase db = getWritableDatabase();
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
