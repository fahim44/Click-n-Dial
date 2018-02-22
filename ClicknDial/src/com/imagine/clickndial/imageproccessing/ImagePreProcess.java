package com.imagine.clickndial.imageproccessing;

import android.graphics.Bitmap;
import android.graphics.Matrix;


public class ImagePreProcess {
	
	public Bitmap bitmap;
	
	//public static final String DATA_PATH = Environment
	//		.getExternalStorageDirectory().toString() + "/ClicknDial/";
	
	public ImagePreProcess(Bitmap bt) {
		bitmap = bt.copy(bt.getConfig(), true);
		
		resizeImage(600, 600);
		bitmap = Bitmap.createBitmap(bitmap, 215, 0, bitmap.getWidth()-520, bitmap.getHeight());
		
		Matrix matrix = new Matrix();

		matrix.postRotate(90);
		bitmap = Bitmap.createBitmap(bitmap,0, 0, bitmap .getWidth(), bitmap .getHeight(), matrix, true);
		
		
		//File file = new File(DATA_PATH, "cropped.jpg");
		//if (file.exists())
			//file.delete();
		//try {
		//	FileOutputStream out = new FileOutputStream(file);
		//	bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
		//	out.flush();
		//	out.close();
		//} catch (Exception e) {
		//	e.printStackTrace();
		//}
	}
	
	private void resizeImage(int IMG_WIDTH, int IMG_HEIGHT) {
		if(bitmap.getHeight() > bitmap.getWidth()){
			int temp = IMG_WIDTH;
    		IMG_WIDTH = IMG_HEIGHT;
    		IMG_HEIGHT = temp;
		}
	}

}
