package com.zns.comicdroid.data;

import android.content.Context;
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
}
