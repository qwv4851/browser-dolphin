/**
 * Copyright 2013 Dolphin Emulator Project
 * Licensed under GPLv2
 * Refer to the license.txt file included.
 */

package org.dolphinemu.dolphinemu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * The surface that rendering is done to.
 */
public final class NativeGLSurfaceView extends SurfaceView
{
	private static Thread myRun;
	private static boolean Running = false;
	private static boolean Created = false;

	/**
	 * Constructor.
	 * 
	 * @param context The current {@link Context}.
	 */
	public NativeGLSurfaceView(Context context, AttributeSet attribs)
	{
		super(context, attribs);

		if (!Created)
		{
			myRun = new Thread() 
			{
				@Override
				public void run() {
					NativeLibrary.Run(getHolder().getSurface());
				}
			};

			getHolder().addCallback(new SurfaceHolder.Callback()
			{
					public void surfaceCreated(SurfaceHolder holder)
					{
						// TODO Auto-generated method stub
						if (!Running)
						{
							myRun.start();
							Running = true;
						}
					}

					public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
					{
						// TODO Auto-generated method stub
					}

					public void surfaceDestroyed(SurfaceHolder arg0)
					{
						// TODO Auto-generated method stub
					}
			 });

			Created = true;
		}
	}
}
