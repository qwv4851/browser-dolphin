package org.dolphinemu.dolphinemu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.*;
import android.view.WindowManager.LayoutParams;
import org.dolphinemu.dolphinemu.settings.InputConfigFragment;

import java.util.List;

/**
 * This is the activity where all of the emulation handling happens.
 * This activity is responsible for displaying the SurfaceView that we render to.
 */
public final class EmulationActivity extends Activity
{
	private boolean Running;
	private boolean IsActionBarHidden = false;
	private float screenWidth;
	private float screenHeight;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Retrieve screen dimensions.
		DisplayMetrics displayMetrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE); // the results will be higher than using the activity context object or the getWindowManager() shortcut
		wm.getDefaultDisplay().getMetrics(displayMetrics);
		this.screenHeight = displayMetrics.heightPixels;
		this.screenWidth = displayMetrics.widthPixels;

		// Request window features for the emulation view.
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

		// Set the transparency for the action bar.
		ColorDrawable actionBarBackground = new ColorDrawable(Color.parseColor("#303030"));
		actionBarBackground.setAlpha(175);
		getActionBar().setBackgroundDrawable(actionBarBackground);

		// Set the native rendering screen width/height.
		// Also get the intent passed from the GameList when the game
		// was selected. This is so the path of the game can be retrieved
		// and set on the native side of the code so the emulator can actually
		// load the game.
		Intent gameToEmulate = getIntent();
		NativeLibrary.SetDimensions((int)screenWidth, (int)screenHeight);
		NativeLibrary.SetFilename(gameToEmulate.getStringExtra("SelectedGame"));
		Running = true;

		// Set the emulation window.
		setContentView(R.layout.emulation_view);

		// Hide the action bar by default so it doesn't get in the way.
		getActionBar().hide();
		IsActionBarHidden = true;
	}

	@Override
	public void onStop()
	{
		super.onStop();
		if (Running)
			NativeLibrary.StopEmulation();
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if (Running)
			NativeLibrary.PauseEmulation();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if (Running)
			NativeLibrary.UnPauseEmulation();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		float X = event.getX();
		float Y = event.getY();
		int Action = event.getActionMasked();

		// Converts button locations 0 - 1 to OGL screen coords -1.0 - 1.0
		float ScreenX = ((X / screenWidth) * 2.0f) - 1.0f;
		float ScreenY = ((Y / screenHeight) * -2.0f) + 1.0f;

		NativeLibrary.onTouchEvent(Action, ScreenX, ScreenY);
		
		return false;
	}
	
	@Override
	public void onBackPressed()
	{
		// The back button in the emulation
		// window is the toggle for the action bar.
		if (IsActionBarHidden)
		{
			IsActionBarHidden = false;
			getActionBar().show();
		}
		else
		{
			IsActionBarHidden = true;
			getActionBar().hide();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.emuwindow_overlay, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int itemId, MenuItem item)
	{
		switch(item.getItemId())
		{
			// Save state slots
			case R.id.saveSlot1:
				NativeLibrary.SaveState(0);
				return true;

			case R.id.saveSlot2:
				NativeLibrary.SaveState(1);
				return true;

			case R.id.saveSlot3:
				NativeLibrary.SaveState(2);
				return true;

			case R.id.saveSlot4:
				NativeLibrary.SaveState(3);
				return true;

			case R.id.saveSlot5:
				NativeLibrary.SaveState(4);
				return true;

			// Load state slot
			case R.id.loadSlot1:
				NativeLibrary.LoadState(0);
				return true;

			case R.id.loadSlot2:
				NativeLibrary.LoadState(1);
				return true;

			case R.id.loadSlot3:
				NativeLibrary.LoadState(2);
				return true;

			case R.id.loadSlot4:
				NativeLibrary.LoadState(3);
				return true;

			case R.id.loadSlot5:
				NativeLibrary.LoadState(4);
				return true;

			default:
				return super.onMenuItemSelected(itemId, item);
		}
	}

	// Gets button presses
	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		int action = 0;

		// Special catch for the back key
		// Currently disabled because stopping and starting emulation is broken.
		/*
		if (event.getSource() == InputDevice.SOURCE_KEYBOARD
			&& event.getKeyCode() == KeyEvent.KEYCODE_BACK
			&& event.getAction() == KeyEvent.ACTION_UP)
		{
			if (Running)
				NativeLibrary.StopEmulation();
			Running = false;
			Intent ListIntent = new Intent(this, GameListActivity.class);
			startActivityForResult(ListIntent, 1);
			return true;
		}
		*/

		if (Running)
		{
			switch (event.getAction())
			{
				case KeyEvent.ACTION_DOWN:
					// Handling the case where the back button is pressed.
					if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
					{
						onBackPressed();
						return true;
					}

					// Normal key events.
					action = 0;
					break;
				case KeyEvent.ACTION_UP:
					action = 1;
					break;
				default:
					return false;
			}
			InputDevice input = event.getDevice();
			NativeLibrary.onGamePadEvent(InputConfigFragment.getInputDesc(input), event.getKeyCode(), action);
			return true;
		}
		return false;
	}

	@Override
	public boolean dispatchGenericMotionEvent(MotionEvent event)
	{
		if (((event.getSource() & InputDevice.SOURCE_CLASS_JOYSTICK) == 0) || !Running)
		{
			return super.dispatchGenericMotionEvent(event);
		}

		InputDevice input = event.getDevice();
		List<InputDevice.MotionRange> motions = input.getMotionRanges();

		for (InputDevice.MotionRange range : motions)
		{
			NativeLibrary.onGamePadMoveEvent(InputConfigFragment.getInputDesc(input), range.getAxis(), event.getAxisValue(range.getAxis()));
		}

		return true;
	}
}
