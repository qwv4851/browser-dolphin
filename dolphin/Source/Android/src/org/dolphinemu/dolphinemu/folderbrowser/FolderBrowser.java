/**
 * Copyright 2013 Dolphin Emulator Project
 * Licensed under GPLv2
 * Refer to the license.txt file included.
 */

package org.dolphinemu.dolphinemu.folderbrowser;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.util.*;

import org.dolphinemu.dolphinemu.NativeLibrary;
import org.dolphinemu.dolphinemu.R;
import org.dolphinemu.dolphinemu.gamelist.GameListActivity;

/**
 * A basic folder browser {@link Fragment} that allows
 * the user to select ISOs/ROMs for playing within the
 * emulator.
 * <p>
 * Any valid ISO/ROM selected in this will be added to
 * the game list for easy browsing the next time the
 * application is used.
 * <p>
 * Note that this file browser does not display files 
 * or directories that are hidden
 */
public final class FolderBrowser extends Fragment
{
	private Activity m_activity;
	private FolderBrowserAdapter adapter;
	private ListView mDrawerList;
	private View rootView;
	private static File currentDir = null;

	// Populates the FolderView with the given currDir's contents.
	private void Fill(File currDir)
	{
		m_activity.setTitle(getString(R.string.current_dir) + currDir.getName());
		File[] dirs = currDir.listFiles();
		List<FolderBrowserItem>dir = new ArrayList<FolderBrowserItem>();
		List<FolderBrowserItem>fls = new ArrayList<FolderBrowserItem>();

		// Supported extensions to filter by
		Set<String> validExts = new HashSet<String>(Arrays.asList(".dff", ".dol", ".elf", ".gcm", ".gcz", ".iso", ".wad", ".wbfs"));

		// Search for any directories or files within the current dir.
		for(File entry : dirs)
		{
			try
			{
				String entryName = entry.getName();
				boolean hasExtension = (entryName.lastIndexOf(".") != -1);

				// Skip hidden folders/files.
				if (!entry.isHidden())
				{
					if(entry.isDirectory())
					{
						dir.add(new FolderBrowserItem(entryName, entry.getAbsolutePath()));
					}
					else if (entry.isFile() && hasExtension)
					{
						if (validExts.contains(entryName.toLowerCase().substring(entryName.lastIndexOf('.'))))
						{
							fls.add(new FolderBrowserItem(entryName, getString(R.string.file_size)+entry.length(), entry.getAbsolutePath()));
						}
					}
				}
			}
			catch (Exception ex)
			{
				Log.e("FolderBrowser", ex.toString());
			}
		}

		Collections.sort(dir);
		Collections.sort(fls);
		dir.addAll(fls);

		// Check for a parent directory to the one we're currently in.
		if (!currDir.getPath().equalsIgnoreCase("/"))
			dir.add(0, new FolderBrowserItem("..", getString(R.string.parent_directory), currDir.getParent()));

		adapter = new FolderBrowserAdapter(m_activity, R.layout.gamelist_folderbrowser_list, dir);
		mDrawerList = (ListView) rootView.findViewById(R.id.gamelist);
		mDrawerList.setAdapter(adapter);
		mDrawerList.setOnItemClickListener(mMenuItemClickListener);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if(currentDir == null)
			currentDir = new File(Environment.getExternalStorageDirectory().getPath());

		rootView = inflater.inflate(R.layout.gamelist_listview, container, false);

		Fill(currentDir);
		return mDrawerList;
	}

	private final AdapterView.OnItemClickListener mMenuItemClickListener = new AdapterView.OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			FolderBrowserItem item = adapter.getItem(position);
			if(item.isDirectory())
			{
				currentDir = new File(item.getPath());
				Fill(currentDir);
			}
			else
			{
				FolderSelected();
			}
		}
	};

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);

		// Cache the activity instance.
		m_activity = activity;
	}


	private void FolderSelected()
	{
		String Directories = NativeLibrary.GetConfig("Dolphin.ini", "General", "GCMPathes", "0");
		int intDirectories = Integer.parseInt(Directories);

		// Check to see if a path set in the Dolphin config
		// matches the one the user is trying to add. If it's
		// already set, then don't add it to the list again.
		boolean pathNotPresent = true;
		for (int i = 0; i < intDirectories; i++)
		{
			String gcmPath = NativeLibrary.GetConfig("Dolphin.ini", "General", "GCMPath" + i, "");

			if (gcmPath.equals(currentDir.getPath()))
			{
				pathNotPresent = false;
			}
			else
			{
				pathNotPresent = true;
			}
		}

		// User doesn't have this path in the config, so add it.
		if (pathNotPresent)
		{
			NativeLibrary.SetConfig("Dolphin.ini", "General", "GCMPathes", Integer.toString(intDirectories+1));
			NativeLibrary.SetConfig("Dolphin.ini", "General", "GCMPath" + Integer.toString(intDirectories), currentDir.getPath());
		}

		((GameListActivity)m_activity).SwitchPage(0);
	}
}
