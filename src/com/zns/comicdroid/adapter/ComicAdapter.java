package com.zns.comicdroid.adapter;

import com.zns.comicdroid.R;
import com.zns.comicdroid.util.ImageWorker;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ComicAdapter extends SimpleCursorAdapter
{
	private final int _layout;
	private final LayoutInflater _layoutInflater;
	private final ImageWorker imageWorker = new ImageWorker();
	private int colorDefault;
	private int colorIsBorrowed;
	
	static class ComicHolder
	{
		TextView tvTitle;
		TextView tvAuthor;
		ImageView ivImage;
		TextView tvIssue;
		RelativeLayout rlRow;
	}
	  
	public ComicAdapter(Context context)
	{
		super(context, R.layout.list_comicrow, null, new String[] { "Title" }, null, 0);
		_layout = R.layout.list_comicrow;
		_layoutInflater = LayoutInflater.from(context);
		Resources res = context.getResources();
		colorDefault = res.getColor(R.color.contentBg);
		colorIsBorrowed = res.getColor(R.color.listViewBorrowed);
	}
	
	public int getComicId(int position)
	{
		Cursor cursor = getCursor();
		if (cursor.moveToPosition(position))
			return cursor.getInt(0);
		return 0;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Cursor cursor = getCursor();
	    if (cursor.moveToPosition(position)) 
	    {
	    	ComicHolder holder;
	    	
	    	if (convertView == null)
	    	{
	    		convertView = _layoutInflater.inflate(_layout, null);	    		
	    		holder = new ComicHolder();
	    		holder.tvTitle = (TextView)convertView.findViewById(R.id.tvTitle);
	    		holder.tvAuthor = (TextView)convertView.findViewById(R.id.tvAuthor);
	    		holder.ivImage = (ImageView)convertView.findViewById(R.id.ivImage);
	    		holder.tvIssue = (TextView)convertView.findViewById(R.id.tvIssue);
	    		holder.rlRow = (RelativeLayout)convertView.findViewById(R.id.rlRow);
	    		
	    		convertView.setTag(holder);
	    	}
	    	else
	    	{
	    		holder = (ComicHolder)convertView.getTag();
	    	}
	    	
			String title = cursor.getString(1);
			String subTitle = cursor.getString(2);
			String author = cursor.getString(3);
			String image = cursor.getString(4);
			int issue = cursor.getInt(5);
			boolean isBorrowed = cursor.getInt(6) == 1;
			
			holder.tvTitle.setText(title + (issue > 0 && subTitle != null && !subTitle.equals("") ? " - " + subTitle : ""));
			holder.tvAuthor.setText(author);
			if (issue > 0) {
				holder.tvIssue.setText("Vol. " + Integer.toString(issue));
				holder.tvIssue.setVisibility(View.VISIBLE);
			}
			else {
				holder.tvIssue.setVisibility(View.GONE);
			}
			
			if (image != null) {
				imageWorker.load(image, holder.ivImage);
				holder.ivImage.setVisibility(View.VISIBLE);
			}
			else {
				holder.ivImage.setVisibility(View.GONE);
			}
			
			if (isBorrowed) {
				holder.rlRow.setBackgroundColor(colorIsBorrowed);
			}
			else {
				holder.rlRow.setBackgroundColor(colorDefault);
			}
	    }
	    
		return convertView;	    
	}	
}