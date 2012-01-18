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

import android.app.Activity;
import android.app.Dialog;
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

import com.facebook.android.Facebook;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.jonathanhester.c2dm.C2DMessaging;
import com.jonathanhester.friendwatcher.client.MyRequestFactory;
import com.jonathanhester.friendwatcher.shared.FriendWatcherRequest;

/**
 * Main activity - requests "Hello, World" messages from the server and provides
 * a menu item to invoke the accounts activity.
 */
public class FriendWatcherActivity extends Activity {
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
			showUnfriended();
		}
	};

	private void c2dmError() {
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
		setContentView(R.layout.friend_watcher);
		Button c2dmReg = (Button) findViewById(R.id.c2dm_reg);
		c2dmReg.setVisibility(View.GONE);
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		registerReceiver(mUpdateUIReceiver, new IntentFilter(
				Util.UPDATE_UI_INTENT));
		
		if (authedFb()) {
			verifyServerToken();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!authedFb()) {
			doFbAuth();
		} else if (!c2dmRegistered()) {
			// Register a receiver to provide register/unregister notifications
			registerC2DM();
			startLoading();
		} else 
			showUnfriended();
	}

	ProgressDialog progressDialog;

	public void startLoading() {
		progressDialog = ProgressDialog.show(this, null, "Loading");
	}

	public void stopLoading() {
		progressDialog.dismiss();
	}
	
	private void doFbAuth() {
		//first clear registration id so we'll reregister with new fb creds
		SharedPreferences settings = Util.getSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(Util.DEVICE_REGISTRATION_ID);
        editor.commit();
		Intent authFbIntent = new Intent(this, FbAuthActivity.class);
		startActivity(authFbIntent);
	}

	private boolean authedFb() {
		String accessToken = Util.getSharedPreferences(mContext).getString(
				Util.ACCESS_TOKEN, null);
		if (accessToken == null)
			return false;

		return true;

	}

	private void verifyServerToken() {
		String accessToken = Util.getSharedPreferences(mContext).getString(
				Util.ACCESS_TOKEN, null);
		String fbId = Util.getSharedPreferences(mContext).getString(
				Util.ACCOUNT_NAME, null);
		MyRequestFactory requestFactory = Util.getRequestFactory(mContext,
				MyRequestFactory.class);
		final FriendWatcherRequest request = requestFactory
				.friendWatcherRequest();
		request.verifyToken(fbId, accessToken).fire(new Receiver<Boolean>() {
			@Override
			public void onFailure(ServerFailure failure) {

			}

			@Override
			public void onSuccess(Boolean response) {
				if (!response.booleanValue()) {
					FacebookFriendsChecker.saveFbCreds(mContext, null, null);
					doFbAuth();
				}
			}
		});

	}

	private boolean c2dmRegistered() {
		String deviceRegistrationId = Util.getSharedPreferences(mContext)
				.getString(Util.DEVICE_REGISTRATION_ID, null);
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
		C2DMessaging.register(mContext, Setup.SENDER_ID);
	}

	Facebook facebook = new Facebook(Util.getFacebookId());

	private void showUnfriended() {
		String url = Util.getIframeUrl(mContext);
		WebView iframe = (WebView) findViewById(R.id.iframe);
		iframe.getSettings().setJavaScriptEnabled(true);
		iframe.loadUrl(url);

		// diffedFriends.setAdapter(adapter);
		// String accessToken = Util.getSharedPreferences(mContext).getString(
		// Util.ACCESS_TOKEN, null);
		//
		// facebook.setAccessToken(accessToken);
		// String friendJson = FacebookFriendsChecker
		// .getFriendsFromFacebookJson(facebook);
		// ArrayList<FacebookUser> friendsList = FacebookFriendsChecker
		// .getFriendsFromJson(friendJson);
		// ArrayList<FacebookUser> unfriendedList = FacebookFriendsChecker
		// .getDiffedFriendsList(facebook, this, friendsList);
		//
		// FacebookFriendsChecker.storeFriendsData(this, friendJson);
		//
		// ListView diffedFriends = (ListView) findViewById(R.id.facebookUsers);
		// FacebookUserListAdapter adapter = new FacebookUserListAdapter(this,
		// R.layout.facebook_user, unfriendedList);
		// diffedFriends.setAdapter(adapter);
	}

}
