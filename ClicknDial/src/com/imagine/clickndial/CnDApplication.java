package com.imagine.clickndial;

import android.app.Application;

public class CnDApplication extends Application {

	@Override
	public void onCreate() {
		 Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
		    {
		      @Override
		      public void uncaughtException (Thread thread, Throwable e)
		      {
		        handleUncaughtException (thread, e);
		      }
		    });
		 
		super.onCreate();
	}
	
	
	
	public void handleUncaughtException (Thread thread, Throwable e) {
		System.exit(1);
		//Toast.makeText(CnDApplication.this, "Something Extremely  goes wrong.\nPlease try again.", Toast.LENGTH_LONG).show();
	}
	  
}
