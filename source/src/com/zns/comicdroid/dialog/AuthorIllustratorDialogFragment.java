package com.zns.comicdroid.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.zns.comicdroid.R;

public class AuthorIllustratorDialogFragment extends DialogFragment {

	public interface OnAuthorIllustratorDialogListener {
		public void onDialogPositiveClick(int comicId, String authors, String illustrators);
	}

	private OnAuthorIllustratorDialogListener mAuthorIllustratorCallback;

	public static AuthorIllustratorDialogFragment newInstance(int comicId, String names)
	{
		AuthorIllustratorDialogFragment dialog = new AuthorIllustratorDialogFragment();
		Bundle args = new Bundle();
		args.putStringArray("Names", names.split(","));
		args.putInt("ComicId", comicId);
		dialog.setArguments(args);
		return dialog;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (getTargetFragment() instanceof OnAuthorIllustratorDialogListener) {
			mAuthorIllustratorCallback = (OnAuthorIllustratorDialogListener)getTargetFragment();
		}
		else if (activity instanceof OnAuthorIllustratorDialogListener) {
			mAuthorIllustratorCallback = (OnAuthorIllustratorDialogListener)activity;
		}
	}	

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		final int comicId = getArguments().getInt("ComicId");
		final String[] names = getArguments().getStringArray("Names");

		LayoutInflater inflater = getActivity().getLayoutInflater();

		final LinearLayout parent = new LinearLayout(getActivity());
		parent.setOrientation(1);
		final View viewHead = inflater.inflate(R.layout.dialog_author_illustrator_head, null);
		parent.addView(viewHead);
		for(String name : names)
		{
			final View view = inflater.inflate(R.layout.dialog_author_illustrator, null);
			((TextView)view.findViewById(R.id.tvName)).setText(name);
			parent.addView(view);
		}
		builder
		.setView(parent);

		builder
		.setPositiveButton(getResources().getString(R.string.common_save), new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String authors = "";
				String illustrators = "";
				for (int i = 1; i < parent.getChildCount(); i++) //Start at 1 to skip header
				{
					View child = parent.getChildAt(i);
					TextView tvName = (TextView)child.findViewById(R.id.tvName);
					RadioButton rbAuthor = (RadioButton)child.findViewById(R.id.rbIsAuthor);
					RadioButton rbIllustrator = (RadioButton)child.findViewById(R.id.rbIsIllustrator);
					if (rbAuthor.isChecked())
						authors += tvName.getText() + ",";
					if (rbIllustrator.isChecked())
						illustrators += tvName.getText() + ",";
				}
				authors = authors.replaceAll("[,]+$", "");
				illustrators = illustrators.replaceAll("[,]+$", "");
				if (mAuthorIllustratorCallback != null)
					mAuthorIllustratorCallback.onDialogPositiveClick(comicId, authors, illustrators);
			}
		})

		.setNegativeButton(getResources().getString(R.string.common_cancel), new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AuthorIllustratorDialogFragment.this.getDialog().cancel();
			}
		});

		AlertDialog dialog = builder.create();		
		return dialog;
	}	
}
