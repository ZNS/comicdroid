package com.zns.comicdroid.activity.fragment;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.books.Books;
import com.google.api.services.books.BooksRequestInitializer;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volumes;
import com.zns.comicdroid.BaseFragment;
import com.zns.comicdroid.R;
import com.zns.comicdroid.adapter.GCDIssueAdapter;
import com.zns.comicdroid.data.Comic;
import com.zns.comicdroid.data.Group;
import com.zns.comicdroid.dialog.GroupDialogFragment;
import com.zns.comicdroid.gcd.Client;
import com.zns.comicdroid.gcd.Issue;
import com.zns.comicdroid.util.StringUtil;

public class AddMultipleIssuesFragment extends BaseFragment implements OnItemClickListener, OnClickListener, GroupDialogFragment.OnGroupAddDialogListener {
	
	public static final String ARGUMENT_SERIES_ID = "COMICDROID_SERIES_ID";
	private final static String STATE_CURRENT_SERIES_ID = "CURRENTSERIESID";
	private final static int PAGESIZE = 15;
	private ListView mLvIssues;
	private GCDIssueAdapter mAdapter;
	private LinearLayout mListFooter;
	private Button mBtnMore;
	private ActionMode mMode;
	private final int mSdk = android.os.Build.VERSION.SDK_INT;
	private int mPaddingInPx = 0;
	private Spinner mSpGroup;
	private ArrayAdapter<Group> mAdapterGroups;
	private int mCurrentPage;
	private int mSeriesId;
	private Client mClientGCD;
	
	public static AddMultipleIssuesFragment newInstance() {
		return new AddMultipleIssuesFragment();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_add_multiple_issues, container, false);
	
		mListFooter = (LinearLayout)getSherlockActivity().getLayoutInflater().inflate(R.layout.fragment_add_multiple_search_footer, null);	
		mBtnMore = (Button)mListFooter.findViewById(R.id.add_multiple_btnMore);
		mBtnMore.setOnClickListener(this);
		
		mLvIssues = (ListView)view.findViewById(R.id.add_multiple_lvIssues);		
		mLvIssues.addFooterView(mListFooter);
		mLvIssues.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		mLvIssues.setOnItemClickListener(this);
			
		//Spinner groups		
		mSpGroup = (Spinner)view.findViewById(R.id.add_multiple_spGroup);
		List<Group> groups = getDBHelper().getGroups();
		if (groups == null)
			groups = new ArrayList<Group>();
		groups.add(0, new Group(0, getResources().getString(R.string.common_nogroup), null, 0, 0, 0, 0, 0));
		mAdapterGroups = new ArrayAdapter<Group>(getActivity(), android.R.layout.simple_spinner_item, groups);
		mAdapterGroups.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpGroup.setAdapter(mAdapterGroups);
		
		//Add group
		ImageView ivGroupAdd = (ImageView)view.findViewById(R.id.add_multiple_ivGroupAdd);
		ivGroupAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment dialogAddGroup = new GroupDialogFragment();
				dialogAddGroup.show(getSherlockActivity().getSupportFragmentManager(), "GROUPADD");
			}
		});		
		
		final float scale = getResources().getDisplayMetrics().density;
		mPaddingInPx = (int) (5 * scale + 0.5f);
		
		if (mClientGCD == null) {
			try {
				mClientGCD = new Client(getActivity());
			}
			catch (Exception e) {
				Toast.makeText(getActivity(), getString(R.string.gcd_error_init), Toast.LENGTH_LONG).show();
			}
		}
		
		if (savedInstanceState != null) {
			mSeriesId = savedInstanceState.getInt(STATE_CURRENT_SERIES_ID);
		}
		else {
			mSeriesId = getArguments().getInt(ARGUMENT_SERIES_ID);
		}
		mCurrentPage = 0;
		loadIssues();
		
		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_CURRENT_SERIES_ID, mSeriesId);
		super.onSaveInstanceState(outState);
	}
	
	private void loadIssues()
	{
		new AsyncTask<Integer, Void, List<Issue>>() {
			@Override
			protected List<Issue> doInBackground(Integer... params) {
				List<Issue> result = mClientGCD.getIssuesBySeries(params[0], mCurrentPage);
				return result;
			}
			
			@Override
			protected void onPostExecute(List<Issue> result) {
				if (result != null)
				{
					if (getActivity() != null)
					{
						if (result != null && result.size() > 0)
						{
							if (mCurrentPage == 0) {
								mAdapter = new GCDIssueAdapter(getActivity(), new ArrayList<Issue>(result));
								mLvIssues.setAdapter(mAdapter);
								if (result.size() <= PAGESIZE) {
									mListFooter.setVisibility(View.GONE);
								}
								else {
									mListFooter.setVisibility(View.VISIBLE);
								}
							}
							else {
								mAdapter.addMany(result);
								mAdapter.notifyDataSetChanged(false);
							}
						}
						else {
							mListFooter.setVisibility(View.GONE);
						}
					}
				}				
			}
		}.execute(mSeriesId);		
	}
	
	@Override
	public void onClick(View view) {
		//View more button clicked
		if (view == mBtnMore) {
			mCurrentPage++;
			loadIssues();
		}
	};	
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (parent == mLvIssues)
		{
			boolean viewChecked = ((Checkable)view).isChecked();
			//Ugliest hack in this app. Some api versions (I don't know which, but at least 8 and 9) fires the checkable views setChecked AFTER this event. For those we negate viewChecked
			if (mSdk < 11) {
				viewChecked = !viewChecked;
			}
			//Notify the adapter of which positions are checked, since it reuses row views
			mAdapter.setChecked(position, viewChecked);
			
			if (viewChecked) {
				if(mSdk < 16) {
					view.setBackgroundDrawable(getResources().getDrawable(R.drawable.listitem_selected));
				}
				else {
					view.setBackground(getResources().getDrawable(R.drawable.listitem_selected));
				}
			}
			else {
				view.setBackgroundColor(getResources().getColor(R.color.contentBg));
			}
			//Padding is reset when changing background
			view.setPadding(mPaddingInPx, mPaddingInPx, mPaddingInPx, mPaddingInPx);

			SparseBooleanArray checked = mLvIssues.getCheckedItemPositions();
			boolean hasCheckedElement = false;
			for (int i = 0 ; i < checked.size() && ! hasCheckedElement ; i++) {
				hasCheckedElement = checked.valueAt(i);
			}

			if (hasCheckedElement) {
				if (mMode == null && getSherlockActivity() != null) {
					mMode = getSherlockActivity().startActionMode(new ModeCallback());
				}
			} else {
				if (mMode != null) {
					mMode.finish();
				}
			}
		}
	}
	
	private final class ModeCallback implements ActionMode.Callback {    	 
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Create the menu from the xml file
			MenuInflater inflater = getSherlockActivity().getSupportMenuInflater();
			inflater.inflate(R.menu.actionbar_context_add, menu);
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
			for (int i = 0; i < mLvIssues.getAdapter().getCount(); i++) {
				mLvIssues.setItemChecked(i, false);
				mAdapter.notifyDataSetChanged(true);
			}
			if (mode == mMode) {
				mMode = null;
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			
			//Get selected issues
			List<Issue> checkedIssues = new ArrayList<Issue>();
			SparseBooleanArray checked = mLvIssues.getCheckedItemPositions();
			for (int i = 0 ; i < checked.size(); i++) {
				if (checked.valueAt(i))
				{
					checkedIssues.add(mAdapter.getIssue(i));
				}
			}
			
			//Archive issues as Comics
			new AsyncTask<List<Issue>, Void, Void>() {
				private ProgressDialog mDialog;
				
				@Override
				protected void onPreExecute() {
					mDialog = new ProgressDialog(getActivity());
					mDialog.setCancelable(false);
					mDialog.setMessage(getResources().getString(R.string.gcd_adding_comics));
					mDialog.show();
				};
				
				@Override
				protected Void doInBackground(List<Issue>... params) {
					NetHttpTransport httpTransport = new NetHttpTransport();
					JsonFactory jsonFactory = new JacksonFactory();					
					Books books = new Books.Builder(httpTransport, jsonFactory, null)
					.setApplicationName("ComicDroid/1.0")
					.setGoogleClientRequestInitializer(new BooksRequestInitializer())
					.build();
					
					for (Issue issue : params[0]) {
						Comic comic = Comic.fromGCDIssue(issue, getImagePath(false), null);						
						if (!comic.isComplete() && !StringUtil.nullOrEmpty(issue.isbn)) {
							//Extend from google books
							try
							{	
								Volumes list = books.volumes().list("isbn:" + issue.isbn).execute();
	
								if (list != null && list.getTotalItems() > 0)
								{
									Volume item = list.getItems().get(0);
									Volume.VolumeInfo info = item.getVolumeInfo();
									if (info != null)
									{
										comic.extendFromGoogleBooks(info, getImagePath(false));
									}
								}
							}
							catch (IOException e) {
								e.printStackTrace();
							} 
							catch (ParseException e) {
								e.printStackTrace();
							}
						}
						//Add group
						if (mSpGroup.getSelectedItemPosition() > 0) {
							Group g = (Group)mSpGroup.getSelectedItem();
							comic.setGroupId(g.getId());
						}			
						//Store comic
						getDBHelper().storeComic(comic);
					}
					return null;
				}
				
				@Override
				protected void onPostExecute(Void result) {
					mDialog.dismiss();
					getActivity().getSupportFragmentManager().popBackStackImmediate();
				}
			}.execute(checkedIssues);

			mode.finish();
			return true;
		}
	}

	@Override
	public void onGroupDialogPositiveClick(String groupAdded) {
		List<Group> groups = getDBHelper().getGroups();
		groups.add(0, new Group(0, getResources().getString(R.string.common_nogroup), null, 0, 0, 0, 0, 0));
		mAdapterGroups.clear();
		int i = 0;
		int index = 0;
		for (Group g : groups) {
			mAdapterGroups.add(g);
			if (g.getName().equals(groupAdded)) {
				index = i;
			}
			i++;
		}
		mSpGroup.setSelection(index);		
	}
}
