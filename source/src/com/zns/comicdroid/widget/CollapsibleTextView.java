package com.zns.comicdroid.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.Html;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.zns.comicdroid.R;

public class CollapsibleTextView extends FrameLayout implements OnClickListener {

	private TextView mTv;
	private TextView mTvHandle;
	private View mViewGradient;
	private boolean mIsCollapsed = true;
	
	public CollapsibleTextView(Context context) {
		super(context);
	}
	
	public CollapsibleTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		final LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.textview_collapsible, this);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CollapsibleTextView, 0, 0);
		
		mViewGradient = findViewById(R.id.tvCollapsibleGradient);
		mTv = (TextView)findViewById(R.id.tvCollapsible);
		mTv.setText(Html.fromHtml(a.getString(R.styleable.CollapsibleTextView_text)));
		mTvHandle = (TextView)findViewById(R.id.tvCollapsibleHandle);
		a.recycle();		

		this.setOnClickListener(this);			
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		//Hack, height will always be set to 150dp no matter if it's needed or not
		mTv.getLayoutParams().height = (int)dpToPixel(150);
	}
	
	@Override
	public void onClick(View v) {
		if (mIsCollapsed) {
			mTv.getLayoutParams().height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
			mTvHandle.setText(R.string.common_hide);
			mViewGradient.setVisibility(View.GONE);
		}
		else {
			mTv.getLayoutParams().height = (int)dpToPixel(150);
			mTvHandle.setText(R.string.common_readmore);
			mViewGradient.setVisibility(View.VISIBLE);
		}
		mIsCollapsed = !mIsCollapsed;
	}
	
	private float dpToPixel(float dp){
	    DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
	    float px = dp * (metrics.densityDpi / 160f);
	    return px;
	}	
}