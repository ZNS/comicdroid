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
package com.zns.comicdroid.data;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.services.books.model.Volume.VolumeInfo;
import com.google.api.services.books.model.Volume.VolumeInfo.ImageLinks;
import com.google.api.services.books.model.Volume.VolumeInfo.IndustryIdentifiers;
import com.google.common.base.Joiner;
import com.zns.comicdroid.Application;
import com.zns.comicdroid.gcd.Client;
import com.zns.comicdroid.gcd.Issue;
import com.zns.comicdroid.util.ImageHandler;
import com.zns.comicdroid.util.StringUtil;
import com.zns.openlibrary.Book;

public class Comic
implements Parcelable {

	private int mId;
	private int mGroupId;
	private String mTitle;
	private String mSubTitle;
	private String mAuthor;
	private String mIllustrator;
	private String mPublisher;
	private int mPublishDate;
	private int mAddedDate;
	private int mPageCount;
	private int mIsBorrowed;
	private int mIsRead;
	private int mRating;
	private String mBorrower;
	private int mBorrowedDate;
	private String mImage;
	private String mImageUrl;
	private String mISBN;
	private int mIssue;
	private String mIssues;

	public Comic() {
	}

	private Comic(Parcel in) {
		this.mId = in.readInt();
		this.mGroupId = in.readInt();
		this.mTitle = in.readString();
		this.mSubTitle = in.readString();
		this.mAuthor = in.readString();
		this.mIllustrator = in.readString();
		this.mPublisher = in.readString();
		this.mPublishDate = in.readInt();
		this.mAddedDate = in.readInt();
		this.mPageCount = in.readInt();
		this.mIsBorrowed = in.readInt();
		this.mBorrower = in.readString();
		this.mBorrowedDate = in.readInt();
		this.mImage = in.readString();
		this.mImageUrl = in.readString();
		this.mISBN = in.readString();
		this.mIssue = in.readInt();
		this.mIsRead = in.readInt();
		this.mRating = in.readInt();
		this.mIssues = in.readString();
	}

	public Comic(int id, String title, String subTitle, String author, String illustrator, String publisher, int publishDate, int addedDate, int pageCount, int isBorrowed, String borrower, String image, String isbn, int issue, int groupId, String imageUrl, int borrowedDate, int isRead, int rating, String issues)
	{
		this.mId = id;
		this.mGroupId = groupId;
		this.mTitle = title;
		this.mSubTitle = subTitle;
		this.mAuthor = author;
		this.mIllustrator = illustrator;
		this.mPublisher = publisher;
		this.mPublishDate = publishDate;
		this.mAddedDate = addedDate;
		this.mPageCount = pageCount;
		this.mIsBorrowed = isBorrowed;
		this.mBorrower = borrower;
		this.mBorrowedDate = borrowedDate;
		this.mImage = image;
		this.mImageUrl = imageUrl;
		this.mISBN = isbn;
		this.mIssue = issue;
		this.mIsRead = isRead;
		this.mRating = rating;
		this.mIssues = issues;
	}

	public static Comic fromGCDIssue(Issue issue, String imageDirectory, String isbn)
	{
		if (issue == null)
			return null;
		
		Comic comic = new Comic();
		if (issue.title != null)
		{
			if (StringUtil.nullOrEmpty(issue.volume)) {
				comic.setTitle(issue.title);
			}
			else {
				comic.setTitle(issue.feature);
				comic.setSubTitle(issue.title);
			}
		}
		
		comic.setPageCount(issue.page_count);
		
		if (!StringUtil.nullOrEmpty(issue.volume) && issue.volume.matches("\\d+")) {
			comic.setIssue(Integer.parseInt(issue.volume.replaceAll("[^\\d]", "")));
		}
		else if (!StringUtil.nullOrEmpty(issue.number) && issue.number.matches("\\d+")) {
			comic.setIssue(Integer.parseInt(issue.number.replaceAll("[^\\d]", "")));
		}
		
		if (!StringUtil.nullOrEmpty(issue.publisher)) {
			comic.setPublisher(issue.publisher);
		}
		else if (!StringUtil.nullOrEmpty(issue.brand)) {
			comic.setPublisher(issue.brand);
		}
		
		if (issue.script != null) {
			issue.script = issue.script.replaceAll(";", ",").replaceAll("\\([^)]*\\)", "");
			issue.script = Joiner.on(",").join(toUniqueArray(issue.script, ","));
			comic.setAuthor(issue.script);
		}
		
		String illustrator = (issue.pencils != null ? issue.pencils : "") + "," + (issue.inks != null ? issue.inks : "");
		if (!illustrator.equals(""))
		{
			illustrator = illustrator.replaceAll(";", ",").replaceAll("\\([^)]*\\)", "");
			illustrator = Joiner.on(",").join(toUniqueArray(illustrator, ","));
			comic.setIllustrator(illustrator);
		}
		
		if (!StringUtil.nullOrEmpty(isbn)) {
			comic.setISBN(isbn);
		}
		else if (!StringUtil.nullOrEmpty(issue.isbn)) {
			comic.setISBN(issue.isbn);
		}
		else {
			comic.setISBN("");
		}

		if (issue.images != null && issue.images.length > 0)
		{
			comic.setImageUrl(issue.images[0]);
			try {
				String imgUrl = issue.images[0];
				if (imgUrl.charAt(0) == '/') {
					imgUrl = Client.IMAGEURL + imgUrl;
				}
				URL url = new URL(imgUrl);
				String fileName = ImageHandler.storeImage(url, imageDirectory);
				ImageHandler.resizeOnDisk(imageDirectory + "/" + fileName);
				comic.setImage(fileName);
			} catch (Exception e) {
				if (e != null)
					System.out.println(e.getMessage());
			}
		}
		
		return comic;
	}
	
	public static Comic fromOpenLibraryBook(Book book, String imageDirectory, String isbn)
	{
		if (book == null)
			return null;
		
		Comic comic = new Comic();
		if (book.title != null)
			comic.setTitle(book.title.replaceAll(Application.COMIC_REGEX_BLACKLIST, "").trim());
		if (book.subTitle != null)
			comic.setSubTitle(book.subTitle.replaceAll(Application.COMIC_REGEX_BLACKLIST, "").trim());
		if (book.authors != null && book.authors.length > 0)
			comic.setAuthor(book.authors[0].name);
		comic.setPageCount(book.numberOfPages);
		if (book.publishers != null && book.publishers.length > 0)
			comic.setPublisher(book.publishers[0].name);
		if (book.covers != null && book.covers.medium != null)
		{
			comic.setImageUrl(book.covers.medium);
			try {
				URL url = new URL(book.covers.medium);
				String fileName = ImageHandler.storeImage(url, imageDirectory);
				ImageHandler.resizeOnDisk(imageDirectory + "/" + fileName);
				comic.setImage(fileName);
			} catch (Exception e) {
				if (e != null)
					System.out.println(e.getMessage());
			}
		}
		if (!StringUtil.nullOrEmpty(isbn)) {
			comic.setISBN(isbn);
		}
		else if (book.identifiers != null)
		{
			if (book.identifiers.isbn13 != null && book.identifiers.isbn13.length > 0) {
				comic.setISBN(book.identifiers.isbn13[0]);
			}
			else if (book.identifiers.isbn10 != null && book.identifiers.isbn10.length > 0) {
				comic.setISBN(book.identifiers.isbn10[0]);
			}
		}
		else {
			comic.setISBN("");
		}
		
		return comic;
	}
	
	public static Comic fromVolumeInfo(VolumeInfo info, String imageDirectory, String isbn)
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

		//Set isbn to the passed if available
		if (isbn != null & isbn.length() > 0) {
			comic.setISBN(isbn);		
		}
		else if (info.getIndustryIdentifiers() != null)
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
					String fileName = ImageHandler.storeImage(url, imageDirectory);
					comic.setImage(fileName);
				} catch (Exception e) {
					if (e != null)
						System.out.println(e.getMessage());
				}
			}
		}

		if (comic.getTitle() != null) {
			comic.setTitle(comic.getTitle().replaceAll(Application.COMIC_REGEX_BLACKLIST, "").trim());
		}
		if (comic.getSubTitle() != null) {
			comic.setSubTitle(comic.getSubTitle().replaceAll(Application.COMIC_REGEX_BLACKLIST, "").trim());
		}
		
		return comic;
	}

	public static String[] toUniqueArray(String values, String separator)
	{
		if (StringUtil.nullOrEmpty(values)) {
			return new String[0];
		}
		
		List<String> unique = new ArrayList<String>();
		String[] arr = values.split(separator);
		for (int i = 0; i < arr.length; i++) {
			boolean found = false;
			for (String val : unique) {
				if (val.equalsIgnoreCase(arr[i].trim())) {
					found = true;
				}
			}
			if (!found) {
				unique.add(arr[i].trim());
			}
		}
		return unique.toArray(new String[unique.size()]);
	}

	public int getId() {
		return mId;
	}
	public void setId(int id) {
		this.mId = id;
	}

	public int getGroupId() {
		return mGroupId;
	}
	public void setGroupId(int groupId) {
		this.mGroupId = groupId;
	}

	public String getTitle() {
		return mTitle != null ? mTitle : "";
	}
	public void setTitle(String title) {
		this.mTitle = title;
	}

	public String getSubTitle() {
		return mSubTitle != null ? mSubTitle : "";
	}
	public void setSubTitle(String subTitle) {
		this.mSubTitle = subTitle;
	}

	public String getAuthor() {
		return mAuthor != null ? mAuthor : "";
	}
	public void setAuthor(String author) {
		this.mAuthor = author;
	}

	public String getIllustrator() {
		return mIllustrator != null ? mIllustrator : "";
	}
	public void setIllustrator(String illustrator) {
		this.mIllustrator = illustrator;
	}

	public String getPublisher() {
		return mPublisher != null ? mPublisher : "";
	}
	public void setPublisher(String publisher) {
		this.mPublisher = publisher;
	}

	public int getPublishDateTimestamp()
	{
		return mPublishDate;
	}
	public Date getPublishDate() {
		return new Date(mPublishDate * 1000L);
	}
	public void setPublishDate(Date publishDate) {
		this.mPublishDate = (int)(publishDate.getTime() / 1000L);
	}	

	public int getAddedDateTimestamp()
	{
		return mAddedDate;
	}	
	public Date getAddedDate() {
		return new Date(mAddedDate * 1000L);
	}	
	public void setAddedDate(Date addedDate) {
		this.mAddedDate = (int)(addedDate.getTime() / 1000L);
	}

	public int getBorrowedDateTimestamp()
	{
		return mBorrowedDate;
	}
	public Date getBorrowedDate() {
		return new Date(mBorrowedDate * 1000L);
	}
	public void setBorrowedDate(Date borrowedDate) {
		this.mBorrowedDate = (int)(borrowedDate.getTime() / 1000L);
	}

	public int getPageCount() {
		return mPageCount;
	}
	public void setPageCount(int pageCount) {
		this.mPageCount = pageCount;
	}

	public Boolean getIsBorrowed() {
		return mIsBorrowed == 1;
	}
	public void setIsBorrowed(Boolean isBorrowed) {
		this.mIsBorrowed = isBorrowed ? 1 : 0;
	}

	public Boolean getIsRead() {
		return mIsRead == 1;
	}
	public void setIsRead(Boolean isRead) {
		this.mIsRead = isRead ? 1 : 0;
	}

	public String getBorrower() {
		return mBorrower != null ? mBorrower : "";
	}
	public void setBorrower(String borrower) {
		this.mBorrower = borrower;
	}

	public String getImage() {
		return mImage != null ? mImage : "";
	}

	public void setImage(String image) {
		this.mImage = image;
	}

	public String getImageUrl() {
		return mImageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.mImageUrl = imageUrl;
	}

	public String getISBN() {
		return mISBN;
	}

	public void setISBN(String iSBN) {
		mISBN = iSBN;
	}

	public int getIssue() {
		return mIssue;
	}

	public void setIssue(int issue) {
		this.mIssue = issue;
	}

	public int getRating() {
		return mRating;
	}
	public void setRating(int rating) {
		this.mRating = rating;
	}

	public String getIssues() {
		return mIssues;
	}
	public void setIssues(String issues) {
		this.mIssues = issues;
	}

	public void extendFromOpenLibrary(Book book, String imageDirectory)
	{
		Comic comic = Comic.fromOpenLibraryBook(book, imageDirectory, null);
		extend(comic);
	}

	public void extendFromGCD(Issue issue, String imageDirectory)
	{
		Comic comic = Comic.fromGCDIssue(issue, imageDirectory, null);
		extend(comic);
	}
	
	public void extendFromGoogleBooks(VolumeInfo info, String imageDirectory)
			throws ParseException
	{
		Comic comic = Comic.fromVolumeInfo(info, imageDirectory, null);
		extend(comic);
	}
	
	public void extend(Comic comic) {
		if (StringUtil.nullOrEmpty(mAuthor)) {
			setAuthor(comic.getAuthor());
		}
		if (StringUtil.nullOrEmpty(mIllustrator)){
			setIllustrator(comic.getIllustrator());
		}
		if (StringUtil.nullOrEmpty(mImageUrl) && StringUtil.nullOrEmpty(mImage)) {
			setImageUrl(comic.getImageUrl());
			setImage(comic.getImage());
		}
		if (StringUtil.nullOrEmpty(mPublisher)) {
			setPublisher(comic.getPublisher());
		}
		if (StringUtil.nullOrEmpty(mSubTitle)) {
			setSubTitle(comic.getSubTitle());
		}
		if (StringUtil.nullOrEmpty(mTitle)) {
			setTitle(comic.getTitle());
		}
		if (mIssue == 0) {
			setIssue(comic.getIssue());
		}
		if (mPageCount == 0) {
			setPageCount(comic.getPageCount());
		}
	}
	
	public boolean hasInfo() {
		return !StringUtil.nullOrEmpty(mAuthor) || 
				!StringUtil.nullOrEmpty(mIllustrator) || 
				!StringUtil.nullOrEmpty(mImageUrl) || 
				!StringUtil.nullOrEmpty(mImage) || 
				!StringUtil.nullOrEmpty(mPublisher) || 
				!StringUtil.nullOrEmpty(mTitle) || 
				!StringUtil.nullOrEmpty(mSubTitle); 
	}
	
	public boolean isComplete()
	{
		if (StringUtil.nullOrEmpty(mTitle) || StringUtil.nullOrEmpty(mAuthor) || StringUtil.nullOrEmpty(mIllustrator) || StringUtil.nullOrEmpty(mPublisher) || StringUtil.nullOrEmpty(mImageUrl) || mIssue == 0) {
			return false;
		}
		return true;
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
		dest.writeInt(mId);
		dest.writeInt(mGroupId);
		dest.writeString(mTitle);
		dest.writeString(mSubTitle);
		dest.writeString(mAuthor);
		dest.writeString(mIllustrator);
		dest.writeString(mPublisher);
		dest.writeInt(mPublishDate);
		dest.writeInt(mAddedDate);
		dest.writeInt(mPageCount);
		dest.writeInt(mIsBorrowed);
		dest.writeString(mBorrower);
		dest.writeInt(mBorrowedDate);
		dest.writeString(mImage);
		dest.writeString(mImageUrl);
		dest.writeString(mISBN);
		dest.writeInt(mIssue);
		dest.writeInt(mIsRead);
		dest.writeInt(mRating);
		dest.writeString(mIssues);
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
