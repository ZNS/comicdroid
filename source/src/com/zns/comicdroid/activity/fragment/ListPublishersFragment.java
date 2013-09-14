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
package com.zns.comicdroid.activity.fragment;

import android.app.backup.BackupManager;
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

public class ListPublishersFragment extends BaseListFragment
implements RenameDialogFragment.OnRenameDialogListener {

	public static ListPublishersFragment newInstance(int index)
	{
		ListPublishersFragment fragment = new ListPublishersFragment();
		Bundle b = new Bundle();
		b.putInt("index", index);
		fragment.setArguments(b);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);	 

		mAdapter = new GroupedItemAdapter(getActivity());	

		mListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
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
		registerForContextMenu(mListView);

		return view;
	}

	private GroupedItemAdapter getAdapter()
	{
		return (GroupedItemAdapter)mAdapter;
	}

	@Override
	public String getSQLDefault() {
		return "SELECT 0 AS _id, Publisher AS Name, COUNT(*) AS Count FROM tblBooks GROUP BY Publisher ORDER BY Publisher COLLATE NOCASE";
	}

	@Override
	public String getSQLFilter() {
		return "SELECT 0 AS _id, Publisher AS Name, COUNT(*) AS Count FROM tblBooks WHERE Publisher LIKE ? GROUP BY Publisher ORDER BY Publisher COLLATE NOCASE";
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
					dialogRename.show(getActivity().getSupportFragmentManager(), "PUBLISHERRENAME");
					return true;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void onDialogPositiveClick(String oldName, String newName) {
		DBHelper.getHelper(getActivity()).renamePublisher(oldName, newName);
		this.update();
		//Backup
		BackupManager m = new BackupManager(getActivity());
		m.dataChanged();
	}		
}
