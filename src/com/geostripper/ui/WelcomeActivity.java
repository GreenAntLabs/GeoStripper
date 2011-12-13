package com.geostripper.ui;

import com.geostripper.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class WelcomeActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		TextView welcomeText = (TextView)findViewById(R.id.welcomeTextView);
		
		String styledText = getResources().getString(R.string.welcomeText);
		welcomeText.setText(Html.fromHtml(styledText), TextView.BufferType.SPANNABLE);
		
		Button continueButton = (Button) findViewById(R.id.continueButton);
		continueButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				
			}
		});
		
	}
}
