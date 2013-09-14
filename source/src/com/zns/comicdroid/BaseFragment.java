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
