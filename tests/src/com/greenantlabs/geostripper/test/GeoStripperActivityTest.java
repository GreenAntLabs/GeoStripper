package com.greenantlabs.geostripper.test;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.mock.MockContext;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.greenantlabs.geostripper.R;
import com.greenantlabs.geostripper.test.custom.GreenAntLabsActivityTestCase;
import com.greenantlabs.geostripper.ui.GeoStripperActivity;
import com.greenantlabs.geostripper.ui.IntroActivity;

public class GeoStripperActivityTest extends GreenAntLabsActivityTestCase<GeoStripperActivity> {
	public static final String TAG = "GeoStripperActivityTest";
	public GeoStripperActivityTest() {
		super(GeoStripperActivity.class);
		
	}
	
	/**
	 * During Initial launch, the gallery name preference is not set, so Intro activity must launched
	 */
	public void testIntroActivityLaunchesIfGalleryIsNotSet() {
		ActivityMonitor introMonitor = getInstrumentation().addMonitor(IntroActivity.class.getName(), null, false);
		
		// remove any gallery_name preferences
		SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(getInstrumentation().getTargetContext()).edit();
		prefs.remove(GeoStripperActivity.GALLERY_NAME_KEY);
		prefs.commit();
	
		getInstrumentation().startActivitySync(buildStandaloneLaunchIntent());
		
		Activity introActivity = getInstrumentation().waitForMonitorWithTimeout(introMonitor,500);
		// intro activity was launched
		assertNotNull(introActivity);
		
		getInstrumentation().removeMonitor(introMonitor);
		
	}
	
	/**
	 * If gallery name preference has been previously set, Intro Activity should not be launched
	 */
	public void testIntroActivityDoesNotLaunchIfGalleryIsSet() {
		ActivityMonitor introMonitor = getInstrumentation().addMonitor(IntroActivity.class.getName(), null, false);
		
		// add a gallery_name preference
		SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(getInstrumentation().getTargetContext()).edit();
		prefs.putString(GeoStripperActivity.GALLERY_NAME_KEY, "Gallery");
		prefs.commit();
		
		getInstrumentation().startActivitySync(buildStandaloneLaunchIntent());
	      
		Activity introActivity = getInstrumentation().waitForMonitorWithTimeout(introMonitor,500);
		// intro activity was not launched
		assertNull(introActivity);	
		
		getInstrumentation().removeMonitor(introMonitor);
		
	}
	
	public void testLaunchedAsStandalone()
	{
		ActivityMonitor gsMonitor = getInstrumentation().addMonitor(GeoStripperActivity.class.getName(), null, false);
		
		// add a gallery_name preference
		SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(getInstrumentation().getTargetContext()).edit();
		prefs.putString(GeoStripperActivity.GALLERY_NAME_KEY, "Gallery");
		prefs.commit();
		
		getInstrumentation().startActivitySync(buildStandaloneLaunchIntent());
	      
		Activity gsActivity = getInstrumentation().waitForMonitorWithTimeout(gsMonitor,500);
		// geostripper activity was launched
		assertNotNull(gsActivity);	
		//gallery that was stored in the prefs is selected
		assertEquals("Gallery", ((TextView) gsActivity.findViewById(R.id.galleryName)).getText());
		
		Button continueButton = ((Button) gsActivity.findViewById(R.id.mainContinueButton));
		// continue button should not be available in standalone mode
		assertNull(continueButton);
		getInstrumentation().removeMonitor(gsMonitor);
	}
	
	public void testLaunchedFromOtherApp()
	{
		try{
			ActivityMonitor gsMonitor = getInstrumentation().addMonitor(GeoStripperActivity.class.getName(), null, false);
			IntentFilter filter = new IntentFilter(Intent.ACTION_PICK, "vnd.android.cursor.dir/image");
			ActivityMonitor galleryMonitor = getInstrumentation().addMonitor(filter, null, false);
			// add a gallery_name preference
			SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(getInstrumentation().getTargetContext()).edit();
			prefs.putString(GeoStripperActivity.GALLERY_NAME_KEY, "Gallery");
			prefs.commit();
			
			getInstrumentation().startActivitySync(buildAnotherAppLaunchIntent());
		      
			Activity gsActivity = getInstrumentation().waitForMonitorWithTimeout(gsMonitor,500);
			// geostripper activity was launched
			assertNotNull(gsActivity);	
			
			galleryMonitor.waitForActivityWithTimeout(500);
			// corresponding gallery activity was launched
			assertEquals(1, galleryMonitor.getHits());
			
			
			getInstrumentation().removeMonitor(galleryMonitor);
			getInstrumentation().removeMonitor(gsMonitor);
		
		} catch(Exception mmte)
		{
			Log.e(TAG, mmte.getLocalizedMessage());
			fail(mmte.getLocalizedMessage());
;		}
		
		
	}
	
	private Intent buildAnotherAppLaunchIntent(){
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("vnd.android.cursor.dir/image");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setClassName(getInstrumentation().getTargetContext(), GeoStripperActivity.class.getName());
		return intent;	
	}
	private Intent buildStandaloneLaunchIntent(){
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setClassName(getInstrumentation().getTargetContext(), GeoStripperActivity.class.getName());
		return intent;
	}
	

}
