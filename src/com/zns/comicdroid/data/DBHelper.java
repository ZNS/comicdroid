package com.zns.comicdroid.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;

public class DBHelper extends SQLiteOpenHelper {
	
	private static final int DB_VERSION = 	8;
	private static final String DB_NAME = 	"ComicDroid.db";
	
    private SQLiteDatabase db;
    
    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        db = getWritableDatabase();
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
	
    @Override
    public synchronized void close() {
    	db.close();
    	super.close();
    }
	
    @Override
    public SQLiteDatabase getWritableDatabase () {
    	if (db != null)
    		return db;
    	return super.getWritableDatabase();
    }
    
    @Override
    public SQLiteDatabase getReadableDatabase () {
    	return getWritableDatabase();
    }
    
    public Cursor getCursor(String sql, String[] selectionArgs) {
    	return db.rawQuery(sql, selectionArgs);
    }
    
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
    	return db.update(table, values, whereClause, whereArgs);
    }
    
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
		
		String title = comic.getTitle().trim();
		if (title.toLowerCase().startsWith("the ")) {
			title = title.substring(4) + ", The";
		}
		
		values.put("GroupId", comic.getGroupId());
		values.put("Title", title);
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
		
		db.insert("tblBooks", null, values);
	}
	
	public List<Comic> getComics(String order)
	{
		List<Comic> result = new ArrayList<Comic>();
		String orderBy = "Title";
		if (order.equalsIgnoreCase("forfattare"))
			orderBy = "Author";
		else if (order.equalsIgnoreCase("forlag"))
			orderBy = "Publisher";
		
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
		
		return result;
	}
	
	public List<Comic> getComics(int[] ids)
	{
		List<Comic> list = new ArrayList<Comic>();
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
		return list;
	}
	
	public Comic getComic(int id)
	{
		Comic comic = null;
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
		return comic;
	}
	
	public void deleteComic(int id)
	{
		db.delete("tblBooks", "Id=?", new String[] { Integer.toString(id) });
	}
	
	public List<Group> getGroups()
	{
		List<Group> groups = new ArrayList<Group>();
		Cursor cursor = db.rawQuery("SELECT Id,  Name, Image FROM tblGroups ORDER BY Name", null);
		while (cursor.moveToNext()) {			
			groups.add(new Group(cursor.getInt(0),
					cursor.getString(1),
					cursor.getBlob(2)));
		}
		cursor.close();
		return groups;
	}
	
	public boolean addGroup(String name) {
		boolean isValid = true;
		ContentValues values = new ContentValues();
		values.put("Name", name);

		//Dupecheck
		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM tblGroups WHERE Name = ? COLLATE NOCASE", new String[] { name });
		if (cursor.moveToFirst() && cursor.getInt(0) > 0)
			isValid = false;
		cursor.close();
		
		if (isValid) {
			db.insert("tblGroups", null, values);
		}
		
		return isValid;
	}
		
	public boolean isDuplicateComic(String isbn) {
		boolean duplicate = false;
		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM tblBooks WHERE ISBN = ?", new String[] { isbn });
		if (cursor.moveToFirst())
		{
			int count = cursor.getInt(0);
			duplicate = count > 0;
		}
		cursor.close();
		return duplicate;
	}
}
