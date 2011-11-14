package com.jonathanhester.friendwatcher;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FacebookUserListAdapter extends ArrayAdapter<FacebookUser> {
	private ArrayList<FacebookUser> items;

	public FacebookUserListAdapter(Context context, int textViewResourceId,
			ArrayList<FacebookUser> items) {
		super(context, textViewResourceId, items);
		this.items = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.facebook_user, null);
		}
		FacebookUser facebookUser = items.get(position);
		if (facebookUser != null) {
			TextView name = (TextView) v.findViewById(R.id.name);
			name.setText(facebookUser.getName());
		}

		return v;
	}

	public FacebookUser getResortFromPosition(int position) {
		return items.get(position);
	}
}
