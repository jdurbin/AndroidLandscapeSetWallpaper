/*
* Copyright (C) 2011 K. James Durbin
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package durbin.streak;


import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import android.net.Uri;
import android.app.WallpaperManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;

import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;
import android.text.InputType;
//import android.widget.NumberPicker;  only available in later android?
import android.widget.EditText;


/***
* Android, or maybe just the Streak, doesn't handle landscape wallpapers the way I think
* it should.  No matter what you do, it crops off the top and bottom of the image.  I generally
* want to see the whole image, scaled to fit the display.  Since the cropping seems to happen
* in the guts of Android, out of my reach (AFAIK, with my limited knowledge), I am taking the
* tact of expanding the image by a set amount on top and bottom. 
* 
*/ 
public class StreakSetWallpaper extends Activity {

	static final private int BACK_ID = Menu.FIRST;
	static final private int CLEAR_ID = Menu.FIRST + 1;
	
	ImageView imageView;
	Drawable wallpaperDrawable;
	
	int suggestH = 1200;
	int suggestW = 480;
	
	EditText nph;
	EditText npw;
	
	private EditText mEditor;

	public StreakSetWallpaper() {
	}

	final static private int[] mColors =
	{Color.BLUE, Color.GREEN, Color.RED, Color.LTGRAY, Color.MAGENTA, Color.CYAN,
		Color.YELLOW, Color.WHITE};

	static final int ACTIVITY_SELECT_IMAGE = 1234;

	private Uri getTempUri() {
	 return Uri.fromFile(getTempFile());
	}
	
	private File getTempFile(){
		File f = new File(Environment.getExternalStorageDirectory(),"tempphoto.jpeg");
	  try {
			f.createNewFile();
	  } catch (IOException e) {}
		return(f);
	}

	/**
	* Initialization of the Activity after it is first created.  Must at least
	* call {@link android.app.Activity#setContentView setContentView()} to
	* describe what is to be displayed in the screen.
	*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Be sure to call the super class.
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wallpaper_2);
		final WallpaperManager wpm = WallpaperManager.getInstance(this);
		
		System.err.println("minHeight:"+wpm.getDesiredMinimumHeight());
		System.err.println("minWidth:"+wpm.getDesiredMinimumWidth());
		
		wallpaperDrawable = wpm.getDrawable();
		imageView = (ImageView) findViewById(R.id.imageview);
		imageView.setDrawingCacheEnabled(true);
		imageView.setImageDrawable(wallpaperDrawable);

		// Setup select button
		Button selectWallpaper = (Button) findViewById(R.id.selectWallpaper);
		selectWallpaper.setOnClickListener(new OnClickListener(){
			public void onClick(View view){
				Intent i = new Intent(Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
					
				i.putExtra("crop", "true");	
				i.putExtra("return-data",true);	
				
				//i.putExtra("outputX", 1200);
				//i.putExtra("outputY", 480);
				//i.putExtra("setWallpaper", true);  // This doesn't seem to do anything.
				
				i.putExtra("aspectX", 5);
			  i.putExtra("aspectY", 2);
			  i.putExtra("scale", true);  // I seem to get fewer FAILED BINDER TRANSACTIONS with this in.
																					
				startActivityForResult(i, ACTIVITY_SELECT_IMAGE);
			}
		});

		// Setup randomize button
		//Button randomize = (Button) findViewById(R.id.randomize);
		//randomize.setOnClickListener(new OnClickListener() {
		//	public void onClick(View view) {
		//		int mColor = (int) Math.floor(Math.random() * mColors.length);
		//		wallpaperDrawable.setColorFilter(mColors[mColor], PorterDuff.Mode.MULTIPLY);
		//		imageView.setImageDrawable(wallpaperDrawable);
		//		imageView.invalidate();
		//	}
		//});
		
		// Setup number fields...
		LinearLayout linearLayout = (LinearLayout)findViewById(R.id.linearlayout);
		nph = new EditText(this);
		nph.setMinWidth(100);		
		nph.setInputType(InputType.TYPE_CLASS_NUMBER);		
		String nphStr;
		nphStr = String.format("%d",suggestH);
		nph.setText(nphStr);
		npw = new EditText(this);
		npw.setMinWidth(100);		
		npw.setInputType(InputType.TYPE_CLASS_NUMBER);		
		String npwStr;
		npwStr = String.format("%d",suggestW);
		npw.setText(npwStr);		
		linearLayout.addView(nph);
		linearLayout.addView(npw);
			

		// Setup set wallpaper button
		Button setWallpaper = (Button) findViewById(R.id.setwallpaper);
		setWallpaper.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					try {
						
						
						String suggestHTxt = nph.getText().toString();
						String suggestWTxt = npw.getText().toString();
						
						suggestH = Integer.parseInt(suggestHTxt);
						suggestW = Integer.parseInt(suggestWTxt);
						
						//public void setWallpaperOffsets (IBinder windowToken, float xOffset, float yOffset)
						//wpm.setWallpaperOffsets(imageView.getWindowToken(),400,400);
						wpm.suggestDesiredDimensions(suggestH,suggestW);

						System.err.println("AFTER minWidth:"+wpm.getDesiredMinimumWidth());						
						System.err.println("AFTER minHeight:"+wpm.getDesiredMinimumHeight());
						
						wpm.setBitmap(imageView.getDrawingCache());																		
						finish();
					
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
		});
	}
	
	/***
	* Event that indicates that some activity we spawned has returned it's result, 
	* in this case, hopefully, and image...
	*/ 
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == ACTIVITY_SELECT_IMAGE) {
				try{
										
					final Bundle extras = data.getExtras();
					if (extras != null) {
						Bitmap wallpaperBmp = extras.getParcelable("data");
						wallpaperDrawable = new BitmapDrawable(wallpaperBmp);
					}else{
						System.err.println("extras == null");
					}

					int height = wallpaperDrawable.getIntrinsicHeight();
					int width = wallpaperDrawable.getIntrinsicWidth();
					String infoStr;														
					infoStr = String.format("Width: %d  Height: %d",width,height);
					
					// Display some information about it...
					LinearLayout linearLayout = (LinearLayout)findViewById(R.id.linearlayout);
					TextView tv = new TextView(this);
					tv.setText(infoStr);
					System.err.println(infoStr);
					linearLayout.addView(tv);
					
									
					imageView.setDrawingCacheEnabled(true);
					imageView.setImageDrawable(wallpaperDrawable);	
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
}