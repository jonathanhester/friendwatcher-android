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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.util.Log;

import com.jonathanhester.friendwatcher.requests.MyRequestFactory;
import com.jonathanhester.friendwatcher.requests.RegistrationInfoRequest;
import com.jonathanhester.requestFactory.Receiver;
import com.jonathanhester.requestFactory.Request;
import com.jonathanhester.requestFactory.ServerFailure;

/**
 * Register/unregister with the third-party App Engine server using
 * RequestFactory.
 */
public class DeviceRegistrar {
	public static final String STATUS_EXTRA = "Status";

	public static final int REGISTERED_STATUS = 1;

	public static final int UNREGISTERED_STATUS = 2;

	public static final int ERROR_STATUS = 3;

	private static final String TAG = "DeviceRegistrar";

	public static void registerOrUnregister(final Context context,
			final String deviceRegistrationId, final boolean register) {
		final Intent updateUIIntent = new Intent(Util.UPDATE_UI_INTENT);

		SharedPreferences prefs = Util.getSharedPreferences(context);
		String fbId = prefs.getString(Util.FBID, null);
		String token = prefs.getString(Util.TOKEN, null);

		String deviceId = Secure.getString(context.getContentResolver(),
				Secure.ANDROID_ID);

		RegistrationInfoRequest request = MyRequestFactory
				.registrationInfoRequest(context);

		Request<String> req;
		if (register) {
			req = request.register(fbId, token, deviceRegistrationId, deviceId);
		} else {
			req = request.unregister(fbId, token, deviceRegistrationId,
					deviceId);
		}

		req.fire(new Receiver<String>() {
			@Override
			public void onFailure(ServerFailure failure) {
				Log.w(TAG, "Failure, got :" + failure.getMessage());
				updateUIIntent.putExtra(STATUS_EXTRA, ERROR_STATUS);
				context.sendBroadcast(updateUIIntent);
			}

			@Override
			public void onSuccess(String response) {
				SharedPreferences settings = Util.getSharedPreferences(context);
				SharedPreferences.Editor editor = settings.edit();
				if (register) {
					editor.putString(Util.DEVICE_REGISTRATION_ID,
							deviceRegistrationId);
				} else {
					editor.remove(Util.DEVICE_REGISTRATION_ID);
				}
				editor.commit();
				updateUIIntent.putExtra(STATUS_EXTRA,
						register ? REGISTERED_STATUS : UNREGISTERED_STATUS);
				context.sendBroadcast(updateUIIntent);
			}
		});
	}

}
