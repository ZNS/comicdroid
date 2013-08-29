package com.zns.comicdroid.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.ThumbnailUtils;
import android.os.Environment;


public final class ImageHandler {
	private final static Bitmap.CompressFormat COMPRESSFORMAT = CompressFormat.JPEG;
	private final static int QUALITY = 75;
	private final static String IMAGEEXTENSION = "jpg";
	
	public static class MediaNotReadyException extends Exception {
		private static final long serialVersionUID = 1L;
		public MediaNotReadyException() {}
	}
	
	private ImageHandler() {		
	}

	public static String storeImage(Bitmap bmp, String filePath, String fileName, int quality)
	{
		FileOutputStream out = null;
		File file = null;
		try {
			file = new File(filePath, fileName);
			out = new FileOutputStream(file);
			bmp.compress(COMPRESSFORMAT, quality, out);			
		} 
		catch (FileNotFoundException e) {
			return "";
		}
		finally {
			try {
				if (out != null)
					out.close();
			} 
			catch (IOException e1) {}			
		}
		return file.toString();
	}
	
	public static String storeImage(URL url, String directory)
			throws IOException, MediaNotReadyException {
		
		if (!mediaReadyForWrite()) {
			throw new MediaNotReadyException();
		}
		
		InputStream is = null;
		Bitmap bmp = null;
		ByteArrayOutputStream stream = null;
		String filePath = null;
		OutputStream fos = null;
		
		try
		{
			HttpURLConnection conn= (HttpURLConnection)url.openConnection();
	        conn.setDoInput(true);
	        conn.connect();
	        is = conn.getInputStream();        
	        bmp = BitmapFactory.decodeStream(is);
	        is.close();
	        
	        stream = new ByteArrayOutputStream();
	        if (bmp.compress(COMPRESSFORMAT, QUALITY, stream))
	        {	        	
	        	byte[] imageData = stream.toByteArray();
	        	String fileName = "thumb" + Integer.toString(url.toString().hashCode()) + "." + IMAGEEXTENSION;	        	
	        			
	        	File file = new File(directory, fileName);
	        	filePath = file.toString();
	        	fos = new BufferedOutputStream(new FileOutputStream(file));
	        	fos.write(imageData);
	        }
		}
		finally {
			if (fos != null)
				fos.close();
			if (is != null)
				is.close();
			if (bmp != null)
				bmp.recycle();
			if (stream != null)
				stream.close();			
		}
		
		return filePath;
	}
	
	public static void resizeOnDisk(String path)
			throws IOException, MediaNotReadyException {
		
		if (!mediaReadyForWrite()) {
			throw new MediaNotReadyException();
		}
		
		Bitmap bmp = null;
		ByteArrayOutputStream stream = null;
		OutputStream fos = null;

		try {
			BitmapFactory.Options options = new Options();
			options.inSampleSize = 4;
			bmp = BitmapFactory.decodeFile(path, options);
	    	
			int width = bmp.getWidth();
	    	int height = bmp.getHeight();
	    	double thumbHeight = ((double)height / (double)width) * 128.0d;
	    	bmp = ThumbnailUtils.extractThumbnail(bmp, 128, (int)thumbHeight);
	    	
	        stream = new ByteArrayOutputStream();
	        if (bmp.compress(COMPRESSFORMAT, QUALITY, stream))
	        {	        	
	        	byte[] imageData = stream.toByteArray();       	
	        	File file = new File(path);
	        	fos = new BufferedOutputStream(new FileOutputStream(file));
	        	fos.write(imageData);
	        }
		}
		finally {
			if (fos != null)
				fos.close();
			if (stream != null)
				stream.close();
			if (bmp != null)
				bmp.recycle();
    	}
	}
	
	private static boolean mediaReadyForWrite() {
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED)) {
		    return true;
		}				
		return false;
	}
}
