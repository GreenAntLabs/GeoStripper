package com.greenantlabs.geostripper.test;


import com.greenantlabs.geostripper.R;
import com.greenantlabs.geostripper.test.custom.GreenAntLabsActivityTestCase;
import com.greenantlabs.geostripper.ui.IntroActivity;
import android.app.Activity;
import android.text.Html;
import android.text.Spanned;
import android.widget.Button;
import android.widget.TextSwitcher;
import android.widget.TextView;

public class IntroActivityTest extends
	GreenAntLabsActivityTestCase<IntroActivity> {

	private IntroActivity introActivity;
	private TextSwitcher introText;
	private Button continueButton;
	public IntroActivityTest() {
	      super(IntroActivity.class);
	    }
	
	@Override
    protected void setUp() throws Exception {
        super.setUp();
        introActivity = this.getActivity();
        introText = (TextSwitcher)introActivity.findViewById(R.id.introTextView);
        continueButton = (Button)introActivity.findViewById(R.id.introContinueButton);
        
	}

	public void testInitialIntroText() throws Exception {
		CharSequence actual = ((TextView)introText.getCurrentView()).getText();
		Spanned expected = Html.fromHtml(introActivity.getString(R.string.introText));
		assertEquals(expected.toString(), actual.toString());
	}
	
	public void testContinueFinishesIntroActivity() throws Exception {
		introActivity.runOnUiThread( new Runnable() {
			public void run() {
				continueButton.performClick();
			} 
		}); 
		assertFinishCalledWithResultCode(Activity.RESULT_OK);
	}
	
	
	

}
