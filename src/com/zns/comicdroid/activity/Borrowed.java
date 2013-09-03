package com.zns.comicdroid.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Checkable;
import android.widget.ListView;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.zns.comicdroid.BaseFragmentActivity;
import com.zns.comicdroid.R;
import com.zns.comicdroid.adapter.BorrowedAdapter;
import com.zns.comicdroid.data.Comic;

public class Borrowed extends BaseFragmentActivity
implements OnItemClickListener {

	private BorrowedAdapter mAdapter;
	private ListView mLvComics;
	private ActionMode mMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_borrowed);
		super.onCreate(savedInstanceState);

		mLvComics = (ListView)findViewById(R.id.borrowed_lvComics);
		mLvComics.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		mLvComics.setOnItemClickListener(this);

		List<Comic> comics = getDBHelper().getBorrowed();
		mAdapter = new BorrowedAdapter(this, comics, getImagePath(true));
		mLvComics.setAdapter(mAdapter);

		if (comics.size() == 0) {
			findViewById(R.id.borrowed_tvEmpty).setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {		
		if (parent == mLvComics)
		{
			if (((Checkable)view).isChecked()) {
				view.setBackgroundDrawable(getResources().getDrawable(R.drawable.listitem_selected));
			}
			else {
				view.setBackgroundColor(getResources().getColor(R.color.contentBg));
			}
			final float scale = getResources().getDisplayMetrics().density;
			int padding_in_px = (int) (5 * scale + 0.5f);
			view.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);

			SparseBooleanArray checked = mLvComics.getCheckedItemPositions();
			boolean hasCheckedElement = false;
			for (int i = 0 ; i < checked.size() && ! hasCheckedElement ; i++) {
				hasCheckedElement = checked.valueAt(i);
			}

			if (hasCheckedElement) {
				if (mMode == null) {
					mMode = startActionMode(new ModeCallback());
				}
			} else {
				if (mMode != null) {
					mMode.finish();
				}
			}
		}
		else {
			super.onItemClick(parent, view, pos, id);
		}
	}

	private final class ModeCallback implements ActionMode.Callback {    	 
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Create the menu from the xml file
			MenuInflater inflater = getSupportMenuInflater();
			inflater.inflate(R.menu.actionbar_context_delete, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// Here, you can checked selected items to adapt available actions
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// Destroying action mode, let's unselect all items
			for (int i = 0; i < mLvComics.getAdapter().getCount(); i++)
				mLvComics.setItemChecked(i, false);

			if (mode == mMode) {
				mMode = null;
			}
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			SparseBooleanArray checked = mLvComics.getCheckedItemPositions();
			List<Comic> selected = new ArrayList<Comic>();
			for (int i = 0 ; i < checked.size(); i++) {
				if (checked.valueAt(i))
				{
					selected.add(mAdapter.getComic(i));
				}
			}

			if (selected.size() > 0)
			{
				int[] arr = new int[selected.size()];
				for (int i = 0; i < selected.size(); i++) {
					arr[i] = selected.get(i).getId();
					mAdapter.remove(selected.get(i));
				}
				getDBHelper().setComicReturned(arr);
				mLvComics.clearChoices();
				mLvComics.setAdapter(mAdapter);
			}

			if (mAdapter.getCount() == 0) {
				findViewById(R.id.borrowed_tvEmpty).setVisibility(View.VISIBLE);
			}

			mode.finish();
			return true;
		}
	};	
}