package com.tinyrssapp.activities;

import java.util.Date;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.example.TinyRSSApp.R;
import com.tinyrssapp.activities.actionbar.CategoriesActivity;
import com.tinyrssapp.activities.actionbar.FeedsActivity;
import com.tinyrssapp.constants.TinyTinySpecificConstants;
import com.tinyrssapp.errorhandling.ErrorAlertDialog;
import com.tinyrssapp.request.RequestBuilder;
import com.tinyrssapp.request.RequestParamsBuilder;
import com.tinyrssapp.response.ResponseHandler;
import com.tinyrssapp.storage.internal.InternalStorageUtil;
import com.tinyrssapp.storage.prefs.PrefsCredentials;
import com.tinyrssapp.storage.prefs.PrefsSettings;
import com.tinyrssapp.storage.prefs.PrefsUpdater;

public class LoginActivity extends Activity {
	public static final String HOST_PROP = "host";
	public static final String AUTO_CONNECT = "auto-connect";
	public static final int MINUTES_WITHOUT_FEEDS_CLEAN = 120;
	private static final long MILISECS_WITHOUT_FEEDS_CLEAN = MINUTES_WITHOUT_FEEDS_CLEAN * 60 * 1000;

	private EditText address;
	private EditText username;
	private EditText password;
	private boolean autoConnect = true;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		ThemeUpdater.updateTheme(LoginActivity.this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		initialize();
		loadSavedPrefs();
	}

	private void checkForCleaned(String sessionId) {
		Date now = new Date();
		long lastFeedUpdate = PrefsUpdater.getLastCleanedTime(this);
		if (now.getTime() - lastFeedUpdate >= MILISECS_WITHOUT_FEEDS_CLEAN) {
			InternalStorageUtil.clearFiles(this, sessionId);
		}
	}

	private void initialize() {
		Bundle b = getIntent().getExtras();
		if (b != null) {
			autoConnect = b.getBoolean(AUTO_CONNECT);
		}
		address = (EditText) findViewById(R.id.adressText);
		username = (EditText) findViewById(R.id.usernameText);
		password = (EditText) findViewById(R.id.passwordText);
		((Button) findViewById(R.id.connectButton))
				.setOnClickListener(new Button.OnClickListener() {
					@Override
					public void onClick(View v) {
						onConnectButtonClick();
					}
				});
	}

	protected void onConnectButtonClick() {
		final ProgressDialog progressDialog = ProgressDialog.show(
				LoginActivity.this, "Connecting", "Please wait");
		final String host = RequestParamsBuilder.formatHostAddress(address
				.getText().toString());
		boolean saveCredentials = ((CheckBox) findViewById(R.id.saveCredentialsCheck))
				.isChecked();
		PrefsUpdater.invalidateRefreshTimes(this);
		ResponseHandler handler = getLoginResponseHandler(saveCredentials,
				username.getText().toString(), password.getText().toString(),
				host, progressDialog);
		RequestBuilder.makeRequest(LoginActivity.this, host,
				RequestParamsBuilder.paramsLogin(username.getText().toString(),
						password.getText().toString()), handler);

	}

	private void loadSavedPrefs() {
		address.setText(PrefsCredentials.getHostPref(LoginActivity.this));
		username.setText(PrefsCredentials.getUsernamePref(LoginActivity.this));
		password.setText(PrefsCredentials.getPasswordPref(LoginActivity.this));
		if (autoConnect) {
			clickConnectIfPossible();
		}
	}

	private void clickConnectIfPossible() {
		if (address.getText().toString().length() > 0
				&& username.getText().toString().length() > 0
				&& password.getText().toString().length() > 0) {
			((Button) findViewById(R.id.connectButton)).performClick();
		}
	}

	private ResponseHandler getLoginResponseHandler(
			final boolean saveCredentials, final String username,
			final String password, final String host,
			final ProgressDialog progressDialog) {
		return new ResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				try {
					if (response
							.getInt(TinyTinySpecificConstants.RESPONSE_LOGIN_STATUS_PROP) == TinyTinySpecificConstants.RESPONSE_LOGIN_STATUS_FAIL_VALUE) {
						ErrorAlertDialog.showError(LoginActivity.this,
								R.string.error_login);
					} else {
						String sessionId = response
								.getJSONObject(
										TinyTinySpecificConstants.RESPONSE_CONTENT_PROP)
								.getString(
										TinyTinySpecificConstants.RESPONSE_LOGIN_SESSIONID_PROP);
						checkForCleaned(sessionId);
						if (saveCredentials) {
							PrefsCredentials.putUserPassHost(
									LoginActivity.this, username, password,
									host);
						}
						startNextActivity(host, sessionId);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				if (!LoginActivity.this.isFinishing()) {
					progressDialog.dismiss();
					ErrorAlertDialog.showError(LoginActivity.this,
							R.string.error_login);
				}
			}

			@Override
			public void onFinish() {
				if (!LoginActivity.this.isFinishing()) {
					progressDialog.dismiss();
				}
			}
		};
	}

	private void startNextActivity(String finalHost, String sessionId) {
		if (PrefsSettings.getCategoryMode(this) != PrefsSettings.CATEGORY_NO_MODE) {
			startSimpleIntent(new Intent(LoginActivity.this,
					CategoriesActivity.class), finalHost, sessionId);
		} else {
			startSimpleIntent(new Intent(LoginActivity.this,
					FeedsActivity.class), finalHost, sessionId);
		}
	}

	private void startSimpleIntent(Intent intent, String host, String sessionId) {
		Bundle b = new Bundle();
		b.putString(HOST_PROP, host);
		b.putString(TinyTinySpecificConstants.RESPONSE_LOGIN_SESSIONID_PROP,
				sessionId);
		intent.putExtras(b);
		startActivity(intent);
		finish();
	}
}
