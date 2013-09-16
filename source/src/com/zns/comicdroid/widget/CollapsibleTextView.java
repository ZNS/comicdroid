package com.zns.comicdroid.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
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

	private float mTextHeight;
	private TextView mTv;
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
		a.recycle();		

		this.setOnClickListener(this);			
	}

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
		if (mTextHeight == 0) {
			mTextHeight = pixelsToDp((float)mTv.getHeight());
			if (mTextHeight > 150) {
				mTv.setHeight((int)dpToPixel(150));
        	}        	
		}        
    }
    
	@Override
	public void onClick(View v) {
		if (mIsCollapsed) {
			mTv.setHeight((int)Math.ceil(dpToPixel(mTextHeight + 30)));
			mViewGradient.setVisibility(View.GONE);
		}
		else {
			mTv.setHeight((int)dpToPixel(150));
			mViewGradient.setVisibility(View.VISIBLE);
		}
		mIsCollapsed = !mIsCollapsed;
	}
	
	private float dpToPixel(float dp){
	    DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
	    float px = dp * (metrics.densityDpi / 160f);
	    return px;
	}
	
	private float pixelsToDp(float px){
	    final DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
	    float dp = px / (metrics.densityDpi / 160f);
	    return dp;
	}	
}