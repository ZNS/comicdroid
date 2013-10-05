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

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

import com.amazon.device.associates.AssociatesAPI;
import com.amazon.device.associates.LinkService;
import com.amazon.device.associates.NotInitializedException;
import com.amazon.device.associates.OpenProductPageRequest;
import com.zns.comicdroid.Application;
import com.zns.comicdroid.BaseFragmentActivity;
import com.zns.comicdroid.R;
import com.zns.comicdroid.adapter.ExpandableGroupAdapter;
import com.zns.comicdroid.amazon.AmazonSearchTask;
import com.zns.comicdroid.amazon.Book;
import com.zns.comicdroid.data.Comic;
import com.zns.comicdroid.data.Group;

import de.greenrobot.event.EventBus;

public class WatchedGroups extends BaseFragmentActivity
implements OnItemClickListener, OnChildClickListener, OnGroupClickListener {

	private static final String STATE_SEARCHING = "ISSEARCHING";
	
	private ExpandableGroupAdapter mAdapter;
	private ExpandableListView mElvGroups;
	private int mTaskCount = 0;
	private ProgressDialog mProgress = null;
	
	private synchronized void updateList(int total) {
		mTaskCount++;
		if (mTaskCount == total) {
			if (mProgress!= null && mProgress.isShowing()) {
				mProgress.dismiss();
			}			
			mAdapter.notifyDataSetChanged();
			mTaskCount = 0;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_watched_groups);
		super.onCreate(savedInstanceState);

		mElvGroups = (ExpandableListView)findViewById(R.id.watched_elvGroups);
		mElvGroups.setOnGroupClickListener(this);
		mElvGroups.setOnChildClickListener(this);

		List<Group> groups = getDBHelper().getGroupsWatched();
		mAdapter = new ExpandableGroupAdapter(this, groups, getImagePath(true));
		mElvGroups.setAdapter(mAdapter);
		
		//Amazon
		AssociatesAPI.initialize(new AssociatesAPI.Config(Application.AMAZON_APPLICATION_KEY, this));
	
		//State
		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_SEARCHING)) {
			if (savedInstanceState.getBoolean(STATE_SEARCHING)) {
				searchAmazon(null);
			}
		}
		
		if (groups.size() == 0) {
			findViewById(R.id.watched_tvEmpty).setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		state.putBoolean(STATE_SEARCHING, true);
	} 
	
	@Override 
	protected void onResume() {
		EventBus.getDefault().register(this, "onAmazonRowClick", ExpandableGroupAdapter.AmazonRowClickEvent.class);
		super.onResume();
	}

	@Override 
	protected void onPause() {
		EventBus.getDefault().unregister(this, ExpandableGroupAdapter.AmazonRowClickEvent.class);
		super.onPause();
	}
	
	public void searchAmazon(View view) {
		String cachePath = getExternalFilesDir(null).toString() + "/amazoncache";
		final int groupCount = mAdapter.getGroupCount();
		//Show progress bar
		if (view != null)
		{
			if (mProgress == null) {
				mProgress = new ProgressDialog(this);
				mProgress.setTitle(R.string.amazon_search_progress);
				mProgress.setCancelable(false);
				mProgress.setIndeterminate(true);
			}
			mProgress.show();
		}
		//Do a search for each groupd
		for (int i = 0; i < groupCount; i++)
		{
			Group group = (Group)mAdapter.getGroup(i);
			Comic last = getDBHelper().getLastIssue(group.getId());
			if (last != null && last.getIssue() > 0) {
				AmazonSearchTask.AmazonSearchTaskRequest req = new AmazonSearchTask.AmazonSearchTaskRequest();
				req.associateTag = getString(R.string.key_amazon_associate_tag);
				req.awsKey = getString(R.string.key_amazon_api_key);
				req.awsSecret = getString(R.string.key_amazon_api_secret);
				req.index = i;
				req.query = AmazonSearchTask.getNextIssueQuery(last);
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
			else {
				updateList(groupCount);
			}
		}
	}
	
	@Override
	public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
		Group group = (Group)mAdapter.getGroup(groupPosition);
		if (group != null) {
			Intent intent = new Intent(this, Comics.class);
			intent.putExtra(Comics.INTENT_COMICS_TYPE, Comics.VIEWTYPE_GROUP);
			intent.putExtra(Comics.INTENT_COMICS_VALUE, Integer.toString(group.getId()));
			intent.putExtra(Comics.INTENT_COMICS_HEADING, group.getName());
			intent.putExtra(Comics.INTENT_COMICS_ID, group.getId());
			startActivity(intent);
			return true;
		}		
		return false;
	}
	
	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		Book book = (Book)mAdapter.getChild(groupPosition, childPosition);
	    OpenProductPageRequest request = new OpenProductPageRequest(book.Id);
        try {
            LinkService linkService = AssociatesAPI.getLinkService();
            linkService.openRetailPage(request);
            return true;
        }
        catch (NotInitializedException e) {
            e.printStackTrace();
        }		
		return false;
	}
	
	public void onAmazonRowClickMainThread(ExpandableGroupAdapter.AmazonRowClickEvent result) {
		if (mElvGroups.expandGroup(result.position)) {
			mElvGroups.setSelectedGroup(result.position);
		}
		else {
			mElvGroups.collapseGroup(result.position);
		}
	}
}
