package com.zns.comicdroid.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.zns.comicdroid.BaseFragmentActivity;
import com.zns.comicdroid.R;

public class GroupDialogFragment extends DialogFragment {
    
	public interface OnGroupAddDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
    }
    
	OnGroupAddDialogListener groupAddCallback;
	
	public static GroupDialogFragment newInstance(int groupId, String name, int totalCount)
	{
		GroupDialogFragment dialog = new GroupDialogFragment();
		Bundle args = new Bundle();
		args.putInt("GroupId", groupId);
		args.putString("Name", name);
		args.putInt("TotalCount", totalCount);
		dialog.setArguments(args);
		return dialog;
	}
	
	@Override
	public void onAttach(Activity activity) {
	    super.onAttach(activity);	    
	    if (activity instanceof OnGroupAddDialogListener) {
	    	groupAddCallback = (OnGroupAddDialogListener)activity;
	    }
	}
		
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {				
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog_groupadd, null);
		final EditText etName = (EditText)view.findViewById(R.id.dialogAddGroup_etName);
		final EditText etTotalCount = (EditText)view.findViewById(R.id.dialogAddGroup_etTotalCount);
		
		final Bundle bundle = getArguments();	
		if (bundle != null)
		{
			etName.setText(bundle.getString("Name"));
			if (bundle.getInt("TotalCount") > 0)
				etTotalCount.setText(Integer.toString(bundle.getInt("TotalCount")));
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());	
		builder
		.setView(view)			
		.setPositiveButton(getResources().getString(R.string.dialog_group_create), new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {				
				BaseFragmentActivity activity = (BaseFragmentActivity)getActivity();
				
				if (bundle != null)
				{
					//Update group
					ContentValues values = new ContentValues();
					values.put("Name", etName.getText().toString());
					if (etTotalCount.length() > 0)
						values.put("TotalBookCount", Integer.parseInt(etTotalCount.getText().toString()));
					activity.getDBHelper().update("tblGroups", values, "_id=?", new String[] { Integer.toString(bundle.getInt("GroupId")) });
				}
				else
				{
					//Insert group
					activity.getDBHelper().addGroup(etName.getText().toString());
				}
				
				if (groupAddCallback != null)
					groupAddCallback.onDialogPositiveClick(GroupDialogFragment.this);
			}
		})	
		.setNegativeButton(getResources().getString(R.string.common_cancel), new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				GroupDialogFragment.this.getDialog().cancel();
			}
		});
				
		return builder.create();
	}
}
