package com.zns.comicdroid.data;

import java.util.List;

import com.zns.comicdroid.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ComicArrayAdapter extends ArrayAdapter<Comic> {
	  private final Context context;
	  private final List<Comic> values;

	  static class ComicHolder
	  {
		  TextView tvTitle;
		  TextView tvAuthor;
		  ImageView ivImage;
		  TextView tvIssue;
	  }
	  
	  public ComicArrayAdapter(Context context, List<Comic> values) 
	  {
	    super(context, R.layout.list_comicrow, values);
	    this.context = context;
	    this.values = values;
	  }
	  
	  public Comic getComic(int position)
	  {
		  if (position < values.size())
			  return values.get(position);
		  return null;
	  }
	  
	  public View getView(int position, View convertView, ViewGroup parent) 
	  {		  
		  View row = convertView;
		  ComicHolder holder = null;
		  
		  if (row == null)
		  {
			  LayoutInflater inflater = ((Activity)context).getLayoutInflater();
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
		  
		  Comic comic = values.get(position);
		  holder.tvTitle.setText(comic.getTitle() + (comic.getIssue() > 0 && comic.getSubTitle() != null ? " - " + comic.getSubTitle() : ""));
		  holder.tvAuthor.setText(comic.getAuthor());
		  if (comic.getIssue() > 0)
		  {
			  holder.tvIssue.setText("Vol. " + Integer.toString(comic.getIssue()));
			  holder.tvIssue.setVisibility(View.VISIBLE);
		  }
		  else
		  {
			  holder.tvIssue.setVisibility(View.GONE);
		  }
		  if (comic.getImage() != null)
		  {
			  Bitmap bmp = BitmapFactory.decodeByteArray(comic.getImage(), 0, comic.getImage().length);
			  holder.ivImage.setImageBitmap(bmp);
			  holder.ivImage.setVisibility(View.VISIBLE);
		  }
		  else
		  {
			  holder.ivImage.setVisibility(View.GONE);
		  }
		  
		  return row;
	  }
}
