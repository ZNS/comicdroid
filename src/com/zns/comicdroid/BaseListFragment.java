package com.zns.comicdroid;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.commonsware.cwac.loaderex.acl.SQLiteCursorLoader;
import com.zns.comicdroid.data.DBHelper;

public abstract class BaseListFragment extends BaseFragment 
	implements	LoaderCallbacks<Cursor> {
	
	public interface OnListLoadedListener {
		public void onListLoaded();
	}
	
	private static final String STATE_SCROLLY = "LISTVIEW_SCROLL_Y";
	private static final String STATE_FILTER = "FILTER";
	public OnListLoadedListener listLoadedCallback = null;
	public SimpleCursorAdapter adapter;	
	private String filter;
	private int[] scrollPos = new int[] { 0, 0 };
	private boolean doScroll = false;
	protected String orderBy;
	protected ListView listView;
	protected int index;	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);	
		if (activity instanceof OnListLoadedListener) {
			listLoadedCallback = (OnListLoadedListener)activity;
		}
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        Bundle args = getArguments();
        if (args != null) {
            index = args.getInt("index");
        }
        if (savedInstanceState != null)
        {
        	scrollPos = savedInstanceState.getIntArray(STATE_SCROLLY);
        	filter = savedInstanceState.getString(STATE_FILTER);
        }
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_baselist, container, false);	
		listView = (ListView)view.findViewById(R.id.fragment_baseListView);
		return view;
	}
	
	@Override
	public void onActivityCreated (Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (getActivity().getSupportLoaderManager().getLoader(index) == null)
			getActivity().getSupportLoaderManager().initLoader(index,  null, this);
		else
			getActivity().getSupportLoaderManager().restartLoader(index,  null, this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		scrollPos[0] = listView.getFirstVisiblePosition();
		View v = listView.getChildAt(0);
		scrollPos[1] = (v == null) ? 0 : v.getTop();
	}
	
	@Override
	public void onResume() {
		doScroll = true;
		super.onResume();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    outState.putIntArray(STATE_SCROLLY, scrollPos);
	    outState.putString(STATE_FILTER, filter);
	}
		
	@Override
	public void onDestroy() {
		Loader<Cursor> loader = getActivity().getSupportLoaderManager().getLoader(index);
		if (loader != null) {
			loader.abandon();
			getActivity().getSupportLoaderManager().destroyLoader(index);
		}
		super.onDestroy();
	}
	
	public String getFilter() {
		return this.filter;
	}
	
	public void setFilter(String filter) {		
		this.filter = filter;
		this.update();
	}
	
	public void clearFilter() {
		this.setFilter(null);
	}
	
	public void update() {
		getActivity().getSupportLoaderManager().restartLoader(index,  null, this);
	}
	
	public void BindList()
	{
		listView.setAdapter(adapter);
		if (doScroll)
		{
			listView.setSelectionFromTop(scrollPos[0], scrollPos[1]);
			doScroll = false;
		}
	}
	
	public String getSQLDefault() {
		return "";
	}
	
	public String getSQLFilter() {
		return "";
	} 

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
		this.update();
	}
	
	public String getOrderBy() {
		return orderBy;
	}
	
	public int[] getItemIds() {
		return null;
	}
	
	//Loader Implementation
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Loader<Cursor> loader = null;
		if (filter != null && !filter.equals("")) {
			String sql = getSQLFilter();
			List<String> params = new ArrayList<String>();
			int paramIndex = -1;
			while((paramIndex = sql.indexOf("?", paramIndex + 1)) > -1) {
				params.add("%" + filter + "%");
			}
			loader = new SQLiteCursorLoader(getActivity(), DBHelper.getHelper(getActivity()), appendOrderBy(getSQLFilter()), params.toArray(new String[params.size()]));
		}
		else {
			loader = new SQLiteCursorLoader(getActivity(), DBHelper.getHelper(getActivity()), appendOrderBy(getSQLDefault()), null); 
		}
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		adapter.changeCursor(cursor);
		BindList();
		if (listLoadedCallback != null) {
			listLoadedCallback.onListLoaded();
		}		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.changeCursor(null);
		BindList();
	}	
	
	private String appendOrderBy(String sql)
	{
		if (!sql.toLowerCase(Locale.ENGLISH).contains("order by")) {
			return sql + " ORDER BY " + orderBy;
		}
		return sql;
	}
}
