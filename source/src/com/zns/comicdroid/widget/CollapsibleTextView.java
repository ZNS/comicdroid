package com.zns.comicdroid.widget;

import com.zns.comicdroid.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

public class CollapsibleTextView extends FrameLayout {

	public CollapsibleTextView(Context context) {
		super(context);
	}
	
	public CollapsibleTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		final LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.textview_collapsible, this);
	}
}
