package com.zns.comicdroid;

import com.actionbarsherlock.app.SherlockFragment;
import com.zns.comicdroid.data.DBHelper;

public class BaseFragment extends SherlockFragment {

	protected DBHelper getDBHelper() {
		return ((BaseFragmentActivity)getActivity()).getDBHelper();
	}

	public String getImagePath(boolean appendSlash) {
		return ((Application)getActivity().getApplication()).getImagePath(appendSlash);
	}

	public String getImagePath(String imageName) {
		return ((Application)getActivity().getApplication()).getImagePath(imageName);
	}	
}