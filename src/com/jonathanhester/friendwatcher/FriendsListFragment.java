package com.jonathanhester.friendwatcher;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.widget.ArrayAdapter;

public class FriendsListFragment extends ListFragment {

	ArrayAdapter<FriendStatus> friendsAdapter;
	ArrayList<FriendStatus> friendsList;

	public void updateFriends(ArrayList<FriendStatus> friends) {
		friendsList.addAll(friends);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		friendsList = new ArrayList<FriendStatus>();
		friendsAdapter = new FriendsListArrayAdapter(getActivity(),
				R.layout.facebook_user, friendsList);
		setListAdapter(friendsAdapter);
	}

}
