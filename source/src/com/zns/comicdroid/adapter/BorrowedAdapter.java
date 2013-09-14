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
package com.zns.comicdroid.adapter;

import java.text.DateFormat;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zns.comicdroid.R;
import com.zns.comicdroid.data.Comic;
import com.zns.comicdroid.util.ImageWorker;

public class BorrowedAdapter extends ArrayAdapter<Comic> {
	private final ImageWorker mImageWorker = new ImageWorker();  
	private final List<Comic> mValues;
	private final String mImagePath;
	private final DateFormat mDateFormat;
	
	static class ComicHolder
	{
		TextView tvTitle;
		ImageView ivImage;
		TextView tvBorrower;
		TextView tvBorrowedDate;
	}

	public BorrowedAdapter(Context context, List<Comic> values, String imagePath) 
	{
		super(context, R.layout.list_borrowedrow, values);
		this.mValues = values;
		this.mImagePath = imagePath;
		this.mDateFormat = android.text.format.DateFormat.getDateFormat(context);
	}

	public Comic getComic(int position)
	{
		if (position < mValues.size())
			return mValues.get(position);
		return null;
	}

	@Override
	public long getItemId(int position) {
		return mValues.get(position).getId();
	}

	public View getView(int position, View convertView, ViewGroup parent) 
	{		  
		View row = convertView;
		ComicHolder holder = null;

		if (row == null)
		{
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
			row = inflater.inflate(R.layout.list_borrowedrow, parent, false);

			holder = new ComicHolder();
			holder.tvTitle = (TextView)row.findViewById(R.id.tvTitle);
			holder.ivImage = (ImageView)row.findViewById(R.id.ivImage);
			holder.tvBorrower = (TextView)row.findViewById(R.id.tvBorrower);
			holder.tvBorrowedDate = (TextView)row.findViewById(R.id.tvBorrowedDate);

			row.setTag(holder);
		}
		else
		{
			holder = (ComicHolder)row.getTag();
		}

		Comic comic = mValues.get(position);
		holder.tvTitle.setText(comic.getTitle() + (comic.getIssue() > 0 && comic.getSubTitle() != null ? " - " + comic.getSubTitle() : "") + (comic.getIssue() > 0 ? " - Vol. " + Integer.toString(comic.getIssue()) : ""));
		holder.tvBorrower.setText(comic.getBorrower());
		holder.tvBorrowedDate.setText(mDateFormat.format(comic.getBorrowedDate()));
		if (comic.getImage() != null && !comic.getImage().equals(""))
		{
			mImageWorker.load(mImagePath.concat(comic.getImage()), holder.ivImage);
			holder.ivImage.setVisibility(View.VISIBLE);
		}
		else
		{
			holder.ivImage.setVisibility(View.GONE);
		}

		return row;
	}
}
