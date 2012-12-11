package com.zns.comicdroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.zns.comicdroid.data.Aggregate;
import com.zns.comicdroid.data.AggregateAdapter;

public class ListAggregatesFragment extends BaseListFragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);	 

		adapter = new AggregateAdapter(getActivity());		
		
		listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) 
			{
				Aggregate aggregate = getAdapter().getAggregate(position);
				if (aggregate != null)
				{
					if (aggregate.getType() == 1)
					{
						Intent intent = new Intent(getActivity(), ComicView.class);
						intent.putExtra("com.zns.comic.COMICID", aggregate.getId());
						startActivity(intent);
					}
					else if (aggregate.getType() == 2)
					{
						Intent intent = new Intent(getActivity(), Comics.class);
						intent.putExtra("com.zns.comic.COMICS_TYPE", Comics.VIEWTYPE_GROUP);
						intent.putExtra("com.zns.comic.COMICS_VALUE", Integer.toString(aggregate.getId()));
						intent.putExtra("com.zns.comic.COMICS_HEADING", aggregate.getTitle());
						startActivity(intent);
					}
				}
			}			
		});
		
		return view;
	}
	
	private AggregateAdapter getAdapter() {
		return (AggregateAdapter)adapter;
	}
	
	@Override
	protected String getSQLDefault() {
		return "SELECT Id AS _id, Title, Subtitle, Author, Image, 1 AS ItemType, 0 AS BookCount FROM tblBooks WHERE GroupId = 0 OR ifnull(GroupId, '') = '' " +
				"UNION " +
				"SELECT Id AS _id, Name AS Title, '' AS Subtitle, '' AS Author, Image, 2 AS ItemType, BookCount FROM tblGroups " +
				"ORDER BY Title";
	}
	
	@Override
	protected String getSQLFilter() {
		return getSQLDefault();
	}	
}