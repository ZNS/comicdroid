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
package com.zns.comicdroid.activity;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ListView;

import com.zns.comicdroid.BaseFragmentActivity;
import com.zns.comicdroid.R;
import com.zns.comicdroid.adapter.GroupAdapter;
import com.zns.comicdroid.data.Group;

public class WatchedGroups extends BaseFragmentActivity
implements OnItemClickListener {

	private GroupAdapter mAdapter;
	private ListView mLvGroups;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_watched_groups);
		super.onCreate(savedInstanceState);

		mLvGroups = (ListView)findViewById(R.id.watched_lvGroups);
		mLvGroups.setOnItemClickListener(this);

		List<Group> groups = getDBHelper().getGroupsWatched();
		mAdapter = new GroupAdapter(this, groups, getImagePath(true));
		mLvGroups.setAdapter(mAdapter);

		if (groups.size() == 0) {
			findViewById(R.id.watched_tvEmpty).setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {		
		if (parent == mLvGroups)
		{
			Group group = mAdapter.getGroup(pos);
			if (group != null) {
				Intent intent = new Intent(this, Comics.class);
				intent.putExtra(Comics.INTENT_COMICS_TYPE, Comics.VIEWTYPE_GROUP);
				intent.putExtra(Comics.INTENT_COMICS_VALUE, Integer.toString(group.getId()));
				intent.putExtra(Comics.INTENT_COMICS_HEADING, group.getName());
				intent.putExtra(Comics.INTENT_COMICS_ID, group.getId());
				startActivity(intent);				
			}
		}
		else
		{
			super.onItemClick(parent, view, pos, id);
		}
	}
}
