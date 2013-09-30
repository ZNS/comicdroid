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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;

import com.zns.comicdroid.BaseFragmentActivity;
import com.zns.comicdroid.R;
import com.zns.comicdroid.adapter.ExpandableGroupAdapter;
import com.zns.comicdroid.amazon.AmazonSearchTask;
import com.zns.comicdroid.data.Comic;
import com.zns.comicdroid.data.Group;

public class WatchedGroups extends BaseFragmentActivity
implements OnItemClickListener {

	private ExpandableGroupAdapter mAdapter;
	private ExpandableListView mElvGroups;
	private int mTaskCount = 0;
	
	private synchronized void updateList(int total) {
		mTaskCount++;
		if (mTaskCount == total) {
			mAdapter.notifyDataSetChanged();
			mTaskCount = 0;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_watched_groups);
		super.onCreate(savedInstanceState);

		mElvGroups = (ExpandableListView)findViewById(R.id.watched_elvGroups);
		mElvGroups.setOnItemClickListener(this);

		List<Group> groups = getDBHelper().getGroupsWatched();
		mAdapter = new ExpandableGroupAdapter(this, groups, getImagePath(true));
		mElvGroups.setAdapter(mAdapter);
		
		if (groups.size() == 0) {
			findViewById(R.id.watched_tvEmpty).setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {		
		if (parent == mElvGroups)
		{
			Group group = (Group)mAdapter.getGroup(pos);
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
	
	public void searchAmazon(View view) {
		String cachePath = getExternalFilesDir(null).toString() + "/amazoncache";
		final int groupCount = mAdapter.getGroupCount();
		for (int i = 0; i < groupCount; i++)
		{
			Group group = (Group)mAdapter.getGroup(i);
			Comic last = getDBHelper().getLastIssue(group.getId());
			if (last != null && last.getIssue() > 0) {
				String author = last.getAuthor();
				if (author != null && author.length() > 0) {
					String[] parts = author.split(" ");
					author = parts[parts.length - 1];
				}
				
				AmazonSearchTask.AmazonSearchTaskRequest req = new AmazonSearchTask.AmazonSearchTaskRequest();
				req.index = i;
				req.query = (author != null && author.length() > 0 ? "author:" + author + " " : "") + "title:\"" + last.getTitle() + "\" " + (last.getIssue() + 1);
				req.issue = last.getIssue() + 1;
				req.cachePath = cachePath;
				new AmazonSearchTask() {
					@Override
					protected void onPostExecute(AmazonSearchTask.AmazonSearchTaskResponse result) {
						((Group)mAdapter.getGroup(result.index)).setAmazonBooks(result.books);
						updateList(groupCount);
					}
				}.execute(req);
			}
		}
	}
}
