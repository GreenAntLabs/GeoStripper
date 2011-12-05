package com.geostripper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.GPSTagConstants;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;

import android.content.Context;

import android.os.Environment;

import android.util.Log;

public class GeoStripper {
	public static final String TAG = "GeoStripper";
	public static Context APP_CONTEXT;
	protected static String TEMP_FOLDER = ".geostripped";

	
	public static String stripGeoTags(String filename) throws IOException, ImageReadException, ImageWriteException
	{

		OutputStream os = null;
		try
		{
			File file = new File(filename);
	
	        TiffOutputSet outputSet = null;
	
	        IImageMetadata metadata = Sanselan.getMetadata(file);
	        JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
	        boolean foundGPSTags=false;
	        //make sure that image has any EXIF metadata whatsoever
	        if (null != jpegMetadata)
	        {
                TiffImageMetadata exif = jpegMetadata.getExif();

                if (null != exif)
                {
                 
                	outputSet = exif.getOutputSet();
                }
                
                //Check if GPS Tags are present 
    	        for(int i=0;i< GPSTagConstants.ALL_GPS_TAGS.length;i++)
                {
    	        	TiffField field = jpegMetadata.findEXIFValue(GPSTagConstants.ALL_GPS_TAGS[i]);
                	if(field!=null)
                	{
                		foundGPSTags=true;
                		break;
                	}
                }
            }
	       
	        //if exif data is not present or gps tags are not present, just return the same file
            if (null == outputSet || foundGPSTags == false)
            {
                return filename;
            }
            
            //delete temp files if they were present
	        deleteTempFiles();
	        
            // remove gps tags
        	Log.d(TAG,"Found GPS Tags in '"+filename+"'. Proceeding with GeoStripping.");
			
            for(int i=0;i< GPSTagConstants.ALL_GPS_TAGS.length;i++)
            {
            	outputSet.removeField(GPSTagConstants.ALL_GPS_TAGS[i]);
            }
            
            //Setup the output file
    		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
    		{
    			throw new IOException("Your SD Card is not mounted. Please Insert it before proceeding");
    		}
    			
    		File geoStrippedDir = new File(APP_CONTEXT.getExternalFilesDir(null), TEMP_FOLDER);
    		if(!geoStrippedDir.exists())
    			geoStrippedDir.mkdir();
    		File destination = new File(geoStrippedDir,file.getName());
    		
            os = new FileOutputStream(destination);
            os = new BufferedOutputStream(os);

            new ExifRewriter().updateExifMetadataLossless(file, os,
                    outputSet);

            os.close();
            os = null;
            Log.d(TAG,"GPS Stripping Completed: '"+filename+"'.");
			
            return destination.getAbsolutePath();

		}
		finally
		{
			if (os != null)
                try
                {
                    os.close();
                } catch (IOException e)
                {

                }
		}
	}

	
	

	
	
	protected static void printTagValue(JpegImageMetadata jpegMetadata,
	            TagInfo tagInfo)
    {
        TiffField field = jpegMetadata.findEXIFValue(tagInfo);
        if (field == null)
            Log.d("Sanselan", tagInfo.name + ": " + "Not Found.");

        else
        	Log.d("Sanselan", tagInfo.name + ": " + field.getValueDescription());
            
    }
	 
	private static void printGPSTags(String filename) 
	{
    	try
		{
			
	
			File file = new File(filename);
			IImageMetadata metadata = Sanselan.getMetadata(file);
	        
	        if (metadata instanceof JpegImageMetadata)
	        {
	            JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
	
	
	            Log.d("Sanselan", "file: " + file.getPath());
	            for(int i=0;i< GPSTagConstants.ALL_GPS_TAGS.length;i++)
	            {
	            	printTagValue(jpegMetadata,GPSTagConstants.ALL_GPS_TAGS[i]);
	            }
	           
	        }
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		catch(ImageReadException ire)
		{
			ire.printStackTrace();
		}
	}	
	
	
	
	/** 
	 * Deletes all files in the temp folder
	 * @throws IOException
	 */
	protected static void deleteTempFiles() throws IOException
	{
		File geoStrippedDir = new File(APP_CONTEXT.getExternalFilesDir(null), TEMP_FOLDER);
		if(geoStrippedDir.exists() && geoStrippedDir.isDirectory())
		{
	        String[] files = geoStrippedDir.list();
	        for (int i = 0; i < files.length; i++) {
	        	File file = new File(geoStrippedDir, files[i]);
	        	long timeDiff = (new Date()).getTime() - file.lastModified();
	        	//if the file was modified more than 1 hour ago, delete it
	        	if((timeDiff / (60 * 60 * 1000)) > 1)
	        			file.delete();
	        }
	    }
	}
	
	

	

}
