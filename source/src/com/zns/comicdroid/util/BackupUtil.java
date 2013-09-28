/*******************************************************************************
 * Copyright (c) 2013 Ulrik Andersson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Ulrik Andersson - initial API and implementation
 ******************************************************************************/
package com.zns.comicdroid.util;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;

import com.google.common.base.Joiner;
import com.zns.comicdroid.R;
import com.zns.comicdroid.data.DBHelper;
import com.zns.comicdroid.service.ProgressResult;

import de.greenrobot.event.EventBus;

public class BackupUtil {
	private static WifiLock wifiLock = null;
	
	public static void extendWifiLock() {
		if (wifiLock != null) {
			wifiLock.acquire();
		}
	}
	
	public static void releaseWifiLock() {
		if (wifiLock != null && wifiLock.isHeld()) {
			wifiLock.release();
		}
	}
	
	public static boolean acquireWifiLock(Context context)
	{
		//We only allow backup on wifi connection. Make sure we are connected and if so lock the wifi connection
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifi.isConnected()) {
			if (wifiLock == null)
			{
		        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		        wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL , "com.zns.comicdroid.wifilock");
		        wifiLock.setReferenceCounted(true);
		        wifiLock.acquire();
			}
			else {
				wifiLock.acquire();
			}
	        return true;
		}
		return false;
	}
	
	public static int BackupDataToFile(DBHelper db, File fileSql, String imagePath)
	throws IOException {
		DataOutputStream writer = null;
		int byteCount = 0;
		try
		{
			Cursor cb = null;			
			writer = new DataOutputStream(new FileOutputStream(fileSql));
			
			try //Destroy cursor
			{
				cb = db.getCursor("SELECT _id, GroupId, Title, Subtitle, Publisher, Author, Illustrator, Image, ImageUrl, PublishDate, AddedDate, PageCount, IsBorrowed, Borrower, BorrowedDate, ISBN, Issue, Issues, IsRead, Rating" +
						" FROM tblBooks ORDER BY _id", null);
				int count = cb.getCount();

				if (count <= 0) {
					//There is nothing to backup, return
					return 0;
				}
				
				writer.writeInt(count);
				while (cb.moveToNext())
				{
					try //This is just to make sure one failed insert doesn't abort everything
					{
						writer.writeInt(cb.getInt(0));
						writer.writeUTF(String.format("INSERT OR REPLACE INTO tblBooks(_id, GroupId, Title, Subtitle, Publisher, Author, Illustrator, Image, ImageUrl, PublishDate, AddedDate, PageCount, IsBorrowed, Borrower, BorrowedDate, ISBN, Issue, Issues, IsRead, Rating)" +
								" VALUES(%d ,%d, %s, %s, %s, %s, %s, %s, %s, %d, %d, %d, %d, %s, %d, %s, %d, %s, %d, %d);", 
								cb.getInt(0),
								cb.getInt(1),
								dbString(cb.getString(2)),
								dbString(cb.getString(3)),
								dbString(cb.getString(4)),
								dbString(cb.getString(5)),
								dbString(cb.getString(6)),
								dbString(cb.getString(7)),
								dbString(cb.getString(8)),
								cb.getInt(9),
								cb.getInt(10),
								cb.getInt(11),
								cb.getInt(12),
								dbString(cb.getString(13)),
								cb.getInt(14),
								dbString(cb.getString(15)),
								cb.getInt(16),
								dbString(cb.getString(17)),
								cb.getInt(18),
								cb.getInt(19)));
						//Image
						String fileName = cb.getString(7);
						String imgUrl = cb.getString(8);
						if ((imgUrl == null || imgUrl.length() == 0) && (fileName != null && fileName.length() > 0))
						{
							Bitmap bmp = BitmapFactory.decodeFile(imagePath.concat(fileName));
							if (bmp != null)
							{
								ByteArrayOutputStream stream = new ByteArrayOutputStream();
								bmp.compress(CompressFormat.JPEG, 100, stream);
								byte[] imgData = stream.toByteArray();
								//Write image size
								writer.writeInt(imgData.length);
								//Write file name
								writer.writeUTF(fileName);
								//Write image data							
								writer.write(imgData);
							}
							else {
								writer.writeInt(0);
							}
						}
						else
						{
							writer.writeInt(0);
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			finally {
				cb.close();
			}

			try
			{
				cb = db.getCursor("SELECT _id, Name, BookCount, TotalBookCount, IsWatched, IsFinished, IsComplete, ImageComicId" +
						" FROM tblGroups ORDER BY _id", null);
				writer.writeInt(cb.getCount());
				while (cb.moveToNext())
				{
					writer.writeUTF(String.format("INSERT OR REPLACE INTO tblGroups(_id, Name, BookCount, TotalBookCount, IsWatched, IsFinished, IsComplete, ImageComicId)" +
							" VALUES(%d, %s, %d, %d, %d, %d, %d, %d);", 
							cb.getInt(0),
							dbString(cb.getString(1)),
							cb.getInt(2),
							cb.getInt(3),
							cb.getInt(4),
							cb.getInt(5),
							cb.getInt(6),
							cb.getInt(7)));
				}
			}
			finally {
				cb.close();
			}
		}
		finally {
			if (writer != null) {
				try {
					writer.close();
					byteCount = writer.size();
				} 
				catch (IOException e) {}
			}
		}		
		return byteCount;
	}
	
	public static void RestoreSharedPrefencesFromStream(ObjectInputStream input, Editor editor) 
	throws IOException, ClassNotFoundException {
		int size = input.readInt();
		for (int i = 0; i < size; i++) {
			String key = input.readUTF();
			Object val = input.readObject();
			if (val instanceof Boolean)
				editor.putBoolean(key, ((Boolean) val).booleanValue());
			else if (val instanceof Float)
				editor.putFloat(key, ((Float) val).floatValue());
			else if (val instanceof Integer)
				editor.putInt(key, ((Integer) val).intValue());
			else if (val instanceof Long)
				editor.putLong(key, ((Long) val).longValue());
			else if (val instanceof String)
				editor.putString(key, ((String) val));
		}
		editor.commit();
	}
	
	public static void RestoreDataFromStream(DataInputStream in, DBHelper db, String imagePath, Resources r)
	throws IOException
	{
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

			//Restore local images, read image size
			int imgSize = in.readInt();
			if (imgSize > 0)
			{
				//Read image name
				String fileName = in.readUTF();
				//Read image data
				byte[] imgData = new byte[imgSize];
				try {
					in.readFully(imgData, 0, imgSize);
					Bitmap bmp = BitmapFactory.decodeByteArray(imgData, 0, imgSize);
					if (bmp != null)
					{
						ImageHandler.storeImage(bmp, imagePath, fileName, 100);
						ContentValues val = new ContentValues();
						val.put("Image", fileName);
						db.update("tblBooks", val, "_id=?", new String[] { Integer.toString(id) });
					}							
				}
				catch (IOException x) {
					x.printStackTrace();
				}
			}
			
			Double part = (((double)i + 1) / (double)rows) * 100.0;
			EventBus.getDefault().post(new ProgressResult(part.intValue(), r.getString(R.string.progress_restorecomics)));
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

		//Fix images with urls
		Cursor cb = null;				
		try 
		{
			String ids = Joiner.on(',').join(addedComics);
			cb = db.getCursor("SELECT _id, Image, ImageUrl FROM tblBooks WHERE _id IN (" + ids  + ") AND ImageUrl <> ''", null);
			int count = cb.getCount();
			int i = 0;
			while (cb.moveToNext()) {
				String fileName = cb.getString(1);
				if (fileName.length() > 0) {
					File file = new File(imagePath.concat(cb.getString(1)));
					if (file.exists()) {
						Double part = (((double)i + 1) / (double)count) * 100.0;
						EventBus.getDefault().post(new ProgressResult(part.intValue(), r.getString(R.string.progress_restoreimages)));						
						i++;
						continue;
					}
				}
				String url = cb.getString(2);
				if (url.length() > 0) {
					try
					{
						fileName = ImageHandler.storeImage(new URL(url), imagePath);
						ContentValues val = new ContentValues();
						val.put("Image", fileName);
						db.update("tblBooks", val, "_id=?", new String[] { Integer.toString(cb.getInt(0)) });
					}
					catch (Exception x)
					{
						//Unable to save image to disk
						x.printStackTrace();
					}
				}
				
				Double part = (((double)i + 1) / (double)count) * 100.0;
				EventBus.getDefault().post(new ProgressResult(part.intValue(), r.getString(R.string.progress_restoreimages)));						
				i++;
			}
		}
		finally
		{
			cb.close();
		}

		//Fix group images
		db.execSQL("UPDATE tblGroups SET Image = (SELECT Image FROM tblBooks WHERE _id = tblGroups.ImageComicId), ImageUrl = (SELECT ImageUrl FROM tblBooks WHERE _id = tblGroups.ImageComicId)");		
	}
	
	private static String dbString(String val) {
		if (val != null) {
			val = val.replaceAll("'", "''");
			return "'" + val + "'";
		}
		return "null";
	}		
}
