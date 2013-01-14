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
	
	private static final int DB_VERSION = 	22;
	private static final String DB_NAME = 	"ComicDroid.db";
	
	private static DBHelper instance;
    private SQLiteDatabase db;
    
    private DBHelper(Context context) {
    	super(context, DB_NAME, null, DB_VERSION);
        if (db == null)
        	db = getWritableDatabase();    	
    }

    public static synchronized DBHelper getHelper(Context context) {
    	if (instance == null)
    		instance = new DBHelper(context.getApplicationContext());
    	return instance;
    }
    
    public void finalize() throws Throwable {
    	if (db != null)
    		db.close();
    	db = null;
        super.finalize();
    }
    
	@Override
	public void onCreate(SQLiteDatabase db) {
		String tblBooks = "CREATE TABLE tblBooks (" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"GroupId INTEGER DEFAULT 0," +
				"Title TEXT," +
				"Subtitle TEXT," +
				"Publisher TEXT," +
				"Author TEXT," +
				"Image TEXT," +
				"ImageUrl TEXT," +
				"PublishDate INTEGER," +
				"AddedDate INTEGER," +
				"PageCount INTEGER," +
				"IsBorrowed INTEGER DEFAULT 0," +
				"Borrower TEXT," +
				"BorrowedDate INTEGER," +
				"ISBN TEXT," +
				"Issue INTEGER" +
				")";
		
		String tblGroups = "CREATE TABLE tblGroups (" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"Name TEXT," +
				"Image TEXT," +
				"ImageUrl TEXT," +
				"BookCount INTEGER DEFAULT 0" +
				")";
		
		db.execSQL(tblBooks);
		db.execSQL(tblGroups);
		
		String triggerGroupId = "CREATE TRIGGER update_boook_groupid_image AFTER UPDATE OF GroupId ON tblBooks " +
				"WHEN new.Issue = 1 " +
				"BEGIN " +
				"UPDATE tblGroups SET Image = new.Image, ImageUrl = new.ImageUrl WHERE new.GroupId <> 0 AND _id = new.GroupId; " +
				"END;";
		
		String triggerGroupId4 = "CREATE TRIGGER insert_boook_groupid_image AFTER INSERT ON tblBooks " +
				"WHEN new.Issue = 1 " +
				"BEGIN " +
				"UPDATE tblGroups SET Image = new.Image, ImageUrl = new.ImageUrl WHERE new.GroupId <> 0 AND _id = new.GroupId; " +
				"END;";
		
		String triggerGroupId2 = "CREATE TRIGGER update_book_groupid_count AFTER UPDATE OF GroupId ON tblBooks " +
				"WHEN new.GroupId <> old.GroupId " +
				"BEGIN " +
				"UPDATE tblGroups SET BookCount=BookCount+1 WHERE new.GroupId <> 0 AND _id = new.GroupId; " +
				"UPDATE tblGroups SET BookCount=BookCount-1 WHERE old.GroupId <> 0 AND _id = old.GroupId; " +
				"END;";
		
		String triggerGroupId3 = "CREATE TRIGGER insert_book_groupid_count AFTER INSERT ON tblBooks " +
				"WHEN new.GroupId > 0 " +
				"BEGIN " +
				"UPDATE tblGroups SET BookCount=BookCount+1 WHERE _id = new.GroupId; " +
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
		/*for (int i = 0; i < 500; i++) {
			db.execSQL("INSERT INTO tblBooks (Title,Subtitle,Publisher,Author,Image) SELECT Title,Subtitle,Publisher,Author,Image FROM tblBooks LIMIT 1");
		}*/		
	}
	
    @Override
    public synchronized void close() {
    	if (db != null)
    		db.close();
    	db = null;
    	super.close();
    }
	
    @Override
    public synchronized SQLiteDatabase getWritableDatabase () {
    	if (db == null)
    		db = super.getWritableDatabase();
    	return db;
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
    
    public long insert(String table, ContentValues values) {
    	return db.insert(table, null, values);
    }
    
	public int GetDateStamp(String strDate) 
			throws ParseException
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
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
		values.put("ImageUrl", comic.getImageUrl());
		
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
		
		Cursor cursor = db.rawQuery("SELECT _id, Title, Subtitle, Author, Publisher, PublishDate, " +
				"AddedDate, PageCount, IsBorrowed, Borrower, Image, ISBN, Issue, GroupId, ImageUrl, BorrowedDate FROM tblBooks ORDER BY " + orderBy, null);
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
					cursor.getString(10),
					cursor.getString(11),
					cursor.getInt(12),
					cursor.getInt(13),
					cursor.getString(14),
					cursor.getInt(15));
			result.add(comic);
		}
		cursor.close();
		
		return result;
	}
	
	public List<Comic> getComics(int[] ids)
	{
		List<Comic> list = new ArrayList<Comic>();
		Cursor cursor = db.rawQuery("SELECT _id, Title, Subtitle, Author, Publisher, PublishDate, " +
				"AddedDate, PageCount, IsBorrowed, Borrower, Image, ISBN, Issue, GroupId, ImageUrl, BorrowedDate FROM tblBooks WHERE _id IN (" +
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
					cursor.getString(10),
					cursor.getString(11),
					cursor.getInt(12),
					cursor.getInt(13),
					cursor.getString(14),
					cursor.getInt(15));
			list.add(comic);
		}
		cursor.close();
		return list;
	}
	
	public List<Comic> getComics(int groupId)
	{
		List<Comic> list = new ArrayList<Comic>();
		Cursor cursor = db.rawQuery("SELECT _id, Title, Subtitle, Author, Publisher, PublishDate, " +
				"AddedDate, PageCount, IsBorrowed, Borrower, Image, ISBN, Issue, GroupId, ImageUrl, BorrowedDate FROM tblBooks " +
				"WHERE GroupId = ? " +
				"ORDER BY Issue", new String[] { Integer.toString(groupId) });
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
					cursor.getString(10),
					cursor.getString(11),
					cursor.getInt(12),
					cursor.getInt(13),
					cursor.getString(14),
					cursor.getInt(15));
			list.add(comic);
		}
		cursor.close();
		return list;
	}
	
	public List<Comic> getBorrowed()
	{
		List<Comic> list = new ArrayList<Comic>();
		Cursor cursor = db.rawQuery("SELECT _id, Title, Subtitle, Author, Publisher, PublishDate, " +
				"AddedDate, PageCount, IsBorrowed, Borrower, Image, ISBN, Issue, GroupId, ImageUrl, BorrowedDate FROM tblBooks " +
				"WHERE IsBorrowed = 1 " +
				"ORDER BY Borrower, BorrowedDate", null);
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
					cursor.getString(10),
					cursor.getString(11),
					cursor.getInt(12),
					cursor.getInt(13),
					cursor.getString(14),
					cursor.getInt(15));
			list.add(comic);
		}
		cursor.close();
		return list;
	}
	
	public Comic getComic(int id)
	{
		Comic comic = null;
		Cursor cursor = db.rawQuery("SELECT _id, Title, Subtitle, Author, Publisher, PublishDate, " +
				"AddedDate, PageCount, IsBorrowed, Borrower, Image, ISBN, Issue, GroupId, ImageUrl, BorrowedDate FROM tblBooks WHERE _id = ?", new String[] { Integer.toString(id) });
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
					cursor.getString(10),
					cursor.getString(11),
					cursor.getInt(12),
					cursor.getInt(13),
					cursor.getString(14),
					cursor.getInt(15));					
		}
		cursor.close();		
		return comic;
	}
	
	public Comic getComic(String isbn)
	{
		Comic comic = null;
		Cursor cursor = db.rawQuery("SELECT _id, Title, Subtitle, Author, Publisher, PublishDate, " +
				"AddedDate, PageCount, IsBorrowed, Borrower, Image, ISBN, Issue, GroupId, ImageUrl, BorrowedDate FROM tblBooks WHERE ISBN = ?", new String[] { isbn });
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
					cursor.getString(10),
					cursor.getString(11),
					cursor.getInt(12),
					cursor.getInt(13),
					cursor.getString(14),
					cursor.getInt(15));					
		}
		cursor.close();		
		return comic;
	}
	
	public void deleteComic(int id)
	{
		db.delete("tblBooks", "_id=?", new String[] { Integer.toString(id) });
	}
	
	public void setComicBorrowed(int comicId, String borrower) {
		setComicBorrowed(new int[] { comicId }, borrower);
	}
	
	public void setComicBorrowed(int[] comicId, String borrower) {
		ContentValues values = new ContentValues();
		values.put("Borrower", borrower);
		if (borrower == null || borrower.equals("")) {
			values.put("IsBorrowed", false);
			values.put("BorrowedDate", "");
		}
		else {
			values.put("IsBorrowed", true);
			values.put("BorrowedDate", (int)(System.currentTimeMillis() / 1000L));			
		}
		
		StringBuilder sbWhere = new StringBuilder("_id IN (");
		String[] ids = new String[comicId.length];
		int i = 0;
		for (int id : comicId) {
			sbWhere.append("?,");
			ids[i] = Integer.toString(id);
			i++;
		}
		sbWhere.setLength(sbWhere.length() - 1);
		sbWhere.append(")");
		
		db.update("tblBooks", values, sbWhere.toString(), ids);
	}

	public void setComicReturned(int[] comicId) {
		ContentValues values = new ContentValues();
		values.put("Borrower", "");
		values.put("IsBorrowed", false);
		values.putNull("Borrower");
		
		StringBuilder sbWhere = new StringBuilder("_id IN (");
		String[] ids = new String[comicId.length];
		int i = 0;
		for (int id : comicId) {
			sbWhere.append("?,");
			ids[i] = Integer.toString(id);
			i++;
		}
		sbWhere.setLength(sbWhere.length() - 1);
		sbWhere.append(")");
		
		db.update("tblBooks", values, sbWhere.toString(), ids);
	}
	
	public void renameAuthor(String oldName, String newName) {
		ContentValues values = new ContentValues();
		values.put("Author", newName);
		update("tblBooks", values, "Author=?", new String[] { oldName });
	}
	
	public void renamePublisher(String oldName, String newName) {
		ContentValues values = new ContentValues();
		values.put("Publisher", newName);
		update("tblBooks", values, "Publisher=?", new String[] { oldName });
	}
	
	public List<Group> getGroups()
	{
		List<Group> groups = new ArrayList<Group>();
		Cursor cursor = db.rawQuery("SELECT _id,  Name, Image FROM tblGroups ORDER BY Name", null);
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
