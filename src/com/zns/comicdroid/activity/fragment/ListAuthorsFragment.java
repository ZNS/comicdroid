package com.zns.comicdroid.activity.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.zns.comicdroid.BaseListFragment;
import com.zns.comicdroid.R;
import com.zns.comicdroid.activity.Comics;
import com.zns.comicdroid.adapter.GroupedItemAdapter;
import com.zns.comicdroid.data.DBHelper;
import com.zns.comicdroid.dialog.RenameDialogFragment;
import com.zns.comicdroid.service.UploadService;

public class ListAuthorsFragment extends BaseListFragment
	implements RenameDialogFragment.OnRenameDialogListener
{
	public static ListAuthorsFragment newInstance(int index)
	{
		ListAuthorsFragment fragment = new ListAuthorsFragment();
		Bundle b = new Bundle();
		b.putInt("index", index);
		fragment.setArguments(b);
		return fragment;
	}
	
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
					intent.putExtra(Comics.INTENT_COMICS_TYPE, Comics.VIEWTYPE_AUTHOR);
					intent.putExtra(Comics.INTENT_COMICS_VALUE, name);
					intent.putExtra(Comics.INTENT_COMICS_HEADING, name);
					startActivity(intent);
				}
			}			
		});
		registerForContextMenu(listView);
		
		return view;
	}
	
	private GroupedItemAdapter getAdapter()
	{
		return (GroupedItemAdapter)adapter;
	}
	
	@Override
	public String getSQLDefault() {
		return "SELECT 0 AS _id, Author AS Name, COUNT(*) AS Count FROM tblBooks GROUP BY Author ORDER BY Author COLLATE NOCASE";
	}
	
	@Override
	public String getSQLFilter() {
		return "SELECT 0 AS _id, Author AS Name, COUNT(*) AS Count FROM tblBooks WHERE Author LIKE ? GROUP BY Author ORDER BY Author COLLATE NOCASE";
	}	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getActivity().getMenuInflater().inflate(R.menu.edit_context_menu, menu);
	}
	
	//Handle click on Context Menu
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (getUserVisibleHint())
		{
			AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
			String name = getAdapter().getGroupedItemName((int)info.position);
			if (name != null)
			{
				switch (item.getItemId()) {
					case R.id.context_edit:
						RenameDialogFragment dialogRename = new RenameDialogFragment();
						dialogRename.setName(name);
						dialogRename.setTargetFragment(this, 0);
						dialogRename.show(getActivity().getSupportFragmentManager(), "AUTHORRENAME");
						return true;
				}
			}
			return true;
		}	
		return false;
	}
	
	@Override
	public void onDialogPositiveClick(String oldName, String newName) {
		DBHelper.getHelper(getActivity()).renameAuthor(oldName, newName);
		this.update();
		//Sync with google drive
		Intent intent = new Intent(getActivity(), UploadService.class);
		getActivity().startService(intent);
	}	
}