package com.zns.comicdroid.activity.fragment;

import com.zns.comicdroid.BaseListFragment;
import com.zns.comicdroid.activity.Comics;
import com.zns.comicdroid.adapter.GroupedItemAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

public class ListPublishersFragment extends BaseListFragment
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);	 

		adapter = new GroupedItemAdapter(getActivity());	

		listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) 
			{
				String name = getAdapter().getGroupedItemName(position);
				if (name != null)
				{
					Intent intent = new Intent(getActivity(), Comics.class);
					intent.putExtra(Comics.INTENT_COMICS_TYPE, Comics.VIEWTYPE_PUBLISHER);
					intent.putExtra(Comics.INTENT_COMICS_VALUE, name);
					intent.putExtra(Comics.INTENT_COMICS_HEADING, name);
					startActivity(intent);
				}
			}			
		});		
		
		return view;
	}
	
	private GroupedItemAdapter getAdapter()
	{
		return (GroupedItemAdapter)adapter;
	}
	
	@Override
	public String getSQLDefault() {
		return "SELECT 0 AS _id, Publisher AS Name, COUNT(*) AS Count FROM tblBooks GROUP BY Publisher ORDER BY Publisher COLLATE NOCASE";
	}
	
	@Override
	public String getSQLFilter() {
		return "SELECT 0 AS _id, Publisher AS Name, COUNT(*) AS Count FROM tblBooks WHERE Publisher LIKE ? GROUP BY Publisher ORDER BY Publisher COLLATE NOCASE";
	}	
}