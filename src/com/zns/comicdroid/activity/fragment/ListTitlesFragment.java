package com.zns.comicdroid.activity.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.zns.comicdroid.BaseListFragment;
import com.zns.comicdroid.activity.ComicView;
import com.zns.comicdroid.adapter.ComicAdapter;

public class ListTitlesFragment extends BaseListFragment {

	public static ListTitlesFragment newInstance(int index)
	{
		ListTitlesFragment fragment = new ListTitlesFragment();
		Bundle b = new Bundle();
		b.putInt("index", index);
		fragment.setArguments(b);
		fragment.orderBy = "Title, Issue";
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		
		adapter = new ComicAdapter(getActivity());
		listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) 
			{
				int comicId = getAdapter().getComicId(position);
				Intent intent = new Intent(getActivity(), ComicView.class);
				intent.putExtra(ComicView.INTENT_COMIC_ID, comicId);
				startActivity(intent);
			}
		});		
		registerForContextMenu(listView);
		
		return view;
	}

	private ComicAdapter getAdapter()
	{
		return (ComicAdapter)adapter;
	}
	
	@Override
	public String getSQLDefault() {
		return "SELECT _id, Title, Subtitle, Author, Image, Issue, IsBorrowed FROM tblBooks";
	}
	
	@Override
	public String getSQLFilter() {
		return "SELECT _id, Title, Subtitle, Author, Image, Issue, IsBorrowed FROM tblBooks WHERE Title LIKE ?";
	}
	
	@Override
	public int[] getItemIds() {
		int[] ids = null;
		if (adapter.getCursor() != null)
		{
			Cursor cursor = adapter.getCursor();
			ids = new int[cursor.getCount()];
			int i = 0;
			cursor.moveToPosition(-1);
			while(cursor.moveToNext()) {
				ids[i] = cursor.getInt(0);
				i++;
			}
		}
		return ids;
	}	
}
