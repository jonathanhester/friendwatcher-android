package com.jonathanhester.friendwatcher;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FriendsListFragment extends ListFragment {

	ArrayAdapter<FriendStatus> friendsAdapter;
	ArrayList<FriendStatus> friendsList;
	View metaView;

	public void updateFriendData(FriendData data) {
		((TextView) metaView.findViewById(R.id.meta_name)).setText(data
				.getName());
		((TextView) metaView.findViewById(R.id.meta_started_tracking))
				.setText(data.getCreated());

		if (!data.getLastSynced().equals("null")) {
			metaView.findViewById(R.id.meta_extra).setVisibility(View.VISIBLE);
			((TextView) metaView.findViewById(R.id.meta_last_update))
					.setText(data.getLastSynced());
			((TextView) metaView.findViewById(R.id.meta_num_removed))
					.setText(data.getNumRemoved());
			((TextView) metaView.findViewById(R.id.meta_total_tracking))
					.setText(data.getTotal());
		}

		if (data.getFriendStatusList().size() > 0) {
			metaView.findViewById(R.id.activity_filler)
					.setVisibility(View.GONE);
		}

		ArrayList<FriendStatus> friends = data.getFriendStatusList();
		friendsList.clear();
		friendsList.addAll(friends);
		friendsAdapter.notifyDataSetChanged();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		metaView = getHeaderView();
		getListView().addHeaderView(metaView);

		friendsList = new ArrayList<FriendStatus>();

		friendsAdapter = new FriendsListArrayAdapter(getActivity(),
				R.layout.facebook_user, friendsList);
		setListAdapter(friendsAdapter);

	}

	private View getHeaderView() {
		View view;
		LayoutInflater inflater = (LayoutInflater) this.getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.meta_data, null);
		return view;
	}

}
