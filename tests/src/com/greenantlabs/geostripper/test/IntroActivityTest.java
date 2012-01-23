package com.greenantlabs.geostripper.test;

import com.greenantlabs.geostripper.R;
import com.greenantlabs.geostripper.ui.IntroActivity;

import android.test.ActivityInstrumentationTestCase2;
import android.text.Html;
import android.text.Spanned;
import android.widget.Button;
import android.widget.TextSwitcher;
import android.widget.TextView;

public class IntroActivityTest extends
		ActivityInstrumentationTestCase2<IntroActivity> {

	private IntroActivity introActivity;
	private TextSwitcher introText;
	private Button continueButton;
	public IntroActivityTest() {
	      super("com.greenantlabs.geostripper.ui", IntroActivity.class);
	    }
	
	@Override
    protected void setUp() throws Exception {
        super.setUp();
        introActivity = this.getActivity();
        introText = (TextSwitcher)introActivity.findViewById(R.id.introTextView);
		continueButton = (Button)introActivity.findViewById(R.id.introContinueButton);
    }

	public void testInitialIntroText() throws Exception
	{
		CharSequence actual = ((TextView)introText.getCurrentView()).getText();
		Spanned expected = Html.fromHtml(introActivity.getString(R.string.introText1));
		assertEquals(expected.toString(), actual.toString());
	}
}
