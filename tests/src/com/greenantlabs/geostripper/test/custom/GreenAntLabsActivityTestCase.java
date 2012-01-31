package com.greenantlabs.geostripper.test.custom;



import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

public class GreenAntLabsActivityTestCase<T extends Activity> extends
		ActivityInstrumentationTestCase2<T> {
	
	public GreenAntLabsActivityTestCase(Class<T> activityClass) {
		super(activityClass);
		// TODO Auto-generated constructor stub
	}
	
	public void assertFinishCalledWithResultCode(int expectedResultCode) {
		assertFinishCalledWithResultCodeAndIntent(expectedResultCode, null);
	}
	public void assertFinishCalledWithResultCodeAndIntent(int expectedResultCode, Intent expectedResultIntent) {
		try {
			Field f = Activity.class.getDeclaredField("mResultCode");
			f.setAccessible(true);
			int actualResultCode = (Integer)f.get(getActivity());
			assertEquals(expectedResultCode, actualResultCode);
			if (expectedResultIntent != null) {
				f = Activity.class.getDeclaredField("mResultData");
				f.setAccessible(true);
				assertEquals(expectedResultIntent, (Intent)f.get(getActivity()));
			}
				
			
		} catch (NoSuchFieldException e) {
			throw new RuntimeException("Looks like the Android Activity class has changed it's   private fields for mResultCode or mResultData.  Time to update the reflection code.", e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
