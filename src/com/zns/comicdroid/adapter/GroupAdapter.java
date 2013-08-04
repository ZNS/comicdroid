package com.zns.comicdroid.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zns.comicdroid.R;
import com.zns.comicdroid.data.Group;
import com.zns.comicdroid.util.ImageWorker;

public class GroupAdapter extends ArrayAdapter<Group> {
	private final List<Group> values;
	private final ImageWorker imageWorker = new ImageWorker();
	private final LayoutInflater inflater;
	
	static class GroupHolder
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
	  
	public GroupAdapter(Context context, List<Group> values) 
	{
		super(context, R.layout.list_comicrow, values);
		inflater = LayoutInflater.from(context);		
		this.values = values;
	}
	  
	public Group getGroup(int position)
	{
		if (position < values.size())
			return values.get(position);
		return null;
	}
	  
	public List<Group> getAll(){
		return this.values;  
	}	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		GroupHolder holder = null;
		
		if (convertView == null)
		{	    	
			convertView = inflater.inflate(R.layout.list_comicrow, parent, false);
				
			holder = new GroupHolder();
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
			holder = (GroupHolder)convertView.getTag();
		}
		
		Group group = values.get(position);
		holder.tvTitle.setText(group.getName());
		holder.ivGroupFinished.setVisibility(group.getIsFinished() ? View.VISIBLE : View.GONE);
		holder.ivGroupCompleted.setVisibility(group.getIsComplete() ? View.VISIBLE : View.GONE);
		holder.ivGroupWatched.setVisibility(group.getIsWatched() ? View.VISIBLE : View.GONE);		
		holder.ivGroupMark.setVisibility(View.VISIBLE);
		holder.tvCount.setText("(" + group.getBookCount() + (group.getTotalBookCount() > 0 ? "/" + group.getTotalBookCount() : "") + ")");		
		if (group.getImage() != null)
		{
			imageWorker.load(group.getImage(), holder.ivImage);
			holder.ivImage.setVisibility(View.VISIBLE);
		}
		else
		{
			holder.ivImage.setVisibility(View.GONE);
		}
		
		return convertView;
	}
}
