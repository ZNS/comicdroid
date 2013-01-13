package com.zns.comicdroid.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.zns.comicdroid.R;

public class RenameDialogFragment extends DialogFragment {
	
	public interface OnRenameDialogListener {
        public void onDialogPositiveClick(String oldName, String newName);
    }
	
	OnRenameDialogListener renameCallback;
	
	private String name;
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public void onAttach(Activity activity) {
	    super.onAttach(activity);	    
	    if (getTargetFragment() instanceof OnRenameDialogListener) {
	    	renameCallback = (OnRenameDialogListener)getTargetFragment();
	    }
	}	
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View view = inflater.inflate(R.layout.dialog_rename, null);
		((EditText)view.findViewById(R.id.dialogRename_etName)).setText(name);
		
		builder
		.setView(view);
		
		builder
		.setPositiveButton(getResources().getString(R.string.common_save), new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AlertDialog ad = (AlertDialog)dialog;
				EditText etName = (EditText)ad.findViewById(R.id.dialogRename_etName);

				if (renameCallback != null)
					renameCallback.onDialogPositiveClick(name, etName.getText().toString());
			}
		})
		
		.setNegativeButton(getResources().getString(R.string.common_cancel), new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				RenameDialogFragment.this.getDialog().cancel();
			}
		});
					
		AlertDialog dialog = builder.create();		
		return dialog;
	}	
}
