package com.zns.comicdroid.util;

import android.annotation.SuppressLint;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.zns.comicdroid.Application;

@SuppressLint("SimpleDateFormat")
public class Logger {
	public static final String TAG_BACKUP = "Backup";
	public static final String LOG_FILENAME = "comicdroid.log";
	private final File mFile;
	private final SimpleDateFormat mFormat;

	public Logger(String path) {
		mFormat = new SimpleDateFormat("y-MM-d HH:mm:ss");		
		mFile = new File(path, LOG_FILENAME);
		
		if (!Application.DEBUG) {
			return;
		}
		
		if (!mFile.exists())
		{
			try {
				new File(path).mkdirs();
				mFile.createNewFile();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}

	public void appendLog(String text, String tag)
	{          
		if (!Application.DEBUG) {
			return;
		}

		BufferedWriter buf = null;
		try {
			//BufferedWriter for performance, true to set append to file flag
			buf = new BufferedWriter(new FileWriter(mFile, true));
			buf.append(mFormat.format(new Date()));
			buf.append('\t');
			buf.append(tag);
			buf.append('\t');
			buf.append(text);
			buf.newLine();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (buf != null) {
				try {
					buf.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}