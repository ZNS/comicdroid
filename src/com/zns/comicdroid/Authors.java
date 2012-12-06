package com.zns.comicdroid;

import com.zns.comicdroid.data.GroupedItemAdapter;

import android.os.Bundle;

public class Authors extends BaseListFragmentActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	 

		adapter = new GroupedItemAdapter(this);
		listView.setAdapter(adapter);		
	}
	
	private GroupedItemAdapter getAdapter()
	{
		return (GroupedItemAdapter)adapter;
	}
	
	@Override
	protected String getSQLDefault() {
		return "SELECT 0 AS _id, Author AS Name, COUNT(*) AS Count FROM tblBooks GROUP BY Author ORDER BY Author";
	}
	
	@Override
	protected String getSQLFilter() {
		return "SELECT 0 AS _id, Author AS Name, COUNT(*) AS Count FROM tblBooks WHERE Author LIKE ? GROUP BY Author ORDER BY Author";
	}	
}