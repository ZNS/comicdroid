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
import android.widget.EditText;

import com.zns.comicdroid.BaseFragmentActivity;
import com.zns.comicdroid.R;

public class GroupAddDialogFragment extends DialogFragment {

	public interface OnGroupAddDialogListener {
		public void onDialogPositiveClick(DialogFragment dialog);
	}

	private OnGroupAddDialogListener mGoupAddCallback;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);	    
		if (activity instanceof OnGroupAddDialogListener) {
			mGoupAddCallback = (OnGroupAddDialogListener)activity;
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();

		builder
		.setView(inflater.inflate(R.layout.dialog_groupadd, null))

		.setPositiveButton(getResources().getString(R.string.dialog_group_create), new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AlertDialog ad = (AlertDialog)dialog;
				EditText etName = (EditText)ad.findViewById(R.id.dialogAddGroup_etName);

				BaseFragmentActivity activity = (BaseFragmentActivity)getActivity();
				activity.getDBHelper().addGroup(etName.getText().toString());

				if (mGoupAddCallback != null)
					mGoupAddCallback.onDialogPositiveClick(GroupAddDialogFragment.this);
			}
		})

		.setNegativeButton(getResources().getString(R.string.common_cancel), new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				GroupAddDialogFragment.this.getDialog().cancel();
			}
		});

		return builder.create();
	}
}
