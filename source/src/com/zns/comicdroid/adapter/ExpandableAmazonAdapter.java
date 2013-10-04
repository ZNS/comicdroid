package com.zns.comicdroid.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.zns.comicdroid.R;
import com.zns.comicdroid.amazon.Book;

public class ExpandableAmazonAdapter extends BaseExpandableListAdapter {
	private final List<Book> mValues;
	private final LayoutInflater mInflater;
			
	class BookHolder
	{
		ImageView ivImage;
		TextView tvTitle;
		TextView tvDate;
		TextView tvPrice;
	}
	
	public ExpandableAmazonAdapter(Context context, List<Book> values) 
	{
		mInflater = LayoutInflater.from(context);		
		this.mValues = values;
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		if (groupPosition == 0) {
			return mValues.get(childPosition);
		}
		return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		Object child = getChild(groupPosition, childPosition);
		if (child != null) {
			return ((Book)child).Id.hashCode();
		}
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		BookHolder holder = null;
		if (convertView == null)
		{	    	
			convertView = mInflater.inflate(R.layout.list_amazonbookrow, parent, false);
			holder = new BookHolder();
			holder.ivImage = (ImageView)convertView.findViewById(R.id.ivAmazonBook);
			holder.tvTitle = (TextView)convertView.findViewById(R.id.tvAmazonBookTitle);
			holder.tvDate = (TextView)convertView.findViewById(R.id.tvAmazonBookDate);
			holder.tvPrice = (TextView)convertView.findViewById(R.id.tvAmazonBookPrice);
			convertView.setTag(holder);
		}
		else
		{
			holder = (BookHolder)convertView.getTag();
		}		
		
		Book book = (Book)getChild(groupPosition, childPosition);
		if (book != null) {
			holder.tvTitle.setText(book.Title);
			holder.tvPrice.setText(book.Price);
			holder.tvDate.setText(book.PublicationDate);
			if (book.ImageUrl != null) {
				ImageLoader.getInstance().displayImage(book.ImageUrl, holder.ivImage);
				holder.ivImage.setVisibility(View.VISIBLE);
			}
			else {
				holder.ivImage.setVisibility(View.GONE);
			}
		}
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if (groupPosition == 0) {
			return mValues.size();
		}
		return 0;
	}

	@Override
	public Object getGroup(int position) {
		return mValues.get(0);
	}

	@Override
	public int getGroupCount() {
		return 1;
	}

	@Override
	public long getGroupId(int position) {
		return mValues.get(0).Id.hashCode();
	}

	@Override
	public View getGroupView(int position, boolean isExpanded, View convertView, ViewGroup parent) {
		BookHolder holder = null;
		if (convertView == null)
		{	    	
			convertView = mInflater.inflate(R.layout.list_amazongroup, parent, false);
			holder = new BookHolder();
			holder.ivImage = (ImageView)convertView.findViewById(R.id.ivAmazonGroup);
			holder.tvTitle = (TextView)convertView.findViewById(R.id.tvAmazonGroup);
			convertView.setTag(holder);
		}
		else
		{
			holder = (BookHolder)convertView.getTag();
		}		
		
		Book book = (Book)getGroup(position);
		if (book != null) {
			holder.tvTitle.setText(book.Title);
			if (book.ImageUrl != null) {
				ImageLoader.getInstance().displayImage(book.ImageUrl, holder.ivImage);
				holder.ivImage.setVisibility(View.VISIBLE);
			}
			else {
				holder.ivImage.setVisibility(View.GONE);
			}
		}
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		return true;
	}
	
	public List<Book> getAllChildren(int groupPosition) {
		if (groupPosition == 0 && mValues != null) {
			return mValues;
		}
		return null;
	}
}