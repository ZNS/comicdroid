package com.zns.comicdroid;

import com.actionbarsherlock.app.SherlockFragment;
import com.zns.comicdroid.data.DBHelper;

public class BaseFragment extends SherlockFragment {

	protected DBHelper getDBHelper() {
		return ((BaseFragmentActivity)getActivity()).getDBHelper();
	}
	
}