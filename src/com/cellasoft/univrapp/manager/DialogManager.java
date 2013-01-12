package com.cellasoft.univrapp.manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.WindowManager;

public class DialogManager {
	private static DialogManager dm;
	public static final int ERROR_DIALOG_ID = 1;
	public static final int PROGRESS_DIALOG_ID = 2;
	public static final int SUBSCRIBE_DIALOG_ID = 3;

	private AlertDialog allertDialog;
	private ProgressDialog progressDialog;
	private AlertDialog.Builder subscribeDialog;

	public Context mContext;

	private DialogManager(Context context) {
		mContext = context;
		initProgressDialog();
		initAllertDialog();
	}

	public static DialogManager getInstance(Context context) {
		if (dm == null) {
			dm = new DialogManager(context);
		} else
			dm.mContext = context;
		return dm;
	}

	private void initAllertDialog() {
		progressDialog = new ProgressDialog(mContext);
		progressDialog.setTitle("Please wait");
		progressDialog.setMessage("Loading Feed ...");
		progressDialog
				.setOnCancelListener(new DialogInterface.OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						dialog.dismiss();
						((Activity) mContext).finish();
					}
				});

	}

	private void initProgressDialog() {
		allertDialog = new AlertDialog.Builder(mContext)
				.setTitle("UnivrRSSFeed")
				.setIcon(android.R.drawable.ic_dialog_alert).create();
		allertDialog.setButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		WindowManager.LayoutParams lp = allertDialog.getWindow()
				.getAttributes();
		lp.dimAmount = 0.5f;

		allertDialog.getWindow().setAttributes(lp);
		allertDialog.getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
	}

	public Dialog getProgressDialog() {
		return progressDialog;
	}

	public Dialog getAllertDialog() {
		return allertDialog;
	}

	public Dialog getSubscribeDialog() {
		return subscribeDialog.create();
	}

	public void setErrorMessage(String msgError) {
		allertDialog.setMessage(msgError);
	}
}
