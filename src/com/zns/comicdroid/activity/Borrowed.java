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
	
	private BorrowedAdapter adapter;
	private ListView lvComics;
	private ActionMode mMode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_borrowed);
		
		lvComics = (ListView)findViewById(R.id.borrowed_lvComics);
		lvComics.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		lvComics.setOnItemClickListener(this);
		
		List<Comic> comics = getDBHelper().getBorrowed();
		adapter = new BorrowedAdapter(this, comics);
		lvComics.setAdapter(adapter);
		
		if (comics.size() == 0) {
			findViewById(R.id.borrowed_tvEmpty).setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {		
		if (((Checkable)view).isChecked()) {
			view.setBackgroundDrawable(getResources().getDrawable(R.drawable.listitem_selected));
		}
		else {
			view.setBackgroundColor(getResources().getColor(R.color.contentBg));
		}
	    final float scale = getResources().getDisplayMetrics().density;
	    int padding_in_px = (int) (5 * scale + 0.5f);
		view.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);
		
		SparseBooleanArray checked = lvComics.getCheckedItemPositions();
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
            for (int i = 0; i < lvComics.getAdapter().getCount(); i++)
                lvComics.setItemChecked(i, false);
 
            if (mode == mMode) {
                mMode = null;
            }
        }
 
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        	SparseBooleanArray checked = lvComics.getCheckedItemPositions();
        	List<Comic> selected = new ArrayList<Comic>();
        	for (int i = 0 ; i < checked.size(); i++) {
        		if (checked.valueAt(i))
        		{
        			selected.add(adapter.getComic(i));
        		}
        	}
        	
        	if (selected.size() > 0)
        	{
        		int[] arr = new int[selected.size()];
        		for (int i = 0; i < selected.size(); i++) {
        			arr[i] = selected.get(i).getId();
        			adapter.remove(selected.get(i));
        		}
        		getDBHelper().setComicReturned(arr);
        		lvComics.clearChoices();
        		lvComics.setAdapter(adapter);
        	}
        	
    		if (adapter.getCount() == 0) {
    			findViewById(R.id.borrowed_tvEmpty).setVisibility(View.VISIBLE);
    		}
    		
            mode.finish();
            return true;
        }
    };	
}