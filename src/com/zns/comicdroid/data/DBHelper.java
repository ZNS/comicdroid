package com.zns.comicdroid.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	public static final int DB_VERSION = 8;
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
				"Image BLOB," +
				"BookCount INTEGER" +
				")";
		
		db.execSQL(tblBooks);
		db.execSQL(tblGroups);
		
		String triggerGroupId = "CREATE TRIGGER update_boook_groupid_image AFTER UPDATE OF GroupId ON tblBooks " +
				"WHEN new.Issue = 1 " +
				"BEGIN " +
				"UPDATE tblGroups SET Image = new.Image WHERE new.GroupId <> 0 AND Id = new.GroupId; " +
				"END;";
		
		String triggerGroupId4 = "CREATE TRIGGER insert_boook_groupid_image AFTER INSERT ON tblBooks " +
				"WHEN new.Issue = 1 " +
				"BEGIN " +
				"UPDATE tblGroups SET Image = new.Image WHERE new.GroupId <> 0 AND Id = new.GroupId; " +
				"END;";
		
		String triggerGroupId2 = "CREATE TRIGGER update_book_groupid_count AFTER UPDATE OF GroupId ON tblBooks " +
				"WHEN new.GroupId <> old.GroupId " +
				"BEGIN " +
				"UPDATE tblGroups SET BookCount=BookCount+1 WHERE new.GroupId <> 0 AND Id = new.GroupId; " +
				"UPDATE tblGroups SET BookCount=BookCount-1 WHERE old.GroupId <> 0 AND Id = old.GroupId; " +
				"END;";
		
		String triggerGroupId3 = "CREATE TRIGGER insert_book_groupid_count AFTER INSERT ON tblBooks " +
				"WHEN new.GroupId > 0 " +
				"BEGIN " +
				"UPDATE tblGroups SET BookCount=BookCount+1 WHERE Id = new.GroupId; " +
				"END;";
		
		db.execSQL(triggerGroupId);
		db.execSQL(triggerGroupId2);
		db.execSQL(triggerGroupId3);
		db.execSQL(triggerGroupId4);
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
	
	public int GetDateStamp(String strDate) 
			throws ParseException
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
		Date date = dateFormat.parse(strDate);
		return (int)(date.getTime() / 1000L);
	}
	
	public void storeComic(Comic comic)
	{
		ContentValues values = new ContentValues();
		values.put("GroupId", comic.getGroupId());
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
				"AddedDate, PageCount, IsBorrowed, Borrower, Image, ISBN, Issue, GroupId FROM tblBooks ORDER BY " + orderBy, null);
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
					cursor.getInt(12),
					cursor.getInt(13));
			result.add(comic);
		}
		cursor.close();
		db.close();
		
		return result;
	}
	
	public List<Comic> getComics(int[] ids)
	{
		List<Comic> list = new ArrayList<Comic>();
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT Id, Title, Subtitle, Author, Publisher, PublishDate, " +
				"AddedDate, PageCount, IsBorrowed, Borrower, Image, ISBN, Issue, GroupId FROM tblBooks WHERE Id IN (" +
				Joiner.on(",").join(Ints.asList(ids)) +
				")", null);
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
					cursor.getInt(12),
					cursor.getInt(13));
			list.add(comic);
		}
		cursor.close();
		db.close();		
		return list;
	}
	
	public Comic getComic(int id)
	{
		Comic comic = null;
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT Id, Title, Subtitle, Author, Publisher, PublishDate, " +
				"AddedDate, PageCount, IsBorrowed, Borrower, Image, ISBN, Issue, GroupId FROM tblBooks WHERE Id = ?", new String[] { Integer.toString(id) });
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
					cursor.getInt(12),
					cursor.getInt(13));					
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
	
	public List<Group> getGroups()
	{
		List<Group> groups = new ArrayList<Group>();
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT Id,  Name, Image FROM tblGroups ORDER BY Name", null);
		while (cursor.moveToNext()) {			
			groups.add(new Group(cursor.getInt(0),
					cursor.getString(1),
					cursor.getBlob(2)));
		}
		cursor.close();
		db.close();
		return groups;
	}
	
	public boolean addGroup(String name) {
		boolean isValid = true;
		ContentValues values = new ContentValues();
		values.put("Name", name);

		SQLiteDatabase db = getWritableDatabase();		
		//Dupecheck
		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM tblGroups WHERE Name = ? COLLATE NOCASE", new String[] { name });
		if (cursor.moveToFirst() && cursor.getInt(0) > 0)
			isValid = false;
		cursor.close();
		
		if (isValid) {
			db.insert("tblGroups", null, values);
		}
		
		db.close();		
		return isValid;
	}
	
	public void getArchiveList()
	{
		String sql = "SELECT Id, Title AS Name, Image, 1 AS ItemType FROM tblBooks WHERE GroupId = 0" +
				"UNION" +
				"SELECT Id, Name, Image, 2 AS ItemType FROM tblGroups" +
				"ORDER BY Name";
	}
	
	public boolean IsDuplicate(String isbn) {
		boolean duplicate = false;
		SQLiteDatabase db = getReadableDatabase();		
		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM tblBooks WHERE ISBN = ?", new String[] { isbn });
		if (cursor.moveToFirst())
		{
			int count = cursor.getInt(0);
			duplicate = count > 0;
		}
		cursor.close();
		db.close();
		return duplicate;
	}
}
