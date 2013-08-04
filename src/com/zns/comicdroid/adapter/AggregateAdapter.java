package com.zns.comicdroid.adapter;

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

import com.zns.comicdroid.R;
import com.zns.comicdroid.data.Aggregate;
import com.zns.comicdroid.util.ImageWorker;

public class AggregateAdapter extends SimpleCursorAdapter {
	private final int _layout;
	private final LayoutInflater _layoutInflater;
	private final ImageWorker imageWorker = new ImageWorker();
	private int colorDefault;
	private int colorIsBorrowed;
	
	static class AggregateHolder
	{
		TextView tvTitle;
		TextView tvAuthor;
		ImageView ivImage;
		TextView tvCount;
		ImageView ivGroupMark;
		ImageView ivGroupWatched;
		ImageView ivGroupCompleted;
		ImageView ivGroupFinished;
		RelativeLayout rlRow;
	}
	
	public AggregateAdapter(Context context)
	{
		super(context, R.layout.list_comicrow, null, new String[] { "Title" }, null, 0);
		_layout = R.layout.list_comicrow;
		_layoutInflater = LayoutInflater.from(context);		
		Resources res = context.getResources();
		colorDefault = res.getColor(R.color.contentBg);
		colorIsBorrowed = res.getColor(R.color.listViewBorrowed);		
	}
	
	public Aggregate getAggregate(int position) {
		Cursor cursor = getCursor();
		if (cursor.moveToPosition(position)) {
			return new Aggregate(cursor.getInt(0), cursor.getString(1), cursor.getInt(5));
		}
		return null;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Cursor cursor = getCursor();
	    if (cursor.moveToPosition(position)) 
	    {
	    	AggregateHolder holder;
	    	
	    	if (convertView == null)
	    	{
	    		convertView = _layoutInflater.inflate(_layout, null);	    		
	    		holder = new AggregateHolder();
	    		holder.tvTitle = (TextView)convertView.findViewById(R.id.tvTitle);
	    		holder.tvAuthor = (TextView)convertView.findViewById(R.id.tvAuthor);
	    		holder.ivImage = (ImageView)convertView.findViewById(R.id.ivImage);
	    		holder.tvCount = (TextView)convertView.findViewById(R.id.tvCount);
	    		holder.ivGroupMark = (ImageView)convertView.findViewById(R.id.ivGroupMark);
	    		holder.rlRow = (RelativeLayout)convertView.findViewById(R.id.rlRow);
	    		holder.ivGroupCompleted = (ImageView)convertView.findViewById(R.id.ivGroupCompleted);
	    		holder.ivGroupFinished = (ImageView)convertView.findViewById(R.id.ivGroupFinished);
	    		holder.ivGroupWatched = (ImageView)convertView.findViewById(R.id.ivGroupWatched);
	    		
	    		convertView.setTag(holder);
	    	}
	    	else
	    	{
	    		holder = (AggregateHolder)convertView.getTag();
	    	}
	    	
			String title = cursor.getString(1);
			String subTitle = cursor.getString(2);
			String author = cursor.getString(3);
			String image = cursor.getString(4);
			int type = cursor.getInt(5);
			int count = cursor.getInt(6);
			boolean isBorrowed = cursor.getInt(7) == 1;
			int totalCount = cursor.getInt(8);
			boolean isFinished = cursor.getInt(9) == 1;
			boolean isComplete = cursor.getInt(10) == 1;
			boolean isWatched = cursor.getInt(11) == 1;
			
			holder.tvTitle.setText(title + (subTitle != null && !subTitle.equals("") ? " - " + subTitle : ""));
			holder.tvAuthor.setText(author);
			if (type == 2) {
				holder.ivGroupMark.setVisibility(View.VISIBLE);
				holder.tvCount.setText("(" + count + (totalCount > 0 ? "/" + totalCount : "") + ")");
				holder.ivGroupFinished.setVisibility(isFinished ? View.VISIBLE : View.GONE);
				holder.ivGroupCompleted.setVisibility(isComplete ? View.VISIBLE : View.GONE);
				holder.ivGroupWatched.setVisibility(isWatched ? View.VISIBLE : View.GONE);
			}
			else
			{
				holder.ivGroupMark.setVisibility(View.GONE);
				holder.ivGroupCompleted.setVisibility(View.GONE);
				holder.ivGroupFinished.setVisibility(View.GONE);
				holder.ivGroupWatched.setVisibility(View.GONE);
				holder.tvCount.setText("");
			}
			if (image != null)
			{
				imageWorker.load(image, holder.ivImage);
				holder.ivImage.setVisibility(View.VISIBLE);
			}
			else
			{
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
