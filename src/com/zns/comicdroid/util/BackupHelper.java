package com.zns.comicdroid.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;

import com.google.common.base.Joiner;
import com.zns.comicdroid.Application;
import com.zns.comicdroid.data.DBHelper;

public class BackupHelper extends BackupAgent {
	private final static String BACKUP_KEY_PREFS = "com.zns.comicdroid.backup.prefs";
	private final static String BACKUP_KEY_DB = "com.zns.comicdroid.backup.db";
	
	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
			ParcelFileDescriptor newState) throws IOException {
		
		//-----------------------Shared Preferences--------------------------
		ByteArrayOutputStream bufStream = null;
		DataOutputStream writer = null;		
		try
		{
			bufStream = new ByteArrayOutputStream();
			writer = new DataOutputStream(bufStream);
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			String drive_account = prefs.getString(Application.PREF_DRIVE_ACCOUNT, null);
			boolean drive_authenticated = prefs.getBoolean(Application.PREF_DRIVE_AUTHENTICATED, false);
			String drive_webFolderId = prefs.getString(Application.PREF_DRIVE_WEBFOLDERID, null);
			
			writer.writeUTF(drive_account);
			writer.writeBoolean(drive_authenticated);
			writer.writeUTF(drive_webFolderId);
		
			byte[] buffer = bufStream.toByteArray();
			int len = buffer.length;
			data.writeEntityHeader(BACKUP_KEY_PREFS, len);
			data.writeEntityData(buffer, len);
		}
		finally {
			if (writer != null)
				writer.close();
		}		
		
		//-----------------------Database------------------------------------
		DBHelper db = DBHelper.getHelper(getApplicationContext());
		int dataModifed = db.GetLastModifiedDate();
		boolean performBackup = true;
		
		//Check if backup is necessary
		FileInputStream instream = null;
		DataInputStream in = null;		
		try
		{
			instream = new FileInputStream(oldState.getFileDescriptor());
			in = new DataInputStream(instream);
			
			int lastBackup = in.readInt();			
			
			if (lastBackup >= dataModifed)
				performBackup = false;
			}
		catch (Exception x) {
			//Failed to read state, fall through to backup
		}
		finally {
			if (in != null)
				in.close();
		}
		
		if (performBackup)
		{
			//Write backup data
			bufStream = null;
			writer = null;		
			try
			{					
				Cursor cb = null;
				
				bufStream = new ByteArrayOutputStream();
				writer = new DataOutputStream(bufStream);
				
				try
				{
					cb = db.getCursor("SELECT _id, GroupId, Title, Subtitle, Publisher, Author, Image, ImageUrl, PublishDate, AddedDate, PageCount, IsBorrowed, Borrower, BorrowedDate, ISBN, Issue" +
							" FROM tblBooks ORDER BY _id", null);
					int count = cb.getCount();
					writer.writeInt(count);					
					while (cb.moveToNext())
					{
						writer.writeInt(cb.getInt(0));
						writer.writeUTF(String.format("INSERT INTO tblBooks(_id, GroupId, Title, Subtitle, Publisher, Author, Image, ImageUrl, PublishDate, AddedDate, PageCount, IsBorrowed, Borrower, BorrowedDate, ISBN, Issue)" +
								" VALUES(%d ,%d, %s, %s, %s, %s, %s, %s, %d, %d, %d, %d, %s, %d, %s, %d);", 
								cb.getInt(0),
								cb.getInt(1),
								dbString(cb.getString(2)),
								dbString(cb.getString(3)),
								dbString(cb.getString(4)),
								dbString(cb.getString(5)),
								dbString(cb.getString(6)),
								dbString(cb.getString(7)),
								cb.getInt(8),
								cb.getInt(9),
								cb.getInt(10),
								cb.getInt(11),
								dbString(cb.getString(12)),
								cb.getInt(13),
								dbString(cb.getString(14)),
								cb.getInt(15)));
					}
				}
				finally {
					cb.close();
				}
				
				try
				{
					cb = db.getCursor("SELECT _id, Name, Image, ImageUrl, BookCount" +
							" FROM tblGroups ORDER BY _id", null);
					writer.writeInt(cb.getCount());
					while (cb.moveToNext())
					{
						writer.writeUTF(String.format("INSERT INTO tblGroups(_id, Name, Image, ImageUrl, BookCount)" +
								" VALUES(%d, %s, %s, %s, %d);", 
								cb.getInt(0),
								dbString(cb.getString(1)),
								dbString(cb.getString(2)),
								dbString(cb.getString(3)),
								cb.getInt(4)));
					}
				}
				finally {
					cb.close();
				}
				
				byte[] buffer = bufStream.toByteArray();
				int len = buffer.length;
				data.writeEntityHeader(BACKUP_KEY_DB, len);
				data.writeEntityData(buffer, len);
			}
			finally {
				if (writer != null)
					writer.close();
			}
		}
		
		//Write newstate
		FileOutputStream outstream = null;
		DataOutputStream out = null;
		try
		{
			outstream = new FileOutputStream(newState.getFileDescriptor());
			out = new DataOutputStream(outstream);	
			out.writeInt(dataModifed);		
		}
		finally {
			if (out != null)
				out.close();
		}
	}

	@Override
	public void onRestore(BackupDataInput data, int appVersionCode,
			ParcelFileDescriptor newState) throws IOException {
		
		DBHelper db = DBHelper.getHelper(getApplicationContext());
		
		while (data.readNextHeader()) {
			String key = data.getKey();
			int dataSize = data.getDataSize();
	        
			if (key.equals(BACKUP_KEY_PREFS) && dataSize > 0) {				
				byte[] dataBuf = new byte[dataSize];
				data.readEntityData(dataBuf, 0, dataSize);
				
				ByteArrayInputStream baStream = null;
				DataInputStream in = null;				
				try
				{
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					Editor prefsEdit = prefs.edit();
					
					baStream = new ByteArrayInputStream(dataBuf);
					in = new DataInputStream(baStream);
					
					String drive_account = in.readUTF();
					boolean drive_enabled = in.readBoolean();
					String drive_webFolderId = in.readUTF();
					
					if (drive_account != null)
						prefsEdit.putString(Application.PREF_DRIVE_ACCOUNT, drive_account);
					prefsEdit.putBoolean(Application.PREF_DRIVE_AUTHENTICATED, drive_enabled);
					if (drive_webFolderId != null)
						prefsEdit.putString(Application.PREF_DRIVE_WEBFOLDERID, drive_webFolderId);
					prefsEdit.commit();
				}
				finally {
					if (in != null)
						in.close();
				}				
			}
			else if (key.equals(BACKUP_KEY_DB) && dataSize > 0) {
				byte[] dataBuf = new byte[dataSize];
				data.readEntityData(dataBuf, 0, dataSize);
				
				ByteArrayInputStream baStream = null;
				DataInputStream in = null;
				
				try
				{
					baStream = new ByteArrayInputStream(dataBuf);
					in = new DataInputStream(baStream);
					 
					//Comics
					List<Integer> addedComics = new ArrayList<Integer>();
					int rows = in.readInt();
					for (int i = 0; i < rows; i++)
					{
						int id = in.readInt();
						String sql = in.readUTF();
						if (sql != null && !sql.equals("")) {
							try {
								db.execSQL(sql);
								addedComics.add(id);
							}
							catch (Exception x) {
								//Failed to restore comic...
							}
						}
					}
					
					//Groups
					rows = in.readInt();
					for (int i = 0; i < rows; i++)
					{
						String sql = in.readUTF();
						if (sql != null && !sql.equals("")) {
							try {
								db.execSQL(sql);
							}
							catch (Exception x) {
								//Failed to restore group...
							}
						}
					}
					
					//Fix images
					Cursor cb = null;
					String imageDirectory = getExternalFilesDir(null).toString();					
					try 
					{
						String ids = Joiner.on(',').join(addedComics);
						cb = db.getCursor("SELECT _id, Image, ImageUrl FROM tblBooks WHERE _id IN (" + ids  + ")", null);
						while (cb.moveToNext()) {
							String filePath = cb.getString(1);
							if (filePath.length() > 0) {
								File file = new File(filePath);
								if (file.exists())
									continue;
							}
							String url = cb.getString(2);
							if (url.length() > 0) {
								try
								{
									filePath = ImageHandler.storeImage(new URL(url), imageDirectory);
									ContentValues val = new ContentValues();
									val.put("Image", filePath);
									db.update("tblBooks", val, "_id=?", new String[] { Integer.toString(cb.getInt(0)) });
								}
								catch (Exception x)
								{
									//Unable to save image to disk
								}
							}
						}
					}
					finally
					{
						cb.close();
					}
				}
				finally {
					if (in != null)
						in.close();
				}
			}
		}
		
		//Write newstate
		FileOutputStream outstream = null;
		DataOutputStream out = null;
		try
		{
			outstream = new FileOutputStream(newState.getFileDescriptor());
			out = new DataOutputStream(outstream);	
			out.writeInt((int)(System.currentTimeMillis() / 1000L));
		}
		finally {
			if (out != null)
				out.close();
		}		
	}
	
	private String dbString(String val) {
		if (val != null) {
			val = val.replaceAll("'", "''");
			return "'" + val + "'";
		}
		return "null";
	}
}
