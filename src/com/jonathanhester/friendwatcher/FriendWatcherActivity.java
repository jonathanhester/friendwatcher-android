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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;

import com.facebook.android.Facebook;
import com.google.android.c2dm.C2DMessaging;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.jonathanhester.friendwatcher.client.MyRequestFactory;
import com.jonathanhester.friendwatcher.client.MyRequestFactory.HelloWorldRequest;

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
				message = getResources().getString(
						R.string.registration_error);
			}

			// Display a notification
			SharedPreferences prefs = Util.getSharedPreferences(mContext);
			String accountName = prefs.getString(Util.ACCOUNT_NAME, "Unknown");
			Util.generateNotification(mContext,
					String.format(message, accountName));
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
		c2dmReg.setEnabled(true);
	}

	/**
	 * Begins the activity.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.friend_watcher);
		Button c2dmReg = (Button) findViewById(R.id.c2dm_reg);
		c2dmReg.setEnabled(false);
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		registerReceiver(mUpdateUIReceiver, new IntentFilter(
				Util.UPDATE_UI_INTENT));

	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!authedFb()) {
			Intent authFbIntent = new Intent(this, FbAuthActivity.class);
			startActivity(authFbIntent);
		} else if (!c2dmRegistered()) {
			// Register a receiver to provide register/unregister notifications
			registerC2DM();
		}
		showUnfriended();
	}

	private boolean authedFb() {
		String accessToken = Util.getSharedPreferences(mContext).getString(
				Util.ACCESS_TOKEN, null);
		if (accessToken == null)
			return false;
		return true;

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

	private void setHelloWorldScreenContent() {
		final Button sayHelloButton = (Button) findViewById(R.id.c2dm_reg);
		sayHelloButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showUnfriended();
				registerC2DM();
				// sayHelloButton.setEnabled(false);
				// helloWorld.setText(R.string.contacting_server);

				// Use an AsyncTask to avoid blocking the UI thread
				new AsyncTask<Void, Void, String>() {
					private String message;

					@Override
					protected String doInBackground(Void... arg0) {
						MyRequestFactory requestFactory = Util
								.getRequestFactory(mContext,
										MyRequestFactory.class);
						final HelloWorldRequest request = requestFactory
								.helloWorldRequest();
						String accountName = Util
								.getSharedPreferences(mContext).getString(
										Util.ACCOUNT_NAME, "<none>");
						Log.i(TAG, "Sending request to server for account "
								+ accountName);
						request.getMessage().fire(new Receiver<String>() {
							@Override
							public void onFailure(ServerFailure error) {
								message = "Failure: " + error.getMessage();
							}

							@Override
							public void onSuccess(String result) {
								message = result;
							}
						});
						return message;
					}

					@Override
					protected void onPostExecute(String result) {
						// helloWorld.setText(result);
						sayHelloButton.setEnabled(true);
					}
				};// .execute();
			}
		});
	}

	private void showUnfriended() {
		String url = Util.getIframeUrl(mContext);
		WebView iframe = (WebView) findViewById(R.id.iframe);
		iframe.getSettings().setJavaScriptEnabled(true);
		iframe.loadUrl(url);

//		diffedFriends.setAdapter(adapter);
//		String accessToken = Util.getSharedPreferences(mContext).getString(
//				Util.ACCESS_TOKEN, null);
//
//		facebook.setAccessToken(accessToken);
//		String friendJson = FacebookFriendsChecker
//				.getFriendsFromFacebookJson(facebook);
//		ArrayList<FacebookUser> friendsList = FacebookFriendsChecker
//				.getFriendsFromJson(friendJson);
//		ArrayList<FacebookUser> unfriendedList = FacebookFriendsChecker
//				.getDiffedFriendsList(facebook, this, friendsList);
//
//		FacebookFriendsChecker.storeFriendsData(this, friendJson);
//
//		ListView diffedFriends = (ListView) findViewById(R.id.facebookUsers);
//		FacebookUserListAdapter adapter = new FacebookUserListAdapter(this,
//				R.layout.facebook_user, unfriendedList);
//		diffedFriends.setAdapter(adapter);
	}

}
