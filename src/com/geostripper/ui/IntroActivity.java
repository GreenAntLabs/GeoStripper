package com.geostripper.ui;

import com.geostripper.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;

public class IntroActivity extends Activity {
	TextSwitcher introText;
	int[] textIds = {R.string.welcomeText1, R.string.welcomeText2};
	int textIndex = 0;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intro);
		introText = (TextSwitcher)findViewById(R.id.introTextView);
		
				
		introText.setFactory(new ViewFactory() {

			@Override
			public View makeView() {
				TextView t = new TextView(IntroActivity.this);
				t.setTextSize(15f);
				return t;
			}
		});

				
		setMessage(textIds[textIndex++]);
		introText.setInAnimation(AnimationUtils.loadAnimation(this,
				R.anim.right_slide_in));
		introText.setOutAnimation(AnimationUtils.loadAnimation(this,
				R.anim.left_slide_out));
				
		Button continueButton = (Button) findViewById(R.id.continueButton);
		continueButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(textIndex < textIds.length)
				{
					setMessage(textIds[textIndex++]);
				}
				else
				{
					setResult(RESULT_OK);
					finish();
					overridePendingTransition  (R.anim.right_slide_in, R.anim.left_slide_out);
				}
			}
		});
		
	}
	
	private void setMessage(int messageResourceId)
	{
		String styledText = getResources().getString(messageResourceId);
		final TextView t = (TextView) introText.getNextView();
		t.setText(Html.fromHtml(styledText), TextView.BufferType.SPANNABLE);
		introText.showNext();
		
	}
}
