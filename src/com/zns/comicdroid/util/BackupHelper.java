package com.zns.comicdroid.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.database.Cursor;
import android.os.ParcelFileDescriptor;

import com.zns.comicdroid.data.DBHelper;

public class BackupHelper extends BackupAgent {
	private final static String BACKUP_KEY_DB = "com.zns.comicdroid.backup.db";
	
	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
			ParcelFileDescriptor newState) throws IOException {
		
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
			ByteArrayOutputStream bufStream = null;
			DataOutputStream writer = null;		
			try
			{					
				Cursor cb = null;
				
				bufStream = new ByteArrayOutputStream();
				writer = new DataOutputStream(bufStream);
				
				try
				{
					cb = db.getCursor("SELECT _id, GroupId, Title, Subtitle, Publisher, Author, Image, ImageUrl, PublishDate, AddedDate, PageCount, IsBorrowed, Borrower, BorrowedDate, ISBN, Issue" +
							" FROM tblBooks ORDER BY _id", null);					
					writer.writeInt(cb.getCount());					
					while (cb.moveToNext())
					{
						writer.writeUTF(String.format("INSERT INTO tblBooks(_id, GroupId, Title, Subtitle, Publisher, Author, Image, ImageUrl, PublishDate, AddedDate, PageCount, IsBorrowed, Borrower, BorrowedDate, ISBN, Issue)" +
								" VALUES(%d ,%d,'%s','%s', '%s', '%s', '%s', '%s', %d, %d, %d, %d, '%s', %d, '%s', %d);", 
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
								" VALUES(%d '%s', '%s', '%s', %d);", 
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
	        
			if (key.equals(BACKUP_KEY_DB) && dataSize > 0) {
				byte[] dataBuf = new byte[dataSize];
				data.readEntityData(dataBuf, 0, dataSize);
				
				ByteArrayInputStream baStream = null;
				DataInputStream in = null;
				
				try
				{
					baStream = new ByteArrayInputStream(dataBuf);
					in = new DataInputStream(baStream);
					 
					//Comics
					int rows = in.readInt();
					for (int i = 0; i < rows; i++)
					{
						String sql = in.readUTF();
						if (sql != null && !sql.equals("")) {
							try {
								db.execSQL(sql);
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
		}
		return val;
	}
}
