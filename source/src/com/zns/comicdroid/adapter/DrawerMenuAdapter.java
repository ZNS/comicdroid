package com.zns.comicdroid.adapter;

import com.zns.comicdroid.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DrawerMenuAdapter extends BaseAdapter {

	private final String[] mTitle;
	private final String[] mSubTitle;
	private LayoutInflater mInflater;
	private Context mContext;

	public DrawerMenuAdapter(Context context, String[] title, String[] subtitle) {
		this.mContext = context;
		this.mTitle = title;
		this.mSubTitle = subtitle;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mTitle.length;
	}

	@Override
	public Object getItem(int pos) {
		// TODO Auto-generated method stub
		return mTitle[pos];
	}

	@Override
	public long getItemId(int pos) {
		// TODO Auto-generated method stub
		return pos;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView txtTitle;
		TextView txtSubTitle;		
		mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);    		
		View itemView = mInflater.inflate(R.layout.navigation_drawer_menu_item, parent, false);
		txtTitle = (TextView) itemView.findViewById(R.id.title);
		txtSubTitle = (TextView) itemView.findViewById(R.id.subtitle);
		txtTitle.setText(mTitle[position]);
		txtSubTitle.setText(mSubTitle[position]);
		return itemView;
	}
}
