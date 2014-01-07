package com.zns.comicdroid.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.zns.comicdroid.R;
import com.zns.comicdroid.gcd.Client;
import com.zns.comicdroid.gcd.Issue;
import com.zns.comicdroid.util.StringUtil;
import com.zns.comicdroid.widget.CheckableLinearLayout;

public class GCDIssueAdapter extends ArrayAdapter<Issue> {
	private final List<Issue> mValues;
	private final ArrayList<Boolean> mChecked = new ArrayList<Boolean>();
	private final int mSdk = android.os.Build.VERSION.SDK_INT;
	private final Drawable mListItemSelected;
	private final int mListItemBgColor;
	private int mPaddingInPx = 0;	
	
	static class IssueHolder
	{
		TextView tvTitle;
		TextView tvAuthor;
		ImageView ivImage;
		TextView tvIssue;
	}

	public GCDIssueAdapter(Context context, List<Issue> values) 
	{
		super(context, R.layout.list_gcdissuerow, values);
		this.mValues = values;
		mListItemSelected = context.getResources().getDrawable(R.drawable.listitem_selected);
		mListItemBgColor = context.getResources().getColor(R.color.contentBg);
		final float scale = context.getResources().getDisplayMetrics().density;
		mPaddingInPx = (int) (5 * scale + 0.5f);		
        setChecked(false);		
	}

	public Issue getIssue(int position)
	{
		if (position < mValues.size())
			return mValues.get(position);
		return null;
	}

	public void addMany(Collection<Issue> issues) {
		for (Issue i : issues) {
			mValues.add(i);
		}
	}
	
	@Override
	public long getItemId(int position) {
		return mValues.get(position).id;
	}
	
    // AS EVERY TIME LISTVIEW INFLATE YOUR VIEWS WHEN YOU MOVE THEM SO YOU NEED TO SAVE ALL OF YOUR CHECKBOX STATES IN SOME ARRAYLIST OTHERWISE IT WILL SET ANY DEFAULT VALUE.
    private void setChecked(boolean isChecked)
    {
    	mChecked.clear();
        for (int i=0; i < mValues.size(); i++) {
            mChecked.add(i, isChecked);
        }
    }
    
    public void setChecked(int position, boolean isChecked) {
    	mChecked.set(position, isChecked);
    }
    
    public void notifyDataSetChanged(boolean clearChecked) {
    	notifyDataSetChanged();
    	if (clearChecked) {
    		setChecked(false);
    	}
    }
    
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public View getView(final int position, View convertView, ViewGroup parent) 
	{		  
		View row = convertView;
		IssueHolder holder = null;

		if (row == null)
		{
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
			row = inflater.inflate(R.layout.list_gcdissuerow, parent, false);

			holder = new IssueHolder();
			holder.tvTitle = (TextView)row.findViewById(R.id.gcdissue_tvTitle);
			holder.tvAuthor = (TextView)row.findViewById(R.id.gcdissue_tvAuthor);
			holder.ivImage = (ImageView)row.findViewById(R.id.gcdissue_ivImage);
			holder.tvIssue = (TextView)row.findViewById(R.id.gcdissue_tvIssue);

			row.setTag(holder);
		}
		else
		{
			holder = (IssueHolder)row.getTag();
		}

		Issue issue = mValues.get(position);
		holder.tvTitle.setText(issue.title);
		holder.tvAuthor.setText(issue.script);
		if (!StringUtil.nullOrEmpty(issue.volume)) {
			holder.tvIssue.setText(issue.volume.replace("\\D", ""));
		}
		else if (!StringUtil.nullOrEmpty(issue.number)) {
			holder.tvIssue.setText(issue.number.replace("\\D", ""));
		}
		
		if (issue.images != null && issue.images.length > 0 && !StringUtil.nullOrEmpty(issue.images[0]))
		{
			String imgUrl = issue.images[0];
			if (imgUrl.charAt(0) == '/') {
				imgUrl = Client.IMAGEURL + imgUrl;
			}
			ImageLoader.getInstance().displayImage(imgUrl, holder.ivImage);			  
			holder.ivImage.setVisibility(View.VISIBLE);
		}
		else
		{
			holder.ivImage.setVisibility(View.GONE);
		}
		
        if (position < mChecked.size()) {
        	((CheckableLinearLayout)row).setChecked(mChecked.get(position));
        	
    		if (mChecked.get(position)) {
    			if(mSdk < 16) {
    				row.setBackgroundDrawable(mListItemSelected);
    			}
    			else {
    				row.setBackground(mListItemSelected);
    			}
    		}
    		else {
    			row.setBackgroundColor(mListItemBgColor);
    		}
    		row.setPadding(mPaddingInPx, mPaddingInPx, mPaddingInPx, mPaddingInPx);        	
        }
		
		return row;
	}
}
