package com.zns.comicdroid.adapter;

import java.util.List;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.zns.comicdroid.R;
import com.zns.comicdroid.amazon.Book;
import com.zns.comicdroid.data.Group;

import de.greenrobot.event.EventBus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ExpandableGroupAdapter extends BaseExpandableListAdapter {
	private final List<Group> mValues;
	private final LayoutInflater mInflater;
	private final String mImagePath;
			
	class GroupHolder
	{
		TextView tvTitle;
		TextView tvAuthor;
		ImageView ivImage;
		TextView tvCount;
		ImageView ivGroupMark;
		ImageView ivGroupWatched;
		ImageView ivGroupCompleted;
		ImageView ivGroupFinished;
		RelativeLayout rlAmazon;
		ImageView ivAmazon;
		TextView tvAmazon;
		View vSeparator;
	}
	
	class BookHolder
	{
		ImageView ivImage;
		TextView tvTitle;
		TextView tvDate;
		TextView tvPrice;
	}
	
	public class AmazonRowClickEvent {
		public int position;
		
		public AmazonRowClickEvent(int position) {
			this.position = position;
		}
	}
	
	public ExpandableGroupAdapter(Context context, List<Group> values, String imagePath) 
	{
		mInflater = LayoutInflater.from(context);		
		this.mValues = values;
		this.mImagePath = "file://".concat(imagePath);
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		if (mValues.get(groupPosition) != null && mValues.get(groupPosition).getAmazonBooks() != null) {
			return mValues.get(groupPosition).getAmazonBooks().get(childPosition);
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
		Object o = getGroup(groupPosition);
		if (o != null) {
			Group group = (Group)getGroup(groupPosition);
			if (group.getAmazonBooks() != null) {
				return group.getAmazonBooks().size();
			}
		}
		return 0;
	}

	@Override
	public Object getGroup(int position) {
		return mValues.get(position);
	}

	@Override
	public int getGroupCount() {
		return mValues.size();
	}

	@Override
	public long getGroupId(int position) {
		return mValues.get(position).getId();
	}

	@Override
	public View getGroupView(int position, boolean isExpanded, View convertView, ViewGroup parent) {
		GroupHolder holder = null;

		if (convertView == null)
		{	    	
			convertView = mInflater.inflate(R.layout.list_comicrow, parent, false);

			holder = new GroupHolder();
			holder.tvTitle = (TextView)convertView.findViewById(R.id.tvTitle);
			holder.tvAuthor = (TextView)convertView.findViewById(R.id.tvAuthor);
			holder.ivImage = (ImageView)convertView.findViewById(R.id.ivImage);
			holder.tvCount = (TextView)convertView.findViewById(R.id.tvCount);
			holder.ivGroupMark = (ImageView)convertView.findViewById(R.id.ivGroupMark);
			holder.rlAmazon = (RelativeLayout)convertView.findViewById(R.id.rlAmazon);
			holder.ivGroupCompleted = (ImageView)convertView.findViewById(R.id.ivGroupCompleted);
			holder.ivGroupFinished = (ImageView)convertView.findViewById(R.id.ivGroupFinished);
			holder.ivGroupWatched = (ImageView)convertView.findViewById(R.id.ivGroupWatched);
			holder.ivAmazon = (ImageView)convertView.findViewById(R.id.ivComicAmazon);
			holder.tvAmazon = (TextView)convertView.findViewById(R.id.tvComicAmazon);
			holder.vSeparator = convertView.findViewById(R.id.vSeparatorBottom);
			
			//Event for amazon row
			holder.rlAmazon.setTag(R.id.TAG_GROUP_POSITION, position); //Track position for click event
			holder.rlAmazon.setTag(R.id.TAG_GROUP_EXPANDED, isExpanded); //Track if group is expanded
			holder.rlAmazon.setOnClickListener(new OnClickListener() {	
				@Override
				public void onClick(View v) {
					final ImageView img = (ImageView)v.findViewById(R.id.ivComicAmazonHandle);
					if ((Boolean)v.getTag(R.id.TAG_GROUP_EXPANDED) == true) {
						img.setImageResource(R.drawable.amazon_arrow_expand);
					}
					else {
						img.setImageResource(R.drawable.amazon_arrow_collapse);
					}
					EventBus.getDefault().post(new AmazonRowClickEvent((Integer)v.getTag(R.id.TAG_GROUP_POSITION)));
				}
			});
			
			convertView.setTag(holder);
		}
		else
		{
			holder = (GroupHolder)convertView.getTag();
			holder.rlAmazon.setTag(R.id.TAG_GROUP_POSITION, position); //Track position for click event
			holder.rlAmazon.setTag(R.id.TAG_GROUP_EXPANDED, isExpanded); //Track if group is expanded
		}

		Group group = (Group)getGroup(position);
		holder.vSeparator.setVisibility(View.VISIBLE);
		holder.tvTitle.setText(group.getName());
		holder.ivGroupFinished.setVisibility(group.getIsFinished() ? View.VISIBLE : View.GONE);
		holder.ivGroupCompleted.setVisibility(group.getIsComplete() ? View.VISIBLE : View.GONE);
		holder.ivGroupWatched.setVisibility(group.getIsWatched() ? View.VISIBLE : View.GONE);		
		holder.ivGroupMark.setVisibility(View.VISIBLE);
		holder.tvCount.setText("(" + group.getBookCount() + (group.getTotalBookCount() > 0 ? "/" + group.getTotalBookCount() : "") + ")");		
		if (group.getImage() != null && !group.getImage().equals("")) {
			ImageLoader.getInstance().displayImage(mImagePath.concat(group.getImage()), holder.ivImage);
			holder.ivImage.setVisibility(View.VISIBLE);
		}
		else {
			holder.ivImage.setVisibility(View.GONE);
		}
		if (group.getAmazonBooks() != null && group.getAmazonBooks().size() > 0) {
			Book book = group.getAmazonBooks().get(0);
			holder.rlAmazon.setVisibility(View.VISIBLE);
			holder.tvAmazon.setText(book.Title);
			if (book.ImageUrl != null) {
				ImageLoader.getInstance().displayImage(book.ImageUrl, holder.ivAmazon);
				holder.ivAmazon.setVisibility(View.VISIBLE);
			}
			else {
				holder.ivAmazon.setVisibility(View.GONE);
			}
		}
		else {
			holder.rlAmazon.setVisibility(View.GONE);
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
}
