package com.geostripper;

import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
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
	
			
			

	static final int SELECT_IMAGE_REQUEST = 66;
	TextView chooserText;
	Intent selectedIntent;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		GeoStripper.APP_CONTEXT = getApplicationContext();
		
		
		Log.d(TAG, "Arrived with intent: " + getIntent().toString());
		
		setContentView(R.layout.main);
		Button button = (Button) findViewById(R.id.chooserButton);
		chooserText = (TextView)findViewById(R.id.chooserText);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				listIntents();
			}
		});
		
		((Button) findViewById(R.id.launcherButton)).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivityForResult(selectedIntent,SELECT_IMAGE_REQUEST);
					}
				});;

	}
	
	private AlertDialog buildDialog(ResolveInfo[] infos)
    {
    	
		
		
    	final ListAdapter adapter = new ArrayAdapter<ResolveInfo>(
    	                getApplicationContext(), R.layout.activity_row, infos) 
    	{
    	               
    	             
    	 
    	        public View getView(int position, View convertView, ViewGroup parent) 
    	        {
	                final LayoutInflater inflater = (LayoutInflater) getApplicationContext()
	                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	 
	                if (convertView == null) {
	                	convertView = inflater.inflate( R.layout.activity_row, null);
	                }
	                
	                TextView title =  (TextView) convertView.findViewById(R.id.title);
	                ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
	                title.setText(getItem(position).loadLabel(getPackageManager()));
	                icon.setImageDrawable(getItem(position).loadIcon(getPackageManager()));
	                return convertView;
    	        }
    	};
    	
    	DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener()
    	{
    		public void onClick(DialogInterface dialog, final int item)
    		{
    			ResolveInfo info = (ResolveInfo)adapter.getItem(item);
    			String title = info.loadLabel(getPackageManager()).toString(); 
    			Log.d(TAG, "Selected: "+title);
    			chooserText.setText(title);
    			
    			selectedIntent = cloneIntent(getIntent());
    			selectedIntent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
    			//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

    		}
    	};
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("Select Gallery").setAdapter(adapter, clickListener);

        return builder.create();
    
    }

	private Intent cloneIntent(Intent intent)
	{
		String action = intent.getAction();
		String type = intent.getType();
		if(Intent.ACTION_MAIN.equals(action))
		{	
			action = Intent.ACTION_PICK;
			type = "vnd.android.cursor.dir/image";
		}
		Intent newIntent = new Intent(action);
		newIntent.setType(type);
		return newIntent;
		
	}
	
	private void listIntents() {
		Intent intent = cloneIntent(getIntent());
		Log.d(TAG, "Listing with intent: " + intent.toString());
		
		
		List<ResolveInfo> pkgAppsList = getPackageManager()
				.queryIntentActivities(intent, 0);

		Iterator<ResolveInfo> iterator = pkgAppsList.iterator();
		while (iterator.hasNext()) {
			ResolveInfo info = iterator.next();
			Log.d("ResolveInfo", info.toString());
			Log.d(" = ActionInfo ", info.activityInfo.toString());
			if (info.filter != null) {
				String action = "";
				for (int i = 0; i < info.filter.countActions(); i++) {
					action += info.filter.getAction(i) + "   ";
				}
				Log.d(" == IntentFilter ", action);

			}
		}
		
		AlertDialog dialog = buildDialog(pkgAppsList.toArray(new ResolveInfo[0]));
		dialog.show();

	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult: request="+requestCode+" result="+resultCode);
		if(SELECT_IMAGE_REQUEST==requestCode && Activity.RESULT_OK==resultCode)
		{
			Uri imageUri = data.getData();
			String filename = getRealPathFromURI(imageUri);
			Log.d(TAG, "Selected Image:"+filename);
			try
			{
				String newFile = GeoStripper.stripGeoTags(filename);
				
				Log.d(TAG, "Got geoStripped file: " + newFile);
			}
			catch(Exception ioe)
			{
				ioe.printStackTrace();
			}
		}
		
	}
	
	private String getRealPathFromURI(Uri contentUri) {

        // can post image
        String [] proj={MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery( contentUri,
                        proj, // Which columns to return
                        null,       // WHERE clause; which rows to return (all rows)
                        null,       // WHERE clause selection arguments (none)
                        null); // Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
}
	
}