package com.example.betarun;

import java.util.HashMap;
import java.util.Iterator;

import com.example.betarun.audio.AudioOnAir;
import com.example.betarun.openGL.MyGLSurfaceView;
import com.example.betarun.settings.SettingsFragment;
import com.example.betarun.settings.SettingsActivity;
import com.example.betarun.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Camera;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.opengl.EGLContext;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.TextureView;
import android.widget.Button;
import android.widget.TextView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class FullscreenActivity extends Activity implements TextureView.SurfaceTextureListener {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static boolean AUTO_HIDE = true;
	
	public final String TAG = "com.example.betarun";

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will hide the system UI visibility upon interaction.
	 */
	private static boolean TOGGLE_ON_CLICK = true;
	
	/**
	 * If set, will toggle the OnAir/OffAir background.,
	 * will also turn on and off main play back functionality.
	 */
	private static boolean TOGGLE_ONAIR_CLICK = false;


	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;
	
	/**
	 * The instance of the {@link AudioOnAir} for this activity.
	 */
	private AudioOnAir mAudioOnAir;
	
	
	/**
	 * The instance of the {@link GLSurfaceView} for this activity.
	 */
	public MyGLSurfaceView mGLView;
	
	/**
	 * The instance of the {@link OpenGLTextureViewSample} for this activity.
	 */
	public OpenGLTextureViewSample mGLTextureView;
	
	/**
	 * The instance of the {@link Camera} for this activity.
	 */
	private Camera mCamera;
	
	/**
	 * The instance of the activity bar menu
	 */
	private Menu mMenu;

	/**
	 * The instance of the activity bar options MenuItem
	 */
	private MenuItem mMenuItem;
	
	/**
	 * The instance of the Settings Menu
	 */
	private Menu mSettingsMenu;
	
	/**
	 * The instance of the items in the Settings Menu
	 */
	private MenuItem[] mSettingsMenuItems;
		
	private SettingsFragment mSettingsFragment;
	
	/**
	 * The instance of the {@link UsbManager} for this activity.
	 */
	public UsbManager mUsbManager;
	
	
	private PendingIntent mPermissionIntent;
	
	/**
	 * The instance of the {@link UsbManager} for this activity.
	 */
	private static final String ACTION_USB_PERMISSION =
		    "com.example.betarun.USB_PERMISSION";
		
	/**
	 * The instance of the renderscript particleFilter used in MyGLRenderer
	 */
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_fullscreen);

		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.fullscreen_content);
		

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider.setOnVisibilityChangeListener(new 
				SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		
		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
					
				} else {
					mSystemUiHider.hide();
					
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.dummy_button).setOnTouchListener(
				mDelayHideTouchListener);
		findViewById(R.id.dummy_button).setOnClickListener(
				mClickListener);
		
		
		// Create instance on AudioOnAir
		mAudioOnAir = new AudioOnAir((Button) findViewById(R.id.dummy_button), (TextView) findViewById(R.id.fullscreen_content));
		mGLView = new MyGLSurfaceView(this,mAudioOnAir.NoteSpectrum);
		mGLTextureView = new OpenGLTextureViewSample(this);
		mGLTextureView.setSurfaceTextureListener(this);
		
        /*/ Display the fragment as the main content.
		mSettingsFragment = new SettingsFragment();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, mSettingsFragment)
                .commit();//*/	
		
		// get list of USB connected devices.
		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		registerReceiver(mUsbReceiver, filter);
		detectUSB();
}
	
	
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.xml.menu, menu);
	    mMenuItem = menu.findItem(R.id.options_menu_item);
	    mMenuItem.setOnActionExpandListener(mOptionsExpandListener);
	    mSettingsMenu = mMenuItem.getSubMenu();
	    //mMenuItem.setOnMenuItemClickListener(mMenuItemClickedListener);
	    //nflater.mSettingsMenuItems = new MenuItem[mSettingsMenu.size()];
	    //for (int i = 0; i<menu.size(); i++){
	    //	mSettingsMenuItems[i] = mSettingsMenu.getItem(i);
	    //	mSettingsMenuItems[i].setOnMenuItemClickListener(mMenuItemClickedListener);
	    //}
	    
	    
	    mMenu = menu;
	    return true;
	}
	
	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
				
			} 
			return false;
		}
	};
	
	View.OnClickListener mClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			TOGGLE_ONAIR_CLICK = !TOGGLE_ONAIR_CLICK;
			if (TOGGLE_ONAIR_CLICK) {
				//intend to start the audio 
				Intent intent = new Intent(view.getContext(), AudioOnAir.class);				
				mAudioOnAir.Toggle(mGLView);
				//findViewById(R.id.fullscreen_content);
				//setContentView(R.layout.activity_onair);
				setContentView(mGLView);
				//setContentView(mGLTextureView);
				//mGLView.builder.show();
			}else setContentView(R.layout.activity_fullscreen);
			
		}
	};
	
	public void OpenDevicePreferences(View view){
		// Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();	
	}

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
	
	public boolean onOptionsItemSelected(MenuItem item){
		   
		mHideHandler.removeCallbacks(mHideRunnable);
		if (item.getItemId() == R.id.options_menu_item) {
			Intent intent = new Intent(this, SettingsActivity.class);	
			startActivity(intent);
		}
		
		return false;
	}
	
	/**
	 * Remove the UI's hide routine when the options menu is expanded
	 * and hide UI when options menu is collapsed.
	 */
	private MenuItem.OnActionExpandListener mOptionsExpandListener = new MenuItem.OnActionExpandListener() {
		
		@Override
		public boolean onMenuItemActionExpand(MenuItem item) {
			mHideHandler.removeCallbacks(mHideRunnable);
			return false;
		}
		
		@Override
		public boolean onMenuItemActionCollapse(MenuItem item) {
			delayedHide(AUTO_HIDE_DELAY_MILLIS);
			return false;
		}
	};

	
	/**
	 * Open the Preference fragment associated with the item when 
	 * the item is selected.
	 */
	/*private MenuItem.OnMenuItemClickListener mMenuItemClickedListener = new MenuItem.OnMenuItemClickListener() {

		@Override
		public boolean onMenuItemClick(MenuItem item) {
			//mSystemUiHider.hide();
			//mSystemUiHider.disable();
			if (item.getItemId() == R.id.devices_menu_item) {
				
				TOGGLE_ON_CLICK = false; //turn off the UI while in settings.
				mSettingsFragment.addPreferencesFromResource(R.xml.devices);
			} else if (item.getItemId() == R.id.visualizations_menu_item) {
				
			} else if (item.getItemId() == R.id.addons_menu_item) {
				
			}
			return false;
		}
		

	};//*/

	
	
	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
			int height) {
        /*mCamera = Camera.open();

        try {
        	mGLView.getMyGLSurfaceView(this).;
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
        } catch (IOException ioe) {
            // Something bad happened
        }*/
		//mGLTextureView.onSurfaceTextureAvailable(surface, width, height);
	}



	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Get a list of all the audio device connected via USB hub.
	 */
	private void detectUSB() {
		HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		while(deviceIterator.hasNext()){
		    UsbDevice device = deviceIterator.next();
		    mUsbManager.requestPermission(device, mPermissionIntent); // request permission to speak to device
		    if (device.getDeviceClass()==UsbConstants.USB_CLASS_AUDIO) { //check if USB device is an audio device
		    	for (int i = 0; i < device.getInterfaceCount(); i++){
		    		UsbInterface audioInterface = device.getInterface(i);
		    		for (int j = 0; j < audioInterface.getEndpointCount(); j++){
		    			UsbEndpoint audioEndpoint = audioInterface.getEndpoint(j);
		    			String description = audioEndpoint.toString();
		    			
		    			if (audioEndpoint.getDirection()==UsbConstants.USB_DIR_IN){ // Is an audio input device ...
		    				// add input audio device to arraylist
		    				//String[] inputDeviceArray = getResources().getStringArray(R.array.input_devices_entries);
		    				//inputDeviceArray = description;
		    				//getStringArray(R.array.input_devices_keys).addItem("Inteface=" + i + ",EndPoint=" + j);
		    				Log.d("USBInputDevice", "Inteface=" + i + ",EndPoint=" + j + 
		    						"Description=" + description);
		    			}else if (audioEndpoint.getDirection()==UsbConstants.USB_DIR_OUT){ // Is an audio output device ... 
		    				// add output audio device to arraylist
		    				//getStringArray(R.array.output_devices_entries).addItem(description);
		    				//getStringArray(R.array.output_devices_keys).addItem("Inteface=" + i + ",EndPoint=" + j);
		    				Log.d("USBOutputDevice", "Inteface=" + i + ",EndPoint=" + j + 
		    						"Description=" + description);
		    			}
		    		}
		    	}
		    }
		}
	}
	
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
		    String action = intent.getAction();
		    if (ACTION_USB_PERMISSION.equals(action)) {
		        synchronized (this) {
		            UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

		            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
		                if(device != null){
		                	//call method to set up device communication
		                	detectUSB();
		                }
		            } else {
		            	Log.d(TAG, "permission denied for device " + device);
		            }
		        }
		    }
		}
	};
	
}


