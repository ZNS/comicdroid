package com.zns.comicdroid.data;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;

public class DBHelper extends SQLiteOpenHelper {
	
	private static final int DB_VERSION = 	12;
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
				"Illustrator TEXT," +
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
				"BookCount INTEGER DEFAULT 0, " +
				"TotalBookCount INTEGER DEFAULT 0, " +
				"IsWatched INTEGER DEFAULT 0, " +
				"IsFinished INTEGER DEFAULT 0, " +
				"IsComplete INTEGER DEFAULT 0" +
				")";
		
		String tblMeta = "CREATE TABLE tblMeta (" +
				"_id INTEGER PRIMARY KEY," +
				"LastModified INTEGER DEFAULT 0" +
				")";
				
		db.execSQL(tblBooks);
		db.execSQL(tblGroups);
		db.execSQL(tblMeta);
		db.execSQL("INSERT INTO tblMeta(_id,LastModified) VALUES(1, strftime('%s','now'))");
		
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
		
		String triggerModUpd  = "CREATE TRIGGER update_book AFTER UPDATE ON tblBooks " +
				"BEGIN " +
				"UPDATE tblMeta SET LastModified = strftime('%s','now'); " +
				"END;";
		String triggerModIns  = "CREATE TRIGGER insert_book AFTER INSERT ON tblBooks " +
				"BEGIN " +
				"UPDATE tblMeta SET LastModified = strftime('%s','now'); " +
				"END;";
		String triggerModDel  = "CREATE TRIGGER delete_book AFTER DELETE ON tblBooks " +
				"BEGIN " +
				"UPDATE tblGroups SET BookCount=BookCount-1 WHERE BookCount > 0 AND _id = old.GroupId; " +
				"UPDATE tblMeta SET LastModified = strftime('%s','now'); " +
				"END;";
		
		db.execSQL(triggerGroupId);
		db.execSQL(triggerGroupId2);
		db.execSQL(triggerGroupId3);
		db.execSQL(triggerGroupId4);
		db.execSQL(triggerModUpd);
		db.execSQL(triggerModIns);
		db.execSQL(triggerModDel);		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//Just to make sure
		if (oldVersion >= newVersion)
			return;
		
		if (oldVersion < 10) {
			db.execSQL("ALTER TABLE tblGroups ADD COLUMN IsWatched INTEGER DEFAULT 0");
			db.execSQL("ALTER TABLE tblGroups ADD COLUMN IsComplete INTEGER DEFAULT 0");			
			db.execSQL("DROP TRIGGER IF EXISTS delete_book; " +
			"CREATE TRIGGER delete_book AFTER DELETE ON tblBooks " +
			"BEGIN " +
			"UPDATE tblGroups SET BookCount=BookCount-1 WHERE BookCount > 0 AND _id = old.GroupId; " +
			"UPDATE tblMeta SET LastModified = strftime('%s','now'); " +
			"END;");
		}
		if (oldVersion < 11) {
			db.execSQL("ALTER TABLE tblGroups ADD COLUMN IsFinished INTEGER DEFAULT 0");
		}
		if (oldVersion < 12) {
			db.execSQL("ALTER TABLE tblGroups ADD COLUMN TotalBookCount INTEGER DEFAULT 0");
		}
	}
	
	/*private void generateData() {
		for (int i = 0; i < 20; i++) {
			db.execSQL("INSERT INTO tblGroups(Name) VALUES('Group " + i + "')");
		}
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i = 0; i < 100; i++) {
			//Get group
			int group = (int)(Math.random() * 20);
			int issue = 1;
			if (group > 0) {
				issue = map.containsKey(group) ? map.get(group) : 0;
				map.put(group, issue + 1);
			}
			db.execSQL(String.format("INSERT INTO tblBooks(GroupId, Title, Author, Issue) VALUES(%d, '%s', '%s', %d)", group, "Comic " + i, "Author " + i, issue + 1));
		}
	}*/
	
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
    
    public void execSQL(String sql)
    {
    	db.execSQL(sql);
    }
    
	public int GetDateStamp(String strDate) 
			throws ParseException
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = dateFormat.parse(strDate);
		return (int)(date.getTime() / 1000L);
	}
	
	public int GetCurrentTimeStamp() {
		return (int)(System.currentTimeMillis() / 1000L);
	}
	
	public int storeComic(Comic comic)
	{
		ContentValues values = new ContentValues();
		
		String title = comic.getTitle().trim();
		if (title.toLowerCase(Locale.ENGLISH).startsWith("the ")) {
			title = title.substring(4) + ", The";
		}
		
		values.put("GroupId", comic.getGroupId());
		values.put("Title", title);
		values.put("Subtitle", comic.getSubTitle());
		values.put("Publisher", comic.getPublisher());
		values.put("Author", comic.getAuthor());
		values.put("Illustrator", comic.getIllustrator());
		values.put("PublishDate", comic.getPublishDateTimestamp());
		values.put("AddedDate", (int)(System.currentTimeMillis() / 1000L));
		values.put("PageCount", comic.getPageCount());
		values.put("IsBorrowed", comic.getIsBorrowed() ? 1 : 0);
		values.put("Borrower", comic.getBorrower());
		values.put("Image", comic.getImage());
		values.put("ISBN", comic.getISBN());
		values.put("Issue", comic.getIssue());
		values.put("ImageUrl", comic.getImageUrl());
		
		return (int)db.insert("tblBooks", null, values);
	}
	
	public List<Comic> getComics(String order)
	{
		List<Comic> result = new ArrayList<Comic>();
		String orderBy = "Title";
		if (order.equalsIgnoreCase("forfattare"))
			orderBy = "Author";
		else if (order.equalsIgnoreCase("forlag"))
			orderBy = "Publisher";
		
		Cursor cursor = db.rawQuery("SELECT _id, Title, Subtitle, Author, Illustrator, Publisher, PublishDate, " +
				"AddedDate, PageCount, IsBorrowed, Borrower, Image, ISBN, Issue, GroupId, ImageUrl, BorrowedDate FROM tblBooks ORDER BY " + orderBy, null);
		while (cursor.moveToNext())
		{
			Comic comic = new Comic(cursor.getInt(0),
					cursor.getString(1),
					cursor.getString(2),
					cursor.getString(3),
					cursor.getString(4),
					cursor.getString(5),
					cursor.getInt(6),
					cursor.getInt(7),
					cursor.getInt(8),
					cursor.getInt(9),
					cursor.getString(10),
					cursor.getString(11),
					cursor.getString(12),
					cursor.getInt(13),
					cursor.getInt(14),
					cursor.getString(15),
					cursor.getInt(16));
			result.add(comic);
		}
		cursor.close();
		
		return result;
	}
	
	public List<Comic> getComics(int[] ids)
	{
		List<Comic> list = new ArrayList<Comic>();
		Cursor cursor = db.rawQuery("SELECT _id, Title, Subtitle, Author, Illustrator, Publisher, PublishDate, " +
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
					cursor.getString(5),
					cursor.getInt(6),
					cursor.getInt(7),
					cursor.getInt(8),
					cursor.getInt(9),
					cursor.getString(10),
					cursor.getString(11),
					cursor.getString(12),
					cursor.getInt(13),
					cursor.getInt(14),
					cursor.getString(15),
					cursor.getInt(16));
			list.add(comic);
		}
		cursor.close();
		return list;
	}
	
	public List<Comic> getComics(int groupId)
	{
		List<Comic> list = new ArrayList<Comic>();
		Cursor cursor = db.rawQuery("SELECT _id, Title, Subtitle, Author, Illustrator, Publisher, PublishDate, " +
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
					cursor.getString(5),
					cursor.getInt(6),
					cursor.getInt(7),
					cursor.getInt(8),
					cursor.getInt(9),
					cursor.getString(10),
					cursor.getString(11),
					cursor.getString(12),
					cursor.getInt(13),
					cursor.getInt(14),
					cursor.getString(15),
					cursor.getInt(16));
			list.add(comic);
		}
		cursor.close();
		return list;
	}
	
	public List<Comic> getBorrowed()
	{
		List<Comic> list = new ArrayList<Comic>();
		Cursor cursor = db.rawQuery("SELECT _id, Title, Subtitle, Author, Illustrator, Publisher, PublishDate, " +
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
					cursor.getString(5),
					cursor.getInt(6),
					cursor.getInt(7),
					cursor.getInt(8),
					cursor.getInt(9),
					cursor.getString(10),
					cursor.getString(11),
					cursor.getString(12),
					cursor.getInt(13),
					cursor.getInt(14),
					cursor.getString(15),
					cursor.getInt(16));
			list.add(comic);
		}
		cursor.close();
		return list;
	}
	
	public Comic getComic(int id)
	{
		Comic comic = null;
		Cursor cursor = db.rawQuery("SELECT _id, Title, Subtitle, Author, Illustrator, Publisher, PublishDate, " +
				"AddedDate, PageCount, IsBorrowed, Borrower, Image, ISBN, Issue, GroupId, ImageUrl, BorrowedDate FROM tblBooks WHERE _id = ?", new String[] { Integer.toString(id) });
		if (cursor.moveToNext())
		{
			comic = new Comic(cursor.getInt(0),
					cursor.getString(1),
					cursor.getString(2),
					cursor.getString(3),
					cursor.getString(4),
					cursor.getString(5),
					cursor.getInt(6),
					cursor.getInt(7),
					cursor.getInt(8),
					cursor.getInt(9),
					cursor.getString(10),
					cursor.getString(11),
					cursor.getString(12),
					cursor.getInt(13),
					cursor.getInt(14),
					cursor.getString(15),
					cursor.getInt(16));					
		}
		cursor.close();		
		return comic;
	}
	
	public Comic getComic(String isbn)
	{
		Comic comic = null;
		Cursor cursor = db.rawQuery("SELECT _id, Title, Subtitle, Author, Illustrator, Publisher, PublishDate, " +
				"AddedDate, PageCount, IsBorrowed, Borrower, Image, ISBN, Issue, GroupId, ImageUrl, BorrowedDate FROM tblBooks WHERE ISBN = ?", new String[] { isbn });
		if (cursor.moveToNext())
		{
			comic = new Comic(cursor.getInt(0),
					cursor.getString(1),
					cursor.getString(2),
					cursor.getString(3),
					cursor.getString(4),
					cursor.getString(5),
					cursor.getInt(6),
					cursor.getInt(7),
					cursor.getInt(8),
					cursor.getInt(9),
					cursor.getString(10),
					cursor.getString(11),
					cursor.getString(12),
					cursor.getInt(13),
					cursor.getInt(14),
					cursor.getString(15),
					cursor.getInt(16));					
		}
		cursor.close();		
		return comic;
	}
	
	public void deleteComic(int id)
	{
		Comic comic = getComic(id);
		try
		{
			if (comic.getImage() != null && comic.getImage().length() > 0)
			{
				File image = new File(comic.getImage());
				if (image.exists())
					image.delete();
			}
		}
		catch (Exception e) {
			//Doesn't matter too much if image can't be deleted. It will be delete when app is uninstalled.
		}
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
	
	public void renameIllustrator(String oldName, String newName) {
		ContentValues values = new ContentValues();
		values.put("Illustrator", newName);
		update("tblBooks", values, "Illustrator=?", new String[] { oldName });
	}
	
	public void renamePublisher(String oldName, String newName) {
		ContentValues values = new ContentValues();
		values.put("Publisher", newName);
		update("tblBooks", values, "Publisher=?", new String[] { oldName });
	}
	
	public void renameGroup(int id, String newName) {
		ContentValues values = new ContentValues();
		values.put("Name", newName);
		update("tblGroups", values, "_id=?", new String[] { Integer.toString(id) });
	}
	
	public Group getGroup(int id)
	{
		Group group = null;
		Cursor cursor = db.rawQuery("SELECT _id,  Name, Image, BookCount, TotalBookCount, IsWatched, IsFinished, IsComplete FROM tblGroups WHERE _id=? ORDER BY Name", new String[] { Integer.toString(id) });
		if (cursor.moveToNext()) {			
			group = new Group(cursor.getInt(0),
					cursor.getString(1),
					cursor.getString(2),
					cursor.getInt(3),
					cursor.getInt(4),
					cursor.getInt(5),
					cursor.getInt(6),
					cursor.getInt(7));
		}
		cursor.close();
		return group;		
	}
	
	private List<Group> getGroupsBySql(String sql, String[] selectionArgs)
	{
		List<Group> groups = new ArrayList<Group>();
		Cursor cursor = db.rawQuery(sql, selectionArgs);
		while (cursor.moveToNext()) {			
			groups.add(new Group(cursor.getInt(0),
					cursor.getString(1),
					cursor.getString(2),
					cursor.getInt(3),
					cursor.getInt(4),
					cursor.getInt(5),
					cursor.getInt(6),
					cursor.getInt(7)));
		}
		cursor.close();
		return groups;		
	}
	
	public List<Group> getGroups()
	{
		return getGroupsBySql("SELECT _id,  Name, Image, BookCount, TotalBookCount, IsWatched, IsFinished, IsComplete FROM tblGroups ORDER BY Name", null);
	}
	
	public List<Group> getGroupsWatched()
	{
		return getGroupsBySql("SELECT _id,  Name, Image, BookCount, TotalBookCount, IsWatched, IsFinished, IsComplete FROM tblGroups WHERE IsWatched=? ORDER BY Name", new String[] { "1" });
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
	
	public void deleteGroup(int id, boolean deleteBooks)
	{
		if (deleteBooks)
		{
			Cursor cursor = db.rawQuery("SELECT _id FROM tblBooks WHERE GroupId=?", new String[] { Integer.toString(id) });
			while (cursor.moveToNext()) {
				int bookId = cursor.getInt(0);
				deleteComic(bookId);
			}
		}
		else 
		{
			ContentValues values = new ContentValues();
			values.put("GroupId", 0);
			update("tblBooks", values, "GroupId=?", new String[] { Integer.toString(id) });
		}
		db.delete("tblGroups", "_id=?", new String[] { Integer.toString(id) });
	}
	
	public void setGroupIsWatched(int groupId, boolean watched)
	{
		ContentValues values = new ContentValues();
		values.put("IsWatched", watched ? 1 : 0);
		db.update("tblGroups", values, "_id=?", new String[] { Integer.toString(groupId) });
	}
	
	public void setGroupIsFinished(int groupId, boolean finished)
	{
		ContentValues values = new ContentValues();
		values.put("IsFinished", finished ? 1 : 0);
		db.update("tblGroups", values, "_id=?", new String[] { Integer.toString(groupId) });
	}
	
	public void setGroupIsComplete(int groupId, boolean complete)
	{
		ContentValues values = new ContentValues();
		values.put("IsComplete", complete ? 1 : 0);
		db.update("tblGroups", values, "_id=?", new String[] { Integer.toString(groupId) });
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
	
	public List<String> getAuthors(String[] names)
	{
		List<String> foundNames = new ArrayList<String>();
		for (int i = 0; i < names.length; i++)
			names[i] = dbString(names[i]);
		Cursor cursor = db.rawQuery("SELECT Author FROM tblBooks WHERE Author IN('" + Joiner.on("','").join(names) + "')", null);
		while (cursor.moveToNext()) 
		{
			foundNames.add(cursor.getString(0));
		}
		return foundNames;
	}

	public List<String> getIllustrators(String[] names)
	{
		List<String> foundNames = new ArrayList<String>();
		for (int i = 0; i < names.length; i++)
			names[i] = dbString(names[i]);
		Cursor cursor = db.rawQuery("SELECT Illustrator FROM tblBooks WHERE Illustrator IN('" + Joiner.on("','").join(names) + "')", null);
		while (cursor.moveToNext()) 
		{
			foundNames.add(cursor.getString(0));
		}
		return foundNames;
	}
	
	public int GetLastModifiedDate()
	{
		int stamp = 0;
		Cursor cursor = db.rawQuery("SELECT LastModified FROM tblMeta", null);
		if (cursor.moveToFirst())
			stamp = cursor.getInt(0);
		cursor.close();
		return stamp;
	}
	
	private String dbString(String val) {
		if (val == null)
			return "null";
		return val.replaceAll("'", "''");
	}
}
