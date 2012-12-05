package com.zns.comicdroid.data;

import com.zns.comicdroid.R;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.TextView;

public class ComicsAdapter extends SimpleCursorAdapter
	implements FilterQueryProvider
{
	private final int _layout;
	private final LayoutInflater _layoutInflater;
	private final SQLiteDatabase _db;
	//private Cursor _cursor;	
	
	static class ComicHolder
	{
		TextView tvTitle;
		TextView tvAuthor;
		ImageView ivImage;
		TextView tvIssue;
	}
	  
	public ComicsAdapter(Context context)
	{
		super(context, R.layout.list_comicrow, null, null, null);
		_layout = R.layout.list_comicrow;
		_layoutInflater = LayoutInflater.from(context);

		//Attach filter query provider
		this.setFilterQueryProvider(this);
		
		//Get Cursor
		DBHelper dbHelper = new DBHelper(context);
		_db = dbHelper.getReadableDatabase();
		Cursor cursor = _db.rawQuery("SELECT Id AS _id, Title, Subtitle, Author, Image, Issue " +
				"FROM tblBooks ORDER BY Title", null);
		this.changeCursorAndColumns(cursor, new String[] { "Title" }, null);
	}
	
	public int getComicId(int position)
	{
		Cursor cursor = getCursor();
		if (cursor.moveToPosition(position))
			return cursor.getInt(0);
		return 0;
	}
	
	@Override
	public Cursor runQuery(CharSequence constraint)
	{
		String sql = "SELECT Id AS _id, Title, Subtitle, Author, Image, Issue " +
				"FROM tblBooks WHERE Title LIKE '" + constraint + "%' ORDER BY Title";
		Cursor cursor = _db.rawQuery(sql, null);
		return cursor;
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
	    		
	    		convertView.setTag(holder);
	    	}
	    	else
	    	{
	    		holder = (ComicHolder)convertView.getTag();
	    	}
	    	
			String title = cursor.getString(1);
			String subTitle = cursor.getString(2);
			String author = cursor.getString(3);
			byte[] image = cursor.getBlob(4);
			int issue = cursor.getInt(5);
			
			holder.tvTitle.setText(title + (issue > 0 && subTitle != null ? " - " + subTitle : ""));
			holder.tvAuthor.setText(author);
			if (issue > 0)
			{
				holder.tvIssue.setText("Vol. " + Integer.toString(issue));
				holder.tvIssue.setVisibility(View.VISIBLE);
			}
			else
			{
				holder.tvIssue.setVisibility(View.GONE);
			}
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