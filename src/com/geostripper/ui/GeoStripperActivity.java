package com.geostripper.ui;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.geostripper.R;
import com.geostripper.util.GeoStripper;


public class GeoStripperActivity extends Activity {
	public static final String TAG = "GeoStripperActivity";
	public static final String PREFS_NAME = "GeoStripperPreferences";
	public static final String GALLERY_INTENT_URI_KEY = "Gallery Intent";
	public static final String GALLERY_NAME_KEY = "Gallery Name";
	public static final String GALLERY_ICON_KEY = "Gallery Icon";
	public static final String GALLERY_ICON_FILENAME = "selected_gallery_icon.png";

	static final int SELECT_IMAGE_REQUEST = 66;
	TextView chooserText;
	ImageView galleryIcon;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		
		Intent i = new Intent(getApplicationContext(), WelcomeActivity.class);
		startActivityForResult(i,100);
		//Store application context in a static variable
		GeoStripper.APP_CONTEXT = getApplicationContext();

		Log.d(TAG, "Arrived with intent: " + getIntent().toString());

		setContentView(R.layout.main);
		chooserText = (TextView) findViewById(R.id.galleryName);
		galleryIcon = (ImageView) findViewById(R.id.galleryIcon);
		
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String galleryName = settings.getString(GALLERY_NAME_KEY, null);
		// If gallery name/intent has not been stored in the settings, default to first intent match
		if(galleryName == null)
		{
			setGalleryIntent(getResolveInfosForIntent(getIntent())[0]);
		}
		else //if gallery name was stored in the settings
		{
			//display pre-set gallery name
			chooserText.setText(galleryName);
			//try to load pre-set icon for gallery app
			Bitmap iconImage = loadIcon();
			if(iconImage!=null)
			{
				galleryIcon.setImageBitmap(iconImage);
			}
		}
		
		//galleryPref is a layout that contains the icon,name and the arrow
		ViewGroup galleryPref = ((ViewGroup)findViewById(R.id.galleryLayout));
		galleryPref.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Listing with intent: " + getIntent().toString());

				AlertDialog dialog = buildDialog(getResolveInfosForIntent(getIntent()));
				dialog.show();
			}
		});
		
		//Change setting row background to blue on touch down, to transparent on touch up
		galleryPref.setOnTouchListener(new OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
            	if(event.getAction()==MotionEvent.ACTION_DOWN)
            	{
            		v.setBackgroundColor(android.graphics.Color.BLUE );
            		v.invalidate();
            	}
            	else if(event.getAction()==MotionEvent.ACTION_UP)
            	{
            		v.setBackgroundColor(android.graphics.Color.TRANSPARENT );
            		v.invalidate();
            	}
            	//return false, since we want this event to propagate further
                return false;
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
   /**
    * Loads gallery app icon from previously saved file
    * 
    * @return loaded bitmap or null if no icon file is available
    */
    private Bitmap loadIcon()
    {
    	Bitmap icon = null;
    	FileInputStream in = null;
    	try
    	{
    		in = openFileInput(GALLERY_ICON_FILENAME);
    		icon = BitmapFactory.decodeStream(in);
    		
    		in.close();
    		in = null;
    	}
    	 catch (IOException ioe) {
  	       //consume error
    	 }
		finally
		{
			if (in != null)
				try
				{
					in.close();
					in = null;
	            } catch (IOException e)
	            {
	
	            }
		}
    	return icon;
    }
    
    /**
     * Saves a drawable icon into a filesystem for later retrieval
     * @param icon
     */
    private void saveIcon(Drawable icon)
    {
    	if(icon!= null && icon instanceof BitmapDrawable)
    	{
    	   	FileOutputStream out = null;
        	try {
        		out = openFileOutput(GALLERY_ICON_FILENAME, Context.MODE_PRIVATE);
           	    ((BitmapDrawable)icon).getBitmap().compress(Bitmap.CompressFormat.PNG, 90, out);
           	    out.close();
           	    out = null;
           	    
        	} catch (Exception e) {
        	       e.printStackTrace();
        	}
    		finally
    		{
    			if (out != null)
    				try
    				{
    					out.close();
    					out = null;
    	            } catch (IOException e)
    	            {
    	
    	            }
    		}    		
    	}
    	else //icon is either null or not bitmap drawable
    	{
    		//erase existing file
    		deleteFile(GALLERY_ICON_FILENAME);
    	}
 
    	
    }
    /**
     * Displays selected gallery intent and saves it into shared preferences
     * @param info
     */
	private void setGalleryIntent(ResolveInfo info) {
		String title = info.loadLabel(getPackageManager()).toString();
		Drawable icon = info.loadIcon(getPackageManager());

		Log.d(TAG, "Selected: " + title);
		
		chooserText.setText(title);
		galleryIcon.setImageDrawable(icon);
		
		saveIcon(icon);
		//create a copy of intent
		Intent selectedIntent = cloneIntent(getIntent());
		//specify package attributes for later URI generation
		selectedIntent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
		
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		//store intent uri in shared preferences
		editor.putString(GALLERY_INTENT_URI_KEY, selectedIntent.toURI());
		//store displayable gallery app name in shared preferences
		editor.putString(GALLERY_NAME_KEY, title);
		editor.commit();
	}
	
	/**
	 * Creates a copy of provided intent and fixes the action/type if neccessary
	 * @param intent
	 * @return copy of intent with action/type fixed
	 */
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

	/**
	 * Queries for Activities that satisfy provided intent
	 * @param intent
	 * @return Array of ResolveInfo objects that represent the result of the query
	 */
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

	

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (SELECT_IMAGE_REQUEST == requestCode
				&& Activity.RESULT_OK == resultCode) {

			setResult(Activity.RESULT_OK, geoStripIntentData(data));
			finish();			
		}

	}
	
	/**
	 * Will geostrip the image stored in the intent if neccessary
	 * @param data
	 * @return
	 */
	private Intent geoStripIntentData(Intent data)
	{
		Uri imageUri = data.getData();
		String filename = getRealPathFromURI(imageUri);
		Log.d(TAG, "Selected Image:" + filename);
		try {
			//Attempt to strip tags from the selected file
			String newFile = GeoStripper.stripGeoTags(filename);
			
			Log.d(TAG, "Got geoStripped file: " + newFile);
			//store geostripped file in the intent if neccessary
			if(!filename.equals(newFile))
			{
				Uri newUri = Uri.fromFile(new File(newFile));
				data.setData(newUri);
			}
		} catch (Exception ioe) {
			//@TODO: deal with exceptions and passing stuff back
			ioe.printStackTrace();
		}
		return data;
	}
	
	/*
	 * Query the image uri for actual file name
	 *
	 */
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