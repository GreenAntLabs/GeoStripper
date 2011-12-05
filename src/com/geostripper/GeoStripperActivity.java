package com.geostripper;


import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

public class GeoStripperActivity extends Activity {
	public static final String TAG = "GeoStripperActivity";
	public static final String PREFS_NAME = "GeoStripperPreferences";
	public static final String GALLERY_INTENT_URI_KEY = "Gallery Intent";
	public static final String GALLERY_NAME_KEY = "Gallery Name";
	public static final String GALLERY_ICON_KEY = "Gallery Icon";

	static final int SELECT_IMAGE_REQUEST = 66;
	TextView chooserText;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		GeoStripper.APP_CONTEXT = getApplicationContext();

		Log.d(TAG, "Arrived with intent: " + getIntent().toString());

		setContentView(R.layout.main);
		Button button = (Button) findViewById(R.id.chooserButton);
		chooserText = (TextView) findViewById(R.id.chooserText);
		
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String galleryName = settings.getString(GALLERY_NAME_KEY, null);
		// If gallery name/intent has not been stored in the settings, default to first intent match
		if(galleryName == null)
		{
			setGalleryIntent(getResolveInfosForIntent(getIntent())[0]);
		}
		else
		{
			chooserText.setText(galleryName);
		}
		
		
		
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				listIntents();
			}
		});

		((Button) findViewById(R.id.launcherButton))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivityForResult(getStoredIntent(),
								SELECT_IMAGE_REQUEST);
					}
				});
		;

	}
 
	private AlertDialog buildDialog(ResolveInfo[] infos) {

		final ListAdapter adapter = new ArrayAdapter<ResolveInfo>(
				getApplicationContext(), R.layout.activity_row, infos) {

			public View getView(int position, View convertView, ViewGroup parent) {
				final LayoutInflater inflater = (LayoutInflater) getApplicationContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				if (convertView == null) {
					convertView = inflater.inflate(R.layout.activity_row, null);
				}

				TextView title = (TextView) convertView
						.findViewById(R.id.title);
				ImageView icon = (ImageView) convertView
						.findViewById(R.id.icon);
				title.setText(getItem(position).loadLabel(getPackageManager()));
				icon.setImageDrawable(getItem(position).loadIcon(
						getPackageManager()));
				return convertView;
			}
		};

		DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, final int item) {
				setGalleryIntent((ResolveInfo) adapter.getItem(item));

			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(
				"Select Gallery").setAdapter(adapter, clickListener);

		return builder.create();

	}
   private Intent getStoredIntent()
    {
	   Intent newIntent = null;
	   SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	
	    String uri = settings.getString(GALLERY_INTENT_URI_KEY, "#Intent;action=android.intent.action.PICK;type=vnd.android.cursor.dir/image;component=com.cooliris.media/.Gallery;end");
	    try {
			newIntent =  Intent.getIntent(uri);
		} catch (URISyntaxException e) {
			
			e.printStackTrace();
		}
	    return newIntent;
    }
	private void setGalleryIntent(ResolveInfo info) {
		String title = info.loadLabel(getPackageManager()).toString();
		//Drawable icon = info.loadIcon(getPackageManager());
		Log.d(TAG, "Selected: " + title);
		chooserText.setText(title);

		Intent selectedIntent = cloneIntent(getIntent());
		selectedIntent.setClassName(info.activityInfo.packageName,
				info.activityInfo.name);
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		
		editor.putString(GALLERY_INTENT_URI_KEY, selectedIntent.toURI());
		editor.putString(GALLERY_NAME_KEY, title);
		editor.commit();
	}

	private Intent cloneIntent(Intent intent) {
		String action = intent.getAction();
		String type = intent.getType();
		if (Intent.ACTION_MAIN.equals(action)) {
			action = Intent.ACTION_PICK;
			type = "vnd.android.cursor.dir/image";
		}
		Intent newIntent = new Intent(action);
		newIntent.setType(type);
		return newIntent;

	}

	private ResolveInfo[] getResolveInfosForIntent(Intent intent) {
		List<ResolveInfo> riList = getPackageManager().queryIntentActivities(
				cloneIntent(intent), 0);

		ResolveInfo[] riArray = new ResolveInfo[riList.size() - 1];
		int idx = 0;
		for (int i = 0; i < riList.size(); i++) {
			ResolveInfo info = riList.get(i);

			if (!this.getClass().getName().equals(info.activityInfo.name)) {
				riArray[idx++] = info;
			}
		}

		return riArray;

	}

	private void listIntents() {

		Log.d(TAG, "Listing with intent: " + getIntent().toString());

		AlertDialog dialog = buildDialog(getResolveInfosForIntent(getIntent()));
		dialog.show();

	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult: request=" + requestCode + " result="
				+ resultCode);
		if (SELECT_IMAGE_REQUEST == requestCode
				&& Activity.RESULT_OK == resultCode) {
			Uri imageUri = data.getData();
			String filename = getRealPathFromURI(imageUri);
			Log.d(TAG, "Selected Image:" + filename);
			try {
				String newFile = GeoStripper.stripGeoTags(filename);
				
				Log.d(TAG, "Got geoStripped file: " + newFile);
				if(!filename.equals(newFile))
				{
					Uri newUri = Uri.fromFile(new File(newFile));
					data.setData(newUri);
				}
				setResult(Activity.RESULT_OK,data);
				finish();
			} catch (Exception ioe) {
				ioe.printStackTrace();
			}
		}

	}

	private String getRealPathFromURI(Uri contentUri) {

		// can post image
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(contentUri, proj, // Which columns to
														// return
				null, // WHERE clause; which rows to return (all rows)
				null, // WHERE clause selection arguments (none)
				null); // Order-by clause (ascending by name)
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();

		return cursor.getString(column_index);
	}

}