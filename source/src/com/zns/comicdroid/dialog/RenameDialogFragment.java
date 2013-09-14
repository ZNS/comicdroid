/*******************************************************************************
 * Copyright (c) 2013 Ulrik Andersson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Ulrik Andersson - initial API and implementation
 ******************************************************************************/
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

	private OnRenameDialogListener mRenameCallback;
	private String mName;
	
	public void setName(String name) {
		this.mName = name;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (getTargetFragment() instanceof OnRenameDialogListener) {
			mRenameCallback = (OnRenameDialogListener)getTargetFragment();
		}
		else if (activity instanceof OnRenameDialogListener) {
			mRenameCallback = (OnRenameDialogListener)activity;
		}
	}	

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View view = inflater.inflate(R.layout.dialog_rename, null);
		((EditText)view.findViewById(R.id.dialogRename_etName)).setText(mName);

		builder
		.setView(view);

		builder
		.setPositiveButton(getResources().getString(R.string.common_save), new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AlertDialog ad = (AlertDialog)dialog;
				EditText etName = (EditText)ad.findViewById(R.id.dialogRename_etName);

				if (mRenameCallback != null)
					mRenameCallback.onDialogPositiveClick(mName, etName.getText().toString());
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
