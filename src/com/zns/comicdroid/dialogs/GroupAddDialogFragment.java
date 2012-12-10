package com.zns.comicdroid.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.webkit.WebView.FindListener;
import android.widget.EditText;

import com.zns.comicdroid.R;
import com.zns.comicdroid.data.DBHelper;

public class GroupAddDialogFragment extends DialogFragment {
    
	public interface OnGroupAddDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
    }
    
	OnGroupAddDialogListener groupAddCallback;
	
	@Override
	public void onAttach(Activity activity) {
	    super.onAttach(activity);	    
	    if (activity instanceof OnGroupAddDialogListener) {
	    	groupAddCallback = (OnGroupAddDialogListener)activity;
	    }
	}
		
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		builder
		.setView(inflater.inflate(R.layout.dialog_groupadd, null))
		
		.setPositiveButton("Skapa grupp", new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AlertDialog ad = (AlertDialog)dialog;
				EditText etName = (EditText)ad.findViewById(R.id.dialogAddGroup_etName);
				
				DBHelper db = new DBHelper(getActivity());
				db.addGroup(etName.getText().toString());

				if (groupAddCallback != null)
					groupAddCallback.onDialogPositiveClick(GroupAddDialogFragment.this);
			}
		})
		
		.setNegativeButton("Ångra", new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				GroupAddDialogFragment.this.getDialog().cancel();
			}
		});
				
		return builder.create();
	}
}
