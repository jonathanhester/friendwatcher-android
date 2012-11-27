/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.jonathanhester.friendwatcher;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.facebook.android.Facebook;
import com.google.android.apps.analytics.easytracking.TrackedActivity;
import com.jonathanhester.c2dm.C2DMessaging;
import com.jonathanhester.friendwatcher.requests.FriendWatcherRequest;
import com.jonathanhester.friendwatcher.requests.MyRequestFactory;
import com.jonathanhester.requestFactory.Receiver;
import com.jonathanhester.requestFactory.ServerFailure;

/**
 * Main activity - requests "Hello, World" messages from the server and provides
 * a menu item to invoke the accounts activity.
 */
public class FriendWatcherActivity extends TrackedActivity {
	/**
	 * Tag for logging.
	 */
	private static final String TAG = "FriendWatcherActivity";

	/**
	 * The current context.
	 */
	private Context mContext = this;

	/**
	 * A {@link BroadcastReceiver} to receive the response from a register or
	 * unregister request, and to update the UI.
	 */
	private final BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int status = intent.getIntExtra(DeviceRegistrar.STATUS_EXTRA,
					DeviceRegistrar.ERROR_STATUS);
			String message = null;
			if (status == DeviceRegistrar.REGISTERED_STATUS) {
				message = getResources().getString(
						R.string.registration_succeeded);
			} else if (status == DeviceRegistrar.UNREGISTERED_STATUS) {
				message = getResources().getString(
						R.string.unregistration_succeeded);
			} else {
				c2dmError();
				message = getResources().getString(R.string.registration_error);
			}

			// Display a notification
			SharedPreferences prefs = Util.getSharedPreferences(mContext);
			stopLoading();
			if (status == DeviceRegistrar.REGISTERED_STATUS) 
				showUnfriended();
		}
	};

	private void c2dmError() {
		Tracker.getInstance().requestFail(Tracker.TYPE_C2DM);
		Button c2dmReg = (Button) findViewById(R.id.c2dm_reg);
		c2dmReg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				registerC2DM();
			}
		});
		c2dmReg.setVisibility(View.VISIBLE);
	}

	/**
	 * Begins the activity.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.friend_watcher);
		Button c2dmReg = (Button) findViewById(R.id.c2dm_reg);
		c2dmReg.setVisibility(View.GONE);
		Log.i(TAG, "onCreate");

		registerReceiver(mUpdateUIReceiver, new IntentFilter(
				Util.UPDATE_UI_INTENT));
		
	}

	@Override
	protected void onStart() {
		super.onStart();
		reloadState();
	}
	
	private void reloadState() {
		if (!authedFb()) {
			doFbAuth();
		} else if (Util.getSharedPreferences(mContext).getString(Util.USER_ID, null) == null) {
			createUser();
		} else if (!c2dmRegistered()) {
			// Register a receiver to provide register/unregister notifications
			registerC2DM();
		} else {
			verifyServerToken();
			showUnfriended();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	ProgressDialog progressDialog;

	public void startLoading(String message) {
		Tracker.getInstance().startTimeEvent(Tracker.TYPE_TIME_LOADING);
		progressDialog = ProgressDialog.show(this, null, message);
		Log.d("dialog", "Showing dialog: " + progressDialog);
	}

	public void stopLoading() {
		Tracker.getInstance().stopTimeEvent(Tracker.TYPE_TIME_LOADING);
		progressDialog.dismiss();
		Log.d("dialog", "Stopping dialog: " + progressDialog);
	}
	
	public void createUser() {
		startLoading("Validating Facebook user...");
		final FriendWatcherRequest request = MyRequestFactory.friendWatcherRequest(mContext);
		request.validateUser(fbId(), token()).fire(new Receiver<String>() {
			@Override
			public void onFailure(ServerFailure failure) {
				Tracker.getInstance().requestFail(Tracker.TYPE_REQUEST_VALIDATE_USER, 0);
				stopLoading();
				saveFbCreds(null, null);
				doFbAuth();
			}

			@Override
			public void onSuccess(String response) {
				Tracker.getInstance().requestSuccess(Tracker.TYPE_REQUEST_VALIDATE_USER, response.equals("1"));
				stopLoading();

				if (!response.equals("1")) {
					Toast.makeText(mContext, "Unable to verify user. Let's try again", Toast.LENGTH_SHORT).show();
					saveFbCreds(null, null);
					doFbAuth();
				} else {
					SharedPreferences settings = Util.getSharedPreferences(mContext);
			        SharedPreferences.Editor editor = settings.edit();
			        editor.putString(Util.USER_ID, response);
			        editor.commit();
					Toast.makeText(mContext, "Success!", Toast.LENGTH_SHORT).show();
					reloadState();
				}
			}
		});

	}
	
	private void doFbAuth() {
		//first clear registration id so we'll reregister with new fb creds
		SharedPreferences settings = Util.getSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(Util.DEVICE_REGISTRATION_ID);
        editor.commit();
		Intent authFbIntent = new Intent(this, FbAuthActivity.class);
		startActivityForResult(authFbIntent, 1);
	}
	
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
	  super.onActivityResult(requestCode, resultCode, data);
	  String fbId = data.getStringExtra(Util.FBID);
	  String token = data.getStringExtra(Util.TOKEN);
	  saveFbCreds(token, fbId);
	  reloadState();
	}
	
	private boolean authedFb() {
		String token = token();
		Tracker.getInstance().authed(token);
		if (token == null)
			return false;
		return true;
	}
	
	private String token() {
		return Util.getSharedPreferences(mContext).getString(Util.TOKEN, null);
	}
	
	private String fbId() {
		return Util.getSharedPreferences(mContext).getString(Util.FBID, null);
	}
	
	private void verifyServerToken() {
		String accessToken = Util.getSharedPreferences(mContext).getString(
				Util.TOKEN, null);
		String fbId = Util.getSharedPreferences(mContext).getString(
				Util.FBID, null);
		final FriendWatcherRequest request = MyRequestFactory.friendWatcherRequest(mContext);
		Tracker.getInstance().requestStart(Tracker.TYPE_REQUEST_VERIFY);
		request.verifyToken(fbId, accessToken).fire(new Receiver<String>() {
			@Override
			public void onFailure(ServerFailure failure) {
				Tracker.getInstance().requestFail(Tracker.TYPE_REQUEST_VERIFY, 0);
			}

			@Override
			public void onSuccess(String response) {
				Tracker.getInstance().requestSuccess(Tracker.TYPE_REQUEST_VERIFY, response.equals("1"));
				if (!response.equals("1")) {
					saveFbCreds(null, null);
					doFbAuth();
				}
			}
		});
	}
	
	private void saveFbCreds(String token, String fbId) {
		final SharedPreferences prefs = Util.getSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(Util.TOKEN, token);
		editor.putString(Util.FBID, fbId);
		editor.commit();
	}

	private boolean c2dmRegistered() {
		String deviceRegistrationId = Util.getSharedPreferences(mContext)
				.getString(Util.DEVICE_REGISTRATION_ID, null);
		Tracker.getInstance().requestSuccess(Tracker.TYPE_C2DM, (deviceRegistrationId != null));
		if (deviceRegistrationId == null)
			return false;
		return true;
	}

	/**
	 * Shuts down the activity.
	 */
	@Override
	public void onDestroy() {
		unregisterReceiver(mUpdateUIReceiver);
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		// inflater.inflate(R.menu.main_menu, menu);
		// Invoke the Register activity
		// menu.getItem(0).setIntent(new Intent(this, AccountsActivity.class));
		return true;
	}

	private void registerC2DM() {
		Tracker.getInstance().requestStart(Tracker.TYPE_C2DM);
		C2DMessaging.register(mContext, Setup.SENDER_ID);
	}

	Facebook facebook = new Facebook(Util.getFacebookId());

	private void showUnfriended() {
		Tracker.getInstance().loadIframe();
		String url = Util.getIframeUrl(mContext);
		WebView iframe = (WebView) findViewById(R.id.iframe);
		iframe.getSettings().setJavaScriptEnabled(true);
		iframe.loadUrl(url);
	}

}
