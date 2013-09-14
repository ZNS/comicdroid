/*******************************************************************************
 * Copyright (c) 2013 Ulrik Andersson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Ulrik Andersson - initial API and implementation
 ******************************************************************************/
package com.zns.comicdroid.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zns.comicdroid.R;

public class GroupedItemAdapter extends SimpleCursorAdapter {
	private final int mLayout;
	private final LayoutInflater mLayoutInflater;
	private final String mNameNA;

	static class GroupedItemHolder
	{
		TextView tvName;
		TextView tvCount;
	}

	public GroupedItemAdapter(Context context)
	{
		super(context, R.layout.list_groupedrow, null, new String[] { "Name" }, null, 0);
		mLayout = R.layout.list_groupedrow;
		mLayoutInflater = LayoutInflater.from(context);
		mNameNA = context.getString(R.string.list_name_na);
	}

	public String getGroupedItemName(int position)
	{
		Cursor cursor = getCursor();
		if (cursor.moveToPosition(position))
			return cursor.getString(1);
		return null;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Cursor cursor = getCursor();
		if (cursor.moveToPosition(position)) 
		{
			GroupedItemHolder holder;

			if (convertView == null)
			{
				convertView = mLayoutInflater.inflate(mLayout, null);	    		
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
			if (name == null || name.length() == 0) {
				name = mNameNA;
			}
			holder.tvName.setText(name);
			holder.tvCount.setText(Integer.toString(count));
		}

		return convertView;	    
	}
}
