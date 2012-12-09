package com.zns.comicdroid.data;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.http.util.ByteArrayBuffer;

import com.google.api.services.books.model.Volume.VolumeInfo;
import com.google.api.services.books.model.Volume.VolumeInfo.ImageLinks;
import com.google.api.services.books.model.Volume.VolumeInfo.IndustryIdentifiers;
import com.google.common.base.Joiner;

public class Comic {
	private int id;
	private String title;
	private String subTitle;
	private String author;
	private String publisher;
	private int publishDate;
	private int addedDate;
	private int pageCount;
	private int isBorrowed;
	private String borrower;
	private byte[] image;
	private String ISBN;
	private int issue;
	
	public Comic() {
	}
	
	public Comic(int id, String title, String subTitle, String author, String publisher, int publishDate, int addedDate, int pageCount, int isBorrowed, String borrower, byte[] image, String isbn, int issue)
	{
		this.id = id;
		this.title = title;
		this.subTitle = subTitle;
		this.author = author;
		this.publisher = publisher;
		this.publishDate = publishDate;
		this.addedDate = addedDate;
		this.pageCount = pageCount;
		this.isBorrowed = isBorrowed;
		this.borrower = borrower;
		this.image = image;
		this.ISBN = isbn;
		this.issue = issue;
	}

	public static Comic fromVolumeInfo(VolumeInfo info)
			throws ParseException
	{
		if (info == null || info.getTitle() == null)
			return null;

		Comic comic = new Comic();		
		
		String title = info.getTitle();
		
		//Try to parse Issue
		String[] parts = title.split(" ");
		for (String part : parts)
		{
			part = part.replace(":",  "");
			if (part.matches("^[0-9]+$"))
			{
				comic.setIssue(Integer.parseInt(part));
				break;
			}
		}
				
		//Try to parse subtitle/title
		title = title.replaceAll("(i?)(Vol|Vol\\.|Volume)\\s{1}[0-9]+", "");
		if (title.contains(":"))
		{			
			parts = title.split(":");
			title = "";
			for (int i = 0; i < parts.length - 1; i++)
			{
				title += parts[i] + " ";
			}
			String subTitle = parts[parts.length - 1];
			comic.setSubTitle(subTitle.trim());
		}

		//Set title
		comic.setTitle(title.trim());
		
		if (info.getIndustryIdentifiers() != null)
		{
			List<IndustryIdentifiers> ids = info.getIndustryIdentifiers();
			for (IndustryIdentifiers id : ids)
			{
				if (id.getType().equals("ISBN_13"))
				{
					comic.setISBN(id.getIdentifier());
					break;
				}
			}
			if (comic.getISBN() == null)
				comic.setISBN(ids.get(0).getIdentifier());
		}
		if (info.getSubtitle() != null)
			comic.setSubTitle(info.getSubtitle());
		if (info.getAuthors() != null)
			comic.setAuthor(Joiner.on(",").skipNulls().join(info.getAuthors()));
		if (info.getPublisher() != null)
			comic.setPublisher(info.getPublisher());
		try
		{
			if (info.getPageCount() > 0)
				comic.setPageCount(info.getPageCount());
			}
		catch (Exception e) {}
		
		if (info.getPublishedDate() != null)
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
			Date date = dateFormat.parse(info.getPublishedDate());
			comic.setPublishDate(date);
		}
		
		if (info.getImageLinks() != null)
		{
			ImageLinks images = info.getImageLinks();
			String imageUrl = images.getThumbnail();
			if (imageUrl != null)
			{
				try {
		             URL url = new URL(imageUrl);
		             URLConnection ucon = url.openConnection();

		             InputStream is = ucon.getInputStream();
		             BufferedInputStream bis = new BufferedInputStream(is);

		             ByteArrayBuffer baf = new ByteArrayBuffer(500);
		             int current = 0;
		             while ((current = bis.read()) != -1) {
		                     baf.append((byte) current);
		             }

		             comic.setImage(baf.toByteArray());
			     } catch (Exception e) {
			     }
			}
		}
				
		return comic;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getSubTitle() {
		return subTitle;
	}
	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}
	
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getPublisher() {
		return publisher;
	}
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	
	public int getPublishDateTimestamp()
	{
		return publishDate;
	}
	public Date getPublishDate() {
		return new Date(publishDate * 1000L);
	}
	public void setPublishDate(Date publishDate) {
		this.publishDate = (int)(publishDate.getTime() / 1000L);
	}	
	
	public Date getAddedDate() {
		return new Date(addedDate * 1000L);
	}	
	public void setAddedDate(Date addedDate) {
		this.addedDate = (int)(addedDate.getTime() / 1000L);
	}
	
	public int getPageCount() {
		return pageCount;
	}
	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}
	
	public Boolean getIsBorrowed() {
		return isBorrowed == 1;
	}
	public void setIsBorrowed(Boolean isBorrowed) {
		this.isBorrowed = isBorrowed ? 1 : 0;
	}
	
	public String getBorrower() {
		return borrower;
	}
	public void setBorrower(String borrower) {
		this.borrower = borrower;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	public String getISBN() {
		return ISBN;
	}

	public void setISBN(String iSBN) {
		ISBN = iSBN;
	}

	public int getIssue() {
		return issue;
	}

	public void setIssue(int issue) {
		this.issue = issue;
	}
}
