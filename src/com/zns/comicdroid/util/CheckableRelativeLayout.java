package com.zns.comicdroid.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.RelativeLayout;

public class CheckableRelativeLayout extends RelativeLayout
	implements Checkable {

	private boolean mChecked;
	
	public CheckableRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mChecked = false;
	}

	@Override
	public boolean isChecked() {
		return mChecked;
	}

	@Override
	public void setChecked(boolean checked) {
		mChecked = checked;
	}

	@Override
	public void toggle() {
		mChecked = !mChecked;
	}
}