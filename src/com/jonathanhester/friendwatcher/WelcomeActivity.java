package com.jonathanhester.friendwatcher;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class WelcomeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.welcome);

		Button nextButton = (Button) findViewById(R.id.button_next);
		nextButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				DataStore.setString(WelcomeActivity.this,
						DataStore.SKIP_WELCOME, "1");
				finish();
			}
		});
	}

}
