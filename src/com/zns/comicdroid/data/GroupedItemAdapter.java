package com.zns.comicdroid.data;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zns.comicdroid.R;

public class GroupedItemAdapter extends SimpleCursorAdapter {
	private final int _layout;
	private final LayoutInflater _layoutInflater;
	
	static class GroupedItemHolder
	{
		TextView tvName;
		TextView tvCount;
	}
	  
	public GroupedItemAdapter(Context context)
	{
		super(context, R.layout.list_groupedrow, null, new String[] { "Name" }, null, 0);
		_layout = R.layout.list_groupedrow;
		_layoutInflater = LayoutInflater.from(context);
	}
	
	public int getGroupedItemName(int position)
	{
		Cursor cursor = getCursor();
		if (cursor.moveToPosition(position))
			return cursor.getInt(1);
		return 0;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Cursor cursor = getCursor();
	    if (cursor.moveToPosition(position)) 
	    {
	    	GroupedItemHolder holder;
	    	
	    	if (convertView == null)
	    	{
	    		convertView = _layoutInflater.inflate(_layout, null);	    		
	    		holder = new GroupedItemHolder();
	    		holder.tvName = (TextView)convertView.findViewById(R.id.list_groupedrow_tvName);
	    		holder.tvCount = (TextView)convertView.findViewById(R.id.list_groupedrow_tvCount);
	    		
	    		convertView.setTag(holder);
	    	}
	    	else
	    	{
	    		holder = (GroupedItemHolder)convertView.getTag();
	    	}
	    	
			String name = cursor.getString(1);
			int count = cursor.getInt(2);
			holder.tvName.setText(name);
			holder.tvCount.setText(Integer.toString(count));
	    }
	    
		return convertView;	    
	}
}
