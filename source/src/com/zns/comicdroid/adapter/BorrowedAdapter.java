package com.zns.comicdroid.adapter;

import java.text.SimpleDateFormat;
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

	static class ComicHolder
	{
		TextView tvTitle;
		ImageView ivImage;
		TextView tvIssue;
		TextView tvBorrower;
		TextView tvBorrowedDate;
	}

	public BorrowedAdapter(Context context, List<Comic> values, String imagePath) 
	{
		super(context, R.layout.list_borrowedrow, values);
		this.mValues = values;
		this.mImagePath = imagePath;
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
			holder.tvIssue = (TextView)row.findViewById(R.id.tvIssue);
			holder.tvBorrower = (TextView)row.findViewById(R.id.tvBorrower);
			holder.tvBorrowedDate = (TextView)row.findViewById(R.id.tvBorrowedDate);

			row.setTag(holder);
		}
		else
		{
			holder = (ComicHolder)row.getTag();
		}

		Comic comic = mValues.get(position);
		holder.tvTitle.setText(comic.getTitle() + (comic.getIssue() > 0 && comic.getSubTitle() != null ? " - " + comic.getSubTitle() : ""));
		holder.tvBorrower.setText(comic.getBorrower());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		holder.tvBorrowedDate.setText(dateFormat.format(comic.getBorrowedDate()));
		if (comic.getIssue() > 0)
		{
			holder.tvIssue.setText("Vol. " + Integer.toString(comic.getIssue()));
			holder.tvIssue.setVisibility(View.VISIBLE);
		}
		else
		{
			holder.tvIssue.setVisibility(View.GONE);
		}

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