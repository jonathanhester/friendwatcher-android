package com.jonathanhester.friendwatcher;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class FbAuthActivity extends Activity {

	Facebook facebook;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		facebook = new Facebook(Util.getFacebookId()); //local
		attemptAuth();
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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		facebook.authorizeCallback(requestCode, resultCode, data);
	
		String friendsData = FacebookFriendsChecker
				.getFriendsFromFacebookJson(facebook);

		FacebookFriendsChecker.storeFriendsData(this, friendsData);
	}
	
	private void attemptAuth() {
		facebook.authorize(this, new String[] { "offline_access" },
				new DialogListener() {
					@Override
					public void onComplete(Bundle values) {
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
						finish();
					}

					@Override
					public void onFacebookError(FacebookError error) {
						Log.d("FB auth", error.getMessage());
					}

					@Override
					public void onError(DialogError e) {
					}

					@Override
					public void onCancel() {
					}
				});

	}
}
