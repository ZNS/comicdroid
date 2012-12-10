package com.zns.comicdroid.data;

import com.zns.comicdroid.R;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class AggregateAdapter extends SimpleCursorAdapter {
	private final int _layout;
	private final LayoutInflater _layoutInflater;
	
	static class AggregateHolder
	{
		TextView tvTitle;
		TextView tvAuthor;
		ImageView ivImage;
		TextView tvIssue;
	}
	
	public AggregateAdapter(Context context)
	{
		super(context, R.layout.list_comicrow, null, new String[] { "Title" }, null, 0);
		_layout = R.layout.list_comicrow;
		_layoutInflater = LayoutInflater.from(context);		
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
	    		holder.tvIssue = (TextView)convertView.findViewById(R.id.tvIssue);
	    		
	    		convertView.setTag(holder);
	    	}
	    	else
	    	{
	    		holder = (AggregateHolder)convertView.getTag();
	    	}
	    	
			String title = cursor.getString(1);
			String subTitle = cursor.getString(2);
			String author = cursor.getString(3);
			byte[] image = cursor.getBlob(4);
			
			holder.tvTitle.setText(title + (subTitle != null && subTitle != "" ? " - " + subTitle : ""));
			holder.tvAuthor.setText(author);
			if (image != null)
			{
				Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
				holder.ivImage.setImageBitmap(bmp);
				holder.ivImage.setVisibility(View.VISIBLE);
			}
			else
			{
				holder.ivImage.setVisibility(View.GONE);
			}			    	
	    }
	    
		return convertView;	    
	}		
}
