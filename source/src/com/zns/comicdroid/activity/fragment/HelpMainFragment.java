package com.zns.comicdroid.activity.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zns.comicdroid.BaseFragment;
import com.zns.comicdroid.R;

public class HelpMainFragment extends BaseFragment {
	public static HelpMainFragment newInstance() {
		return new HelpMainFragment();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_help_main, container, false);
		return view;
	}
}
