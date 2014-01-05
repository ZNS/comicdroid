package com.zns.comicdroid.activity.fragment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.zns.comicdroid.Application;
import com.zns.comicdroid.BaseFragment;
import com.zns.comicdroid.R;
import com.zns.comicdroid.adapter.GCDSeriesAdapter;
import com.zns.comicdroid.gcd.Series;
import com.zns.comicdroid.util.JsonUtil;

public class AddMultipleSearchFragment extends BaseFragment implements OnClickListener, OnItemClickListener {

	private final static int PAGESIZE = 10;
	private EditText mEtSearch;
	private ListView mLvSeries;
	private LinearLayout mListFooter;
	private GCDSeriesAdapter mAdapter;
	private Button mBtnSearch;
	private Button mBtnMore;
	private int mCurrentPage;
	private TextView mTvNoHits;
	private OnSeriesSelectedListener mSeriesSelectedCallback;
	private String mQuery;
	
	public interface OnSeriesSelectedListener {
		public void onSeriesSelected(int seriesId);
	}
	
	public static AddMultipleSearchFragment newInstance() {
		AddMultipleSearchFragment fragment = new AddMultipleSearchFragment();
		fragment.setRetainInstance(true);
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_add_multiple_search, container, false);
		
		mEtSearch = (EditText)view.findViewById(R.id.add_multiple_etSearch);
		mEtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					mCurrentPage = 0;
					mQuery = mEtSearch.getText().toString();
					search();
					return true;
				}
				return false;
			}
		});
		
		mLvSeries = (ListView)view.findViewById(R.id.add_multiple_lvSeries);
		mTvNoHits = (TextView)view.findViewById(R.id.add_multiple_tvNoHits);
		mBtnSearch = (Button)view.findViewById(R.id.add_multiple_btnSearch);
		mBtnSearch.setOnClickListener(this);
				
		mListFooter = (LinearLayout)getSherlockActivity().getLayoutInflater().inflate(R.layout.fragment_add_multiple_search_footer, null);
		mBtnMore = (Button)mListFooter.findViewById(R.id.add_multiple_btnMore);
		mBtnMore.setOnClickListener(this);

		TextView tvGcd = (TextView)view.findViewById(R.id.add_multiple_gcdlink);
		tvGcd.setText(Html.fromHtml("<a href=\"http://www.comics.org\">" + getResources().getString(R.string.gcd_link) + "</a>"));
		tvGcd.setMovementMethod(LinkMovementMethod.getInstance());		
		
		mLvSeries.addFooterView(mListFooter);
		if (mAdapter == null)
		{
			ArrayList<Series> series = new ArrayList<Series>();
			mAdapter = new GCDSeriesAdapter(getActivity(), series);
			mLvSeries.setAdapter(mAdapter);
		}
		else
		{
			mLvSeries.setAdapter(mAdapter);
		}
		mLvSeries.setOnItemClickListener(this);
		
		if (mQuery != null) {
			mEtSearch.setText(mQuery);
		}
		
		if (mAdapter.getCount() < PAGESIZE) {
			mListFooter.setVisibility(View.GONE);
		}
		
		return view;
	}

	@Override
	public void onAttach(Activity activity) {		
		super.onAttach(activity);
		try {
			mSeriesSelectedCallback = (OnSeriesSelectedListener)activity;
		}
		catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnSeriesSelectedListener");
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v == mBtnMore) {
			mCurrentPage++;
		}
		else if (v == mBtnSearch) {
			mQuery = mEtSearch.getText().toString();
			mCurrentPage = 0;
		}
		search();
	}
	
	private void search()
	{
		new AsyncTask<String, Void, Collection<Series>>() {
			private ProgressDialog mDialog;
			
			@Override
			protected void onPreExecute() {
				mDialog = new ProgressDialog(getActivity());
				mDialog.setCancelable(true);
				mDialog.setMessage(getResources().getString(R.string.common_searcing));
				mDialog.show();
			};
			
			@Override
			protected Collection<Series> doInBackground(String... params) {
				Collection<Series> result = null;
				try 
				{
					result = JsonUtil.deserializeArray(Application.GCD_API_BASEURL + "/search?q=" + URLEncoder.encode(params[0], "utf-8") + "&p=" + mCurrentPage, Series.class);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				return result;
			}
			
			@Override
			protected void onPostExecute(Collection<Series> result) {
				if (getActivity() == null) {
					mDialog.dismiss();
					return;
				}
				
				if (result != null && result.size() > 0)
				{
					mTvNoHits.setVisibility(View.GONE);
					mListFooter.setVisibility(View.VISIBLE);
					if (mCurrentPage == 0) {
						if (result.size() < PAGESIZE) {
							mListFooter.setVisibility(View.GONE);
						}
						mAdapter.clear();
					}
					mAdapter.addMany(result);
					mAdapter.notifyDataSetChanged();
				}
				else {
					if (mCurrentPage == 0) {
						mTvNoHits.setVisibility(View.VISIBLE);
					}
					mListFooter.setVisibility(View.GONE);
				}
				mDialog.dismiss();
			}
		}.execute(mQuery);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
		mSeriesSelectedCallback.onSeriesSelected((int)id);
	}
}
