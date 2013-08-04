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
	
	private GroupAdapter adapter;
	private ListView lvGroups;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_watched_groups);
		super.onCreate(savedInstanceState);
		
		lvGroups = (ListView)findViewById(R.id.watched_lvGroups);
		lvGroups.setOnItemClickListener(this);
		
		List<Group> groups = getDBHelper().getGroupsWatched();
		adapter = new GroupAdapter(this, groups);
		lvGroups.setAdapter(adapter);
		
		if (groups.size() == 0) {
			findViewById(R.id.watched_tvEmpty).setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {		
		if (parent == lvGroups)
		{
			Group group = adapter.getGroup(pos);
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