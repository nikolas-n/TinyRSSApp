package com.tinyrssreader.errorhandling;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.tinyrssreader.R;
import com.tinyrssreader.handlers.AbstractHandler;

public class ErrorAlertDialog {

	public static void showError(Context context, int msg, int title,
			int positiveButtonMsg) {
		if (((Activity) context).isFinishing()) {
			return;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(msg).setTitle(title);

		builder.setPositiveButton(positiveButtonMsg,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	public static void showError(Context context, int msg, int title,
			int positiveButtonMsg, int negativeButtonMsg,
			final AbstractHandler handler) {
		if (((Activity) context).isFinishing()) {
			return;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(msg).setTitle(title);

		builder.setPositiveButton(positiveButtonMsg,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						handler.handle(null);
					}
				});
		builder.setNegativeButton(negativeButtonMsg,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	public static void showError(Context context, int msg) {
		showError(context, msg, R.string.error_title, R.string.error_button);
	}

	public static void showErrorTwoButtonsF(Context context, int msg,
			int positiveButton, int negativeButton, AbstractHandler handler) {
		showError(context, msg, R.string.error_title, positiveButton,
				negativeButton, handler);
	}

	public static void showError(Context context, String msg, int title,
			int positiveButtonMsg) {
		if (((Activity) context).isFinishing()) {
			return;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(msg).setTitle(title);

		builder.setPositiveButton(positiveButtonMsg,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	public static void showError(Context context, String msg) {
		showError(context, msg, R.string.error_title, R.string.error_button);
	}
}
