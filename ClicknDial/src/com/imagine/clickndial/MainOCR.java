package com.imagine.clickndial;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.imagine.clickndial.camera.MainCamera;
import com.imagine.clickndial.imageproccessing.ImagePreProcess;
import com.imagine.clickndial.imageproccessing.ImageProcess;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;



public class MainOCR extends Activity {
	public static final String PACKAGE_NAME = "com.imagine.clickndial";
	public static final String DATA_PATH = Environment
			.getExternalStorageDirectory().toString() + "/ClicknDial/";
	
	public static final String lang = "eng";
	protected String _path;
	protected boolean _taken;
	
	protected static final String PHOTO_TAKEN = "photo_taken";
	
	Bitmap bitmap;
	
	int Red, Green, Blue;
	
	public static boolean is_opning_mainocr_1st_time;
	private boolean is_calling;
	private int op_num;
	
	private ProgressDialog pd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };
		
		for (String path : paths) {
			File dir = new File(path);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					return;
				} else {
				}
			}
		}
		
		if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
			try {

				AssetManager assetManager = getAssets();
				InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
				//GZIPInputStream gin = new GZIPInputStream(in);
				OutputStream out = new FileOutputStream(DATA_PATH
						+ "tessdata/" + lang + ".traineddata");

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				//while ((lenf = gin.read(buff)) > 0) {
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				//gin.close();
				out.close();
			} catch (IOException e) {
			}
		}
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainocr);
		
		pd = new ProgressDialog(this);
		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pd.setMessage("Processing...");
		pd.setIndeterminate(true);
		pd.setCancelable(false);

		_path = DATA_PATH + "/ocr.jpg";
		
		if(is_opning_mainocr_1st_time == true){
			startCameraActivity();
			is_opning_mainocr_1st_time = false;
		}
		is_calling = getIntent().getExtras().getBoolean("isCalling");
		
		if(is_calling == false)
			op_num = getIntent().getExtras().getInt("opNum");
	}
	
	
	
	protected void startCameraActivity() {
		//File file = new File(_path);
		//Uri outputFileUri = Uri.fromFile(file);

		//final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		//intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		

		Intent intent = new Intent(MainOCR.this, MainCamera.class);
		startActivityForResult(intent, 0);
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == -1) {
			onPhotoTaken();
		} else {
			
			recognizing_error();
			//finish();
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(MainOCR.PHOTO_TAKEN,_taken);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState.getBoolean(MainOCR.PHOTO_TAKEN)){
			onPhotoTaken();
		}
		else
			recognizing_error();
	}
	
	

//////////////////image processing & tessaract handling////////////////////////// 	
	protected void onPhotoTaken() {
		_taken = true;

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;

		bitmap = BitmapFactory.decodeFile(_path, options);
		
		
		try {
				ImagePreProcess ipp = new ImagePreProcess(bitmap.copy(bitmap.getConfig(), true));
				bitmap = ipp.bitmap.copy(ipp.bitmap.getConfig(), true);
			
			//image_process();
				ImageProcess ip = new ImageProcess(bitmap.copy(bitmap.getConfig(), true));
				bitmap = ip.bitmap.copy(ip.bitmap.getConfig(), true);
			
		} catch (Exception e1) {
			recognizing_error();
		}

		try {
			ExifInterface exif = new ExifInterface(_path);
			int exifOrientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);

			int rotate = 0;

			switch (exifOrientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			}

			if (rotate != 0) {

				// Getting width & height of the given image.
				int w = bitmap.getWidth();
				int h = bitmap.getHeight();

				// Setting pre rotate
				Matrix mtx = new Matrix();
				mtx.preRotate(rotate);

				// Rotating Bitmap
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
			}

			// Convert to ARGB_8888, required by tess
			bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

		} catch (IOException e) {
			recognizing_error();
		}

//////////////////////		
		try {
			if(bitmap!=null){
			/*	File file = new File(DATA_PATH, "final.jpg");
				if (file.exists())
					file.delete();
				try {
					FileOutputStream out = new FileOutputStream(file);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
					out.flush();
					out.close();
				} catch (Exception e) {
				}*/
				//bitmap.setDensity(500);
				//Toast.makeText(MainOCR.this, Integer.toString(bitmap.getDensity()),	 Toast.LENGTH_LONG).show();
				
			/*
				TessBaseAPI baseApi = new TessBaseAPI();
				baseApi.setDebug(true);
				baseApi.init(DATA_PATH, lang);
				baseApi.setImage(bitmap);
			
				String whiteList = "1234567890";
				baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, whiteList);
				
				String blackList = "sSBb?oOlLjJQ";
				baseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, blackList);
			
				String recognizedText = baseApi.getUTF8Text();
			
				baseApi.end();
				
				
			
			// You now have the text in recognizedText var, you can do anything with it.
			// We will display a stripped out trimmed alpha-numeric version of it (if lang is eng)
			// so that garbage doesn't make it to the display.

				
				if ( lang.equalsIgnoreCase("eng") ) {
					recognizedText = recognizedText.replaceAll("[^0-9]+", "");
				}
				
				recognizedText = recognizedText.trim();

				if ( recognizedText.length() != 0 ) {
					if(is_calling==true)
						calling(recognizedText);
					else
						recharging(recognizedText);
////////////////////////					
					try {
			            File myFile = new File(DATA_PATH, "text.txt");
			            if(myFile.exists())
			            	myFile.delete();
			            myFile.createNewFile();
			            FileOutputStream fOut = new FileOutputStream(myFile);
			            OutputStreamWriter myOutWriter = 
			                                    new OutputStreamWriter(fOut);
			            myOutWriter.append(recognizedText);
			            myOutWriter.close();
			            fOut.close();
			            
			        } catch (Exception e) {
			        }
				
				
				}
				
				
				else {
					recognizing_error();
				}
			
				*/
				
				pd.show();
				new ReadOcrTask().execute();
			}
			else{
				recognizing_error();
			}
		} catch (Exception e) {
			recognizing_error();
		}
		
		
		// Cycle done.
		//finish();
	}
	
	
///////////////////making call/////////////////////////////////////	
	private void calling(final String num_str){
		
			AlertDialog.Builder alert_builder = new AlertDialog.Builder(MainOCR.this);
			alert_builder.setMessage("Find "+ num_str + " \nMake call?");
			alert_builder.setCancelable(false);
			alert_builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					Intent intent = new Intent(Intent.ACTION_CALL);
					intent.setData(Uri.parse("tel:"+ num_str));
					startActivity(intent);
					finish();
				}
			});
			
			alert_builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					finish();	
				}
			});
			AlertDialog alertDialog = alert_builder.create();
			alertDialog.show();	
	}
	
	
	
///////////////////making call/////////////////////////////////////	
	private void recharging(String st){
	
		final String num_str;
		
		if(st.length() < 16){
			recognizing_error();
			return;
		}
		else if(st.length() > 16){
			st = st.substring(st.length()-16);
		}
		
		num_str = st;
		
		AlertDialog.Builder alert_builder = new AlertDialog.Builder(MainOCR.this);
		alert_builder.setMessage("Find "+ num_str + " \nMake call?");
		alert_builder.setCancelable(false);
		alert_builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				String front_str = null;
				if(op_num == 0)
					front_str = "*555*";
				else if(op_num==1)
					front_str = "*123*";
				else if(op_num==2)
					front_str = "*787*";
				else if(op_num==3)
					front_str = "*222*";
				else if(op_num==4)
					front_str = "*151*";
				
				Intent intent = new Intent(Intent.ACTION_CALL);
				intent.setData(Uri.parse("tel:"+ front_str + num_str + Uri.encode("#")));
				startActivity(intent);
				finish();
			}
		});
		
		alert_builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				finish();	
			}
		});
		AlertDialog alertDialog = alert_builder.create();
		alertDialog.show();	
	}	
	
//////////////////////////Retiring photo taking/////////////////////
	private void recognizing_error() {
		pd.cancel();
		
		AlertDialog.Builder alert_builder = new AlertDialog.Builder(MainOCR.this);
		alert_builder.setMessage("Something goes wrong.\nLets try again");
		alert_builder.setCancelable(false);
		alert_builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				Intent intent = new Intent(MainOCR.this, MainCamera.class);
				startActivityForResult(intent, 0);
				//finish();
			}
		});
		
		alert_builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				finish();	
			}
		});
		AlertDialog alertDialog = alert_builder.create();
		alertDialog.show();
	}
//////////////////////////after threading////////////////////////
	private void onReturningFromTask(String recognizedText) {
		pd.cancel();
		
		if ( lang.equalsIgnoreCase("eng") ) {
			recognizedText = recognizedText.replaceAll("[^0-9+]+", "");
		}
		
		recognizedText = recognizedText.trim();

		if ( recognizedText.length() != 0 ) {
			if(is_calling==true)
				calling(recognizedText);
			else
				recharging(recognizedText);
////////////////////////			
		/*	try {
	            File myFile = new File(DATA_PATH, "text.txt");
	            if(myFile.exists())
	            	myFile.delete();
	            myFile.createNewFile();
	            FileOutputStream fOut = new FileOutputStream(myFile);
	            OutputStreamWriter myOutWriter = 
	                                    new OutputStreamWriter(fOut);
	            myOutWriter.append(recognizedText);
	            myOutWriter.close();
	            fOut.close();
	            
	        } catch (Exception e) {
	        }*/
		}	
		else {
			recognizing_error();
		}
		
		File file = new File(DATA_PATH, "ocr.jpg");
		if (file.exists())
			file.delete();
	}
	
	
////////////////////////////threading///////////////////////////////
	private class ReadOcrTask extends AsyncTask<Void, Void, String>{

		TessBaseAPI baseApi=new TessBaseAPI();
		@Override
		protected String doInBackground(Void... params) {
			String recognizedText = null;
			try {
				
				baseApi.init(DATA_PATH, lang);
				baseApi.setDebug(true);
				baseApi.setImage(bitmap);
				
				String whiteList = "1234567890+";
				baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, whiteList);
				
				String blackList = "sSBb?oOlLjJQ";
				baseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, blackList);

				recognizedText = baseApi.getUTF8Text();
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return recognizedText;
		}
		
		@Override
		protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
			baseApi.end();
			onReturningFromTask(result);
			super.onPostExecute(result);
		}
		
	}
	
}
