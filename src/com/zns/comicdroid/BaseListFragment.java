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
	public OnListLoadedListener mListLoadedCallback = null;
	public SimpleCursorAdapter mAdapter;	
	private String mFilter;
	private int[] mScrollPos = new int[] { 0, 0 };
	private boolean mDoScroll = false;
	protected String mOrderBy;
	protected ListView mListView;
	protected int mIndex;	

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);	
		if (activity instanceof OnListLoadedListener) {
			mListLoadedCallback = (OnListLoadedListener)activity;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);        
		Bundle args = getArguments();
		if (args != null) {
			mIndex = args.getInt("index");
		}
		if (savedInstanceState != null)
		{
			mScrollPos = savedInstanceState.getIntArray(STATE_SCROLLY);
			mFilter = savedInstanceState.getString(STATE_FILTER);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_baselist, container, false);	
		mListView = (ListView)view.findViewById(R.id.fragment_baseListView);
		return view;
	}

	@Override
	public void onActivityCreated (Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (getActivity().getSupportLoaderManager().getLoader(mIndex) == null)
			getActivity().getSupportLoaderManager().initLoader(mIndex,  null, this);
		else
			getActivity().getSupportLoaderManager().restartLoader(mIndex,  null, this);
	}

	@Override
	public void onPause() {
		super.onPause();
		mScrollPos[0] = mListView.getFirstVisiblePosition();
		View v = mListView.getChildAt(0);
		mScrollPos[1] = (v == null) ? 0 : v.getTop();
	}

	@Override
	public void onResume() {
		mDoScroll = true;
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putIntArray(STATE_SCROLLY, mScrollPos);
		outState.putString(STATE_FILTER, mFilter);
	}

	@Override
	public void onDestroy() {
		Loader<Cursor> loader = getActivity().getSupportLoaderManager().getLoader(mIndex);
		if (loader != null) {
			loader.abandon();
			getActivity().getSupportLoaderManager().destroyLoader(mIndex);
		}
		super.onDestroy();
	}

	public String getFilter() {
		return this.mFilter;
	}

	public void setFilter(String filter) {		
		this.mFilter = filter;
		this.update();
	}

	public void clearFilter() {
		this.setFilter(null);
	}

	public void update() {
		getActivity().getSupportLoaderManager().restartLoader(mIndex,  null, this);
	}

	public void BindList()
	{
		mListView.setAdapter(mAdapter);
		if (mDoScroll)
		{
			mListView.setSelectionFromTop(mScrollPos[0], mScrollPos[1]);
			mDoScroll = false;
		}
	}

	public String getSQLDefault() {
		return "";
	}

	public String getSQLFilter() {
		return "";
	} 

	public void setOrderBy(String orderBy) {
		this.mOrderBy = orderBy;
		this.update();
	}

	public String getOrderBy() {
		return mOrderBy;
	}

	public int[] getItemIds() {
		return null;
	}

	public boolean hasItems() {
		if (mAdapter.getCursor() != null)
		{
			int pos = mAdapter.getCursor().getPosition();
			if (pos <= 0 && !mAdapter.getCursor().moveToFirst()) {
				return false;
			}
		}
		return true;
	}

	//Loader Implementation
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Loader<Cursor> loader = null;
		if (mFilter != null && !mFilter.equals("")) {
			String sql = getSQLFilter();
			List<String> params = new ArrayList<String>();
			int paramIndex = -1;
			while((paramIndex = sql.indexOf("?", paramIndex + 1)) > -1) {
				params.add("%" + mFilter + "%");
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
		mAdapter.changeCursor(cursor);
		BindList();
		if (mListLoadedCallback != null) {
			mListLoadedCallback.onListLoaded();
		}		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
		BindList();
	}	

	private String appendOrderBy(String sql)
	{
		if (!sql.toLowerCase(Locale.ENGLISH).contains("order by")) {
			return sql + " ORDER BY " + mOrderBy;
		}
		return sql;
	}
}
