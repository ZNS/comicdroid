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

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.zns.comicdroid.R;
import com.zns.comicdroid.data.Comic;

public class ComicArrayAdapter extends ArrayAdapter<Comic> {
	private final List<Comic> mValues;
	private final String mImagePath;

	static class ComicHolder
	{
		TextView tvTitle;
		TextView tvAuthor;
		ImageView ivImage;
		TextView tvIssue;
	}

	public ComicArrayAdapter(Context context, List<Comic> values, String imagePath) 
	{
		super(context, R.layout.list_comicrow, values);
		this.mValues = values;
		this.mImagePath = "file://".concat(imagePath); 
	}

	public Comic getComic(int position)
	{
		if (position < mValues.size())
			return mValues.get(position);
		return null;
	}

	public List<Comic> getAll(){
		return this.mValues;  
	}

	public View getView(int position, View convertView, ViewGroup parent) 
	{		  
		View row = convertView;
		ComicHolder holder = null;

		if (row == null)
		{
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
			row = inflater.inflate(R.layout.list_comicrow, parent, false);

			holder = new ComicHolder();
			holder.tvTitle = (TextView)row.findViewById(R.id.tvTitle);
			holder.tvAuthor = (TextView)row.findViewById(R.id.tvAuthor);
			holder.ivImage = (ImageView)row.findViewById(R.id.ivImage);
			holder.tvIssue = (TextView)row.findViewById(R.id.tvIssue);

			row.setTag(holder);
		}
		else
		{
			holder = (ComicHolder)row.getTag();
		}

		Comic comic = mValues.get(position);
		holder.tvTitle.setText(comic.getTitle() + (comic.getIssue() > 0 && comic.getSubTitle() != null ? " - " + comic.getSubTitle() : ""));
		holder.tvAuthor.setText(comic.getAuthor());
		if (comic.getIssue() > 0)
		{
			holder.tvIssue.setText("Vol. " + Integer.toString(comic.getIssue()));
		}
		else
		{
			holder.tvIssue.setText("");
		}
		if (comic.getImage() != null && !comic.getImage().equals(""))
		{
			ImageLoader.getInstance().displayImage(mImagePath.concat(comic.getImage()), holder.ivImage);			  
			holder.ivImage.setVisibility(View.VISIBLE);
		}
		else
		{
			holder.ivImage.setVisibility(View.GONE);
		}

		return row;
	}
}
