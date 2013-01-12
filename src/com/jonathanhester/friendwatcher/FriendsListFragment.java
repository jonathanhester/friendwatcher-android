package com.jonathanhester.friendwatcher;

import java.util.ArrayList;

import com.jonathanhester.friendwatcher.requests.FriendWatcherRequest;
import com.jonathanhester.friendwatcher.requests.MyRequestFactory;
import com.jonathanhester.requestFactory.Receiver;
import com.jonathanhester.requestFactory.ServerFailure;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FriendsListFragment extends ListFragment {

	ArrayAdapter<FriendStatus> friendsAdapter;
	ArrayList<FriendStatus> friendsList;
	View metaView;
	
	private boolean loadOnStartup = false;

	DataStore dataStore;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dataStore = new DataStore(getActivity());
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (loadOnStartup)
			showUnfriended();
	}

	private void updateFriendData(FriendData data) {
		if (data.getLastSynced() == null) return;
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

	public void showUnfriended() {
		showCachedData();
		if (!isAdded())
			return;
		
		if (dataStore.getListValid())
			return;
		final FriendWatcherRequest request = MyRequestFactory
				.friendWatcherRequest(getActivity());
		Toast.makeText(getActivity(), "Refreshing data...", Toast.LENGTH_SHORT)
				.show();

		request.fetchFriends(dataStore.getFbid(), dataStore.getToken()).fire(
				new Receiver<String>() {
					@Override
					public void onFailure(ServerFailure failure) {
						Tracker.getInstance().requestFail(
								Tracker.TYPE_REQUEST_VERIFY, 0);
						Toast.makeText(getActivity(),
								"Failed to load list from server",
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onSuccess(String response) {
						dataStore.setListValid(true);
						FriendData data = FriendData.fromJson(response);
						dataStore.setCachedData(response);
						updateFriendData(data);
						Toast.makeText(getActivity(), "Data updated...",
								Toast.LENGTH_SHORT).show();

					}
				});
	}

	private View getHeaderView() {
		View view;
		LayoutInflater inflater = (LayoutInflater) this.getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.meta_data, null);
		return view;
	}

	private void showCachedData() {
		String data = dataStore.getCachedData();
		if (data != null) {
			FriendData friendData = FriendData.fromJson(data);
			updateFriendData(friendData);
		}
	}

}
