package com.jonathanhester.friendwatcher;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.google.android.apps.analytics.easytracking.TrackedActivity;

public class FbAuthActivity extends TrackedActivity {

	Facebook facebook;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fb_auth);
		
		Button button = (Button)findViewById(R.id.do_fb_auth);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				attemptAuth();
			}
		});
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		facebook = new Facebook(Util.getFacebookId()); //local
		attemptAuth();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		facebook.authorizeCallback(requestCode, resultCode, data);
	
		String friendsData = FacebookFriendsChecker
				.getFriendsFromFacebookJson(facebook);

		FacebookFriendsChecker.storeFriendsData(this, friendsData);
	}
	
	private void attemptAuth() {
		Tracker.getInstance().requestStart(Tracker.TYPE_FACEBOOK_AUTH);
		facebook.authorize(this, new String[] { "offline_access" },
				new DialogListener() {
					@Override
					public void onComplete(Bundle values) {
						Tracker.getInstance().requestSuccess(Tracker.TYPE_FACEBOOK_AUTH, 0);
						String fbId = "";
						JSONObject user;
						try {
							String response = facebook.request("me");
							user = com.facebook.android.Util.parseJson(response);
							fbId = (String) user.get("id");
						} catch (Exception e) {
							e.getMessage();
						} catch (FacebookError e) {
							e.getMessage();
						}

						FacebookFriendsChecker.saveFbCreds(FbAuthActivity.this, facebook.getAccessToken(), fbId);
						setResult(Activity.RESULT_OK);
						finish();
					}

					@Override
					public void onFacebookError(FacebookError error) {
						Log.d("FB auth", error.getMessage());
						Tracker.getInstance().requestFail(Tracker.TYPE_FACEBOOK_AUTH, 0);
					}

					@Override
					public void onError(DialogError e) {
						Tracker.getInstance().requestFail(Tracker.TYPE_FACEBOOK_AUTH, 0);
					}

					@Override
					public void onCancel() {
						Tracker.getInstance().requestFail(Tracker.TYPE_FACEBOOK_AUTH, 1);
					}
				});

	}
}
