package com.zns.comicdroid.activity.fragment;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zns.comicdroid.BaseFragment;
import com.zns.comicdroid.R;

public class HelpAboutFragment extends BaseFragment {
	public static HelpAboutFragment newInstance() {
		return new HelpAboutFragment();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_help_about, container, false);		
		TextView tvSource = (TextView)view.findViewById(R.id.tvSource);
		tvSource.setText(Html.fromHtml("<a href=\"https://github.com/ZNS/comicdroid\">" + getResources().getString(R.string.help_getsource) + "</a>"));
		tvSource.setMovementMethod(LinkMovementMethod.getInstance());
		return view;
	}
}