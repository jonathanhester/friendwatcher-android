package com.jonathanhester.friendwatcher;

import java.util.ArrayList;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FriendsListArrayAdapter extends ArrayAdapter<FriendStatus> {

	private ArrayList<FriendStatus> items;
	private Context context;

	public FriendsListArrayAdapter(Context context, int textViewResourceId,
			ArrayList<FriendStatus> items) {
		super(context, textViewResourceId, items);
		this.context = context;
		this.items = items;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.facebook_user, null);
		}

		FriendStatus item = items.get(position);
		if (item != null) {
			// My layout has only one TextView
			TextView nameView = (TextView) view.findViewById(R.id.name);
			nameView.setText(item.getName());
			
			TextView linkView = (TextView) view.findViewById(R.id.profile_link_text);
			linkView.setMovementMethod(LinkMovementMethod.getInstance());
			linkView.setText(Html.fromHtml(item.getProfileUrlText()));
			
			TextView eventType = (TextView) view.findViewById(R.id.event_type);
			if (item.getType() == FriendStatus.REMOVED)
				eventType.setText(context.getResources().getString(R.string.removed_text));
			else
				eventType.setText(context.getResources().getString(R.string.added_text));
			
			TextView date = (TextView) view.findViewById(R.id.date);
			date.setText(item.getDateText());
		}

		return view;
	}
}
