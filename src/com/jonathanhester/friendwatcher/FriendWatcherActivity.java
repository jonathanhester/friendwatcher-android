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
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.android.Facebook;
import com.jonathanhester.c2dm.C2DMessaging;
import com.jonathanhester.friendwatcher.requests.FriendWatcherRequest;
import com.jonathanhester.friendwatcher.requests.MyRequestFactory;
import com.jonathanhester.requestFactory.Receiver;
import com.jonathanhester.requestFactory.ServerFailure;

/**
 * Main activity - requests "Hello, World" messages from the server and provides
 * a menu item to invoke the accounts activity.
 */
public class FriendWatcherActivity extends FragmentActivity {
	/**
	 * Tag for logging.
	 */
	private static final String TAG = "FriendWatcherActivity";

	/**
	 * The current context.
	 */
	private Context mContext = this;

	private int createUserFailures = 0;

	private FriendsListFragment friendsFragment;

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
			if (status == DeviceRegistrar.REGISTERED_STATUS)
				showUnfriended();
		}
	};

	private void c2dmError() {
		Tracker.getInstance().requestFail(Tracker.TYPE_C2DM);
	}

	/**
	 * Begins the activity.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i(TAG, "onCreate");

		// Create the list fragment and add it as our sole content.
		if (getSupportFragmentManager().findFragmentById(android.R.id.content) == null) {
			friendsFragment = new FriendsListFragment();
			getSupportFragmentManager().beginTransaction()
					.add(android.R.id.content, friendsFragment).commit();
		}

		registerReceiver(mUpdateUIReceiver, new IntentFilter(
				Util.UPDATE_UI_INTENT));
		Util.setSharedPreference(mContext, Util.LIST_VALID, null);
	}

	@Override
	protected void onStart() {
		super.onStart();
		reloadState();
	}

	private void reloadState() {
		if (Util.getSharedPreferences(mContext).getString(Util.SKIP_WELCOME,
				null) == null) {
			startActivity(new Intent(this, WelcomeActivity.class));
		} else if (!authedFb()) {
			doFbAuth();
		} else if (Util.getSharedPreferences(mContext).getString(Util.USER_ID,
				null) == null) {
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
		progressDialog = ProgressDialog.show(this, "", message);
		Log.d("dialog", "Showing dialog: " + progressDialog);
	}

	public void stopLoading() {
		Tracker.getInstance().stopTimeEvent(Tracker.TYPE_TIME_LOADING);
		progressDialog.dismiss();
		Log.d("dialog", "Stopping dialog: " + progressDialog);
	}

	public void createUser() {
		startLoading("Validating Facebook user...");
		final FriendWatcherRequest request = MyRequestFactory
				.friendWatcherRequest(mContext);
		request.validateUser(fbId(), token()).fire(new Receiver<String>() {
			@Override
			public void onFailure(ServerFailure failure) {
				createUserFailures++;
				Tracker.getInstance().requestFail(
						Tracker.TYPE_REQUEST_VALIDATE_USER, 0);
				stopLoading();
				Util.saveFbCreds(mContext, null, null, null);
				if (createUserFailures > 3)
					Toast.makeText(mContext, "Error creating user",
							Toast.LENGTH_SHORT).show();
				else
					doFbAuth();
			}

			@Override
			public void onSuccess(String response) {
				Tracker.getInstance().requestSuccess(
						Tracker.TYPE_REQUEST_VALIDATE_USER,
						response.equals("1"));
				stopLoading();

				if (!response.equals("1")) {
					Toast.makeText(mContext,
							"Unable to verify user. Let's try again",
							Toast.LENGTH_SHORT).show();
					Util.saveFbCreds(mContext, null, null, null);
					doFbAuth();
				} else {
					Util.setSharedPreference(mContext, Util.USER_ID, response);
					Toast.makeText(mContext, "Success!", Toast.LENGTH_SHORT)
							.show();
					reloadState();
				}
			}
		});

	}

	private void doFbAuth() {
		// first clear registration id so we'll reregister with new fb creds
		Util.setSharedPreference(mContext, Util.DEVICE_REGISTRATION_ID, null);
		Intent authFbIntent = new Intent(this, FbAuthActivity.class);
		startActivityForResult(authFbIntent, 1);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		String fbId = data.getStringExtra(Util.FBID);
		String token = data.getStringExtra(Util.TOKEN);
		Util.saveFbCreds(mContext, token, fbId, null);
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
		String fbId = Util.getSharedPreferences(mContext).getString(Util.FBID,
				null);
		final FriendWatcherRequest request = MyRequestFactory
				.friendWatcherRequest(mContext);
		Tracker.getInstance().requestStart(Tracker.TYPE_REQUEST_VERIFY);
		request.verifyToken(fbId, accessToken).fire(new Receiver<String>() {
			@Override
			public void onFailure(ServerFailure failure) {
				Tracker.getInstance().requestFail(Tracker.TYPE_REQUEST_VERIFY,
						0);
				Util.saveFbCreds(mContext, null, null, null);
			}

			@Override
			public void onSuccess(String response) {
				Tracker.getInstance().requestSuccess(
						Tracker.TYPE_REQUEST_VERIFY, response.equals("1"));
				if (!response.equals("1")) {
					Util.saveFbCreds(mContext, null, null, null);
					doFbAuth();
				}
			}
		});
	}

	private boolean c2dmRegistered() {
		String deviceRegistrationId = Util.getSharedPreferences(mContext)
				.getString(Util.DEVICE_REGISTRATION_ID, null);
		Tracker.getInstance().requestSuccess(Tracker.TYPE_C2DM,
				(deviceRegistrationId != null));
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
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.refresh:
			Util.setSharedPreference(mContext, Util.LIST_VALID, null);
			showUnfriended();
			return true;
		case R.id.reconnect:
			reconnectToFb();
			return true;
		case R.id.refresh_gcm:
			registerC2DM();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void registerC2DM() {
		Tracker.getInstance().requestStart(Tracker.TYPE_C2DM);
		C2DMessaging.register(mContext, Setup.SENDER_ID);
	}

	Facebook facebook = new Facebook(Util.getFacebookId());

	private void showUnfriended() {
		String listValid = Util.getSharedPreferences(mContext).getString(
				Util.LIST_VALID, null);
		if (listValid != null)
			return;
		final FriendWatcherRequest request = MyRequestFactory
				.friendWatcherRequest(mContext);
		startLoading("Fetching list...");
		request.fetchFriends(fbId(), token()).fire(new Receiver<String>() {
			@Override
			public void onFailure(ServerFailure failure) {
				Tracker.getInstance().requestFail(Tracker.TYPE_REQUEST_VERIFY,
						0);
				stopLoading();
			}

			@Override
			public void onSuccess(String response) {
				stopLoading();
				Util.setSharedPreference(mContext, Util.LIST_VALID, "1");
				FriendData data = FriendData.fromJson(response);
				friendsFragment.updateFriendData(data);
			}
		});

	}

	private void reconnectToFb() {
		Util.saveFbCreds(mContext, null, null, null);
		reloadState();
	}

}
