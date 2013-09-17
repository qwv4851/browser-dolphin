/**
 * Copyright 2013 Dolphin Emulator Project
 * Licensed under GPLv2
 * Refer to the license.txt file included.
 */

package org.dolphinemu.dolphinemu.gamelist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import org.dolphinemu.dolphinemu.R;

/**
 * The adapter backing the game list.
 * <p>
 * Responsible for handling each game list item individually.
 */
public final class GameListAdapter extends ArrayAdapter<GameListItem>
{
	private final Context c;
	private final int id;
	private final List<GameListItem>items;

	public GameListAdapter(Context context, int textViewResourceId, List<GameListItem> objects)
	{
		super(context, textViewResourceId, objects);
		c = context;
		id = textViewResourceId;
		items = objects;
	}

	@Override
	public GameListItem getItem(int i)
	{
		return items.get(i);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View v = convertView;
		if (v == null)
		{
			LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(id, parent, false);
		}

		final GameListItem item = items.get(position);
		if (item != null)
		{
			TextView title    = (TextView) v.findViewById(R.id.ListItemTitle);
			TextView subtitle = (TextView) v.findViewById(R.id.ListItemSubTitle);
			ImageView icon    = (ImageView) v.findViewById(R.id.ListItemIcon);

			if (title != null)
				title.setText(item.getName());

			if (subtitle != null)
				subtitle.setText(item.getData());

			if (icon != null)
			{
				icon.setImageBitmap(item.getImage());
				icon.getLayoutParams().width = (int) ((860 / c.getResources().getDisplayMetrics().density) + 0.5);
				icon.getLayoutParams().height = (int)((340 / c.getResources().getDisplayMetrics().density) + 0.5);
			}
		}

		return v;
	}
}

