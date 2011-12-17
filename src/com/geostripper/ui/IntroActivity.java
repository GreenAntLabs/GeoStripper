package com.geostripper.ui;

import com.geostripper.R;

import android.app.Activity;
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
	private TextSwitcher introText;
	private final int[] textIds = {R.string.introText1, R.string.introText2};
	private int textIndex = 0;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intro);
		//build textswitcher
		introText = (TextSwitcher)findViewById(R.id.introTextView);		
		introText.setFactory(new ViewFactory() {

			@Override
			public View makeView() {
				TextView t = new TextView(IntroActivity.this);
				t.setTextSize(15f);
				return t;
			}
		});

		//set first text message		
		setMessage(textIds[textIndex++]);
		//specify the animation after the first message, so that
		//later messages would slide in
		introText.setInAnimation(AnimationUtils.loadAnimation(this,
				R.anim.right_slide_in));
		introText.setOutAnimation(AnimationUtils.loadAnimation(this,
				R.anim.left_slide_out));
				
		Button continueButton = (Button) findViewById(R.id.introContinueButton);
		continueButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//show as many intro messages as we have
				if(textIndex < textIds.length)
				{
					setMessage(textIds[textIndex++]);
				}
				else //and then end activity and return back to main activity
				{
					setResult(RESULT_OK);
					finish();
					
					//change finish animation to mimic natural flow of things
					overridePendingTransition  (R.anim.right_slide_in, R.anim.left_slide_out);
				}
			}
		});
		
	}
	
	/**
	 *	Display styled text of the message referenced by the provided resource id 
	 * @param messageResourceId resource id of the text message
	 */
	private void setMessage(int messageResourceId)
	{
		String styledText = getResources().getString(messageResourceId);
		final TextView t = (TextView) introText.getNextView();
		//set styled text in the TextView contained inside TextSwitcher
		t.setText(Html.fromHtml(styledText), TextView.BufferType.SPANNABLE);
		introText.showNext();
		
	}
}
