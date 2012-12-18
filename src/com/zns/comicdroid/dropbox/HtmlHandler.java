package com.zns.comicdroid.dropbox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import android.content.res.Resources;
import android.database.Cursor;

import com.dropbox.client2.session.AccessTokenPair;
import com.zns.comicdroid.R;
import com.zns.comicdroid.data.Comic;
import com.zns.comicdroid.data.DBHelper;

public final class HtmlHandler {
	
	private HtmlHandler() {}
	
	public static void WriteAndPublish(Resources resources, DBHelper db, AccessTokenPair accessToken, String outPath) 
			throws IOException {
				
		File fileOut = new File(outPath);
		fileOut.mkdirs();
		fileOut = new File(outPath, "index.html");
		
		BufferedReader reader = null;
		BufferedWriter writer = null;
		Cursor cursor = db.getCursor("SELECT _id, Title, Subtitle, Author, ImageUrl, 1 AS ItemType, 0 AS BookCount, IsBorrowed " +
				"FROM tblBooks WHERE GroupId = 0 OR ifnull(GroupId, '') = '' " +
				"UNION " +
				"SELECT _id, Name AS Title, '' AS Subtitle, '' AS Author, ImageUrl, 2 AS ItemType, BookCount, 0 AS IsBorrowed " +
				"FROM tblGroups " +
				"ORDER BY Title", null);
		
		try 
		{			
			String line;
			
			//Read template
			StringBuilder sbTemplate = new StringBuilder();
			reader = new BufferedReader(new InputStreamReader(resources.openRawResource(R.raw.listtemplate)));
			while ((line = reader.readLine()) != null)
			{
				sbTemplate.append(line);
			}
			reader.close();
			
			//Write HTML
			writer = new BufferedWriter(new FileWriter(fileOut));
			reader = new BufferedReader(new InputStreamReader(resources.openRawResource(R.raw.framework)));			
			while ((line = reader.readLine()) != null)
			{
				if (line.trim().equals("#LISTCOMICS#")) {
					while (cursor.moveToNext()) {
						int id = cursor.getInt(0);
						String title = cursor.getString(1);
						String subTitle = cursor.getString(2);
						String author = cursor.getString(3);
						String imageUrl = cursor.getString(4);
						int type = cursor.getInt(5);
						StringBuilder sbChildren = new StringBuilder();
						
						String comicLine = sbTemplate.toString();
						comicLine = comicLine.replace("#TITLE#", title + (type == 1 && !subTitle.equals("") ? " - " + subTitle : ""));
						comicLine = comicLine.replace("#AUTHOR#", author);
						comicLine = comicLine.replace("#IMAGEURL#", imageUrl);
						comicLine = comicLine.replace("#ISSUE#", "");
						
						if (type == 2) {
							//Render group children
							List<Comic> comics = db.getComics(id);
							for(Comic comic : comics) {
								String childComic = sbTemplate.toString();
								childComic = childComic.replace("#TITLE#", comic.getTitle() + (!comic.getSubTitle().equals("") ? " - " + comic.getSubTitle() : ""));
								childComic = childComic.replace("#AUTHOR#", comic.getAuthor());
								childComic = childComic.replace("#IMAGEURL#", comic.getImageUrl());
								childComic = childComic.replace("#ISSUE#", comic.getIssue() > 0 ? Integer.toString(comic.getIssue()) : "");
								childComic = childComic.replace("#ISSUE#", "");
								sbChildren.append(childComic);
							}
							comicLine = comicLine.replace("#CHILDREN#", sbChildren.toString());
						}
						else {
							comicLine = comicLine.replace("#CHILDREN#", "");
						}
						
						writer.write(comicLine);
					}
				}
				else {
					writer.write(line);
				}
			}
		}
		finally {
			if (cursor != null)
				cursor.close();
			if (writer != null)
				writer.close();
			if (reader != null)
				reader.close();
		}
		
		//Upload to dropbox
		DropboxHandler dbHandler = new DropboxHandler(accessToken);
		dbHandler.uploadFile(fileOut);
	}
}
