package com.zns.comicdroid.data;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.services.books.model.Volume.VolumeInfo;
import com.google.api.services.books.model.Volume.VolumeInfo.ImageLinks;
import com.google.api.services.books.model.Volume.VolumeInfo.IndustryIdentifiers;
import com.google.common.base.Joiner;
import com.zns.comicdroid.util.ImageHandler;

public class Comic
	implements Parcelable {
   
	private int id;
	private int groupId;
	private String title;
	private String subTitle;
	private String author;
	private String publisher;
	private int publishDate;
	private int addedDate;
	private int pageCount;
	private int isBorrowed;
	private String borrower;
	private int borrowedDate;
	private String image;
	private String imageUrl;
	private String ISBN;
	private int issue;
	
	public Comic() {
	}

	private Comic(Parcel in) {
		this.id = in.readInt();
		this.groupId = in.readInt();
		this.title = in.readString();
		this.subTitle = in.readString();
		this.author = in.readString();
		this.publisher = in.readString();
		this.publishDate = in.readInt();
		this.addedDate = in.readInt();
		this.pageCount = in.readInt();
		this.isBorrowed = in.readInt();
		this.borrower = in.readString();
		this.borrowedDate = in.readInt();
		this.image = in.readString();
		this.imageUrl = in.readString();
		this.ISBN = in.readString();
		this.issue = in.readInt();
	}
	 
	public Comic(int id, String title, String subTitle, String author, String publisher, int publishDate, int addedDate, int pageCount, int isBorrowed, String borrower, String image, String isbn, int issue, int groupId, String imageUrl, int borrowedDate)
	{
		this.id = id;
		this.groupId = groupId;
		this.title = title;
		this.subTitle = subTitle;
		this.author = author;
		this.publisher = publisher;
		this.publishDate = publishDate;
		this.addedDate = addedDate;
		this.pageCount = pageCount;
		this.isBorrowed = isBorrowed;
		this.borrower = borrower;
		this.borrowedDate = borrowedDate;
		this.image = image;
		this.imageUrl = imageUrl;
		this.ISBN = isbn;
		this.issue = issue;
	}

	public static Comic fromVolumeInfo(VolumeInfo info, String imageDirectory)
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
		
		if (info.getPublishedDate() != null && info.getPublishedDate().length() >= 10)
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date date = dateFormat.parse(info.getPublishedDate());
			comic.setPublishDate(date);
		}
		
		if (info.getImageLinks() != null)
		{
			ImageLinks images = info.getImageLinks();
			String imageUrl = images.getThumbnail();
			if (imageUrl != null)
			{
				comic.setImageUrl(imageUrl);
				try {
		             URL url = new URL(imageUrl);
		             String filePath = ImageHandler.storeImage(url, imageDirectory);
		             comic.setImage(filePath);
			     } catch (Exception e) {
			    	 if (e != null)
			    		 System.out.println(e.getMessage());
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

	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	
	public String getTitle() {
		return title != null ? title : "";
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getSubTitle() {
		return subTitle != null ? subTitle : "";
	}
	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}
	
	public String getAuthor() {
		return author != null ? author : "";
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getPublisher() {
		return publisher != null ? publisher : "";
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
	
	public int getAddedDateTimestamp()
	{
		return addedDate;
	}	
	public Date getAddedDate() {
		return new Date(addedDate * 1000L);
	}	
	public void setAddedDate(Date addedDate) {
		this.addedDate = (int)(addedDate.getTime() / 1000L);
	}
	
	public int getBorrowedDateTimestamp()
	{
		return borrowedDate;
	}
	public Date getBorrowedDate() {
		return new Date(borrowedDate * 1000L);
	}
	public void setBorrowedDate(Date borrowedDate) {
		this.borrowedDate = (int)(borrowedDate.getTime() / 1000L);
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
		return borrower != null ? borrower : "";
	}
	public void setBorrower(String borrower) {
		this.borrower = borrower;
	}

	public String getImage() {
		return image != null ? image : "";
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
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
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) 
			return true;
		if (obj == null || obj.getClass() != this.getClass()) 
			return false;
		Comic c = (Comic)obj;
		return c.getId() == this.getId();
	}
	
	@Override
	public int hashCode() {
		if (this.getISBN() != null && !this.getISBN().equals(""))
			return this.getISBN().hashCode();
		if (this.getId() > 0)
			return this.getId();
		return 0;
	}

	@Override
	public int describeContents() {
		return hashCode();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeInt(groupId);
		dest.writeString(title);
		dest.writeString(subTitle);
		dest.writeString(author);
		dest.writeString(publisher);
		dest.writeInt(publishDate);
		dest.writeInt(addedDate);
		dest.writeInt(pageCount);
		dest.writeInt(isBorrowed);
		dest.writeString(borrower);
		dest.writeInt(borrowedDate);
		dest.writeString(image);
		dest.writeString(imageUrl);
		dest.writeString(ISBN);
		dest.writeInt(issue);
	}
	
    public static final Parcelable.Creator<Comic> CREATOR = new Parcelable.Creator<Comic>() {
		public Comic createFromParcel(Parcel in) {
		    return new Comic(in);
		}
		
		public Comic[] newArray(int size) {
		    return new Comic[size];
		}
    };	
}
