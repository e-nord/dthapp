package com.dth.app.fragment;

import android.app.AlarmManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dth.app.Constants;
import com.dth.app.R;
import com.dth.app.Utils;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

public abstract class EventListFragment extends SwipeRefreshListFragment {

    private static final int OBJECTS_PER_PAGE = 15;
    private ParseQueryAdapter<ParseObject> adapter;
    private OnEventSelectedListener eventSelectedListener;
    private OnUserSelectedListener userSelectedListener;

    public abstract ParseQuery<ParseObject> getQuery();

    public void load(){
        adapter.loadObjects();
    }

    public void bindView(View v, final ParseObject activity) {
        final TextView eventName = (TextView) v.findViewById(R.id.dt_list_item_name);
        final ImageView pic = (ImageView) v.findViewById(R.id.dt_list_item_profile_pic);
        TextView statusText = (TextView) v.findViewById(R.id.dt_list_item_status_text);
        View statusColorBar = v.findViewById(R.id.dt_list_item_status_color);
        TextView nearbyLabel = (TextView) v.findViewById(R.id.dt_list_item_nearby_label);
        v.setTag(activity);

        final String currentUserDisplayName = ParseUser.getCurrentUser().getString(Constants.UserDisplayNameKey);
        final String activityName = activity.getString(Constants.ActivityDTKey);
        final ParseUser activityUser = activity.getParseUser(Constants.ActivityFromUserKey);

        String activityUserDisplayName = activityUser.getString(Constants.UserDisplayNameKey);
        String activityDisplayName = "DTH"; //FIXME
        if (!TextUtils.isEmpty(activityUserDisplayName)) {
            if (!TextUtils.isEmpty(activityName)) {
                if (activityUserDisplayName.equals(currentUserDisplayName)) {
                    activityDisplayName = String.format("%s - %s", activityName, getString(R.string.me));
                } else {
                    activityDisplayName = String.format("%s - %s", activityName, activityUserDisplayName);
                }
            }
            if (activityDisplayName.equals(currentUserDisplayName)) {
                activityDisplayName = getString(R.string.me);
            }
        }
        eventName.setText(activityDisplayName);

        ParseFile profilePic = activityUser.getParseFile(Constants.UserProfilePicSmallKey);
        Picasso.with(getContext()).load(profilePic.getUrl()).into(pic);

        String publicTag = activity.getString(Constants.ActivityPublicTagKey);
        if (publicTag != null && (publicTag.equals(Constants.ActivityPublicTagTypePublicInvite) || publicTag.equals(Constants.ActivityPublicTagTypePublic))) {
            nearbyLabel.setVisibility(View.VISIBLE);
        } else {
            nearbyLabel.setVisibility(View.INVISIBLE);
        }

        if(updateTimeleft(activity, statusText, statusColorBar)) {
            queueTimeUpdate(v, activity, statusText, statusColorBar, AlarmManager.INTERVAL_HOUR / 60);
        }
    }

    private void queueTimeUpdate(final View v, final ParseObject activity, final TextView statusText, final View statusColorBar, final long intervalMs){
        v.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(v.getTag() == activity) { //FIXME use strict equality?
                    if(updateTimeleft(activity, statusText, statusColorBar)) {
                        queueTimeUpdate(v, activity, statusText, statusColorBar, intervalMs);
                    }
                }
            }
        }, intervalMs);
    }

    private boolean updateTimeleft(ParseObject activity, TextView statusText, View statusColorBar){
        boolean shouldUpdate = false;
        int textColor;
        int color;
        long now = System.currentTimeMillis();
        ParseObject event = activity.getParseObject(Constants.ActivityEventKey);
        long lifetimeMs = event.getLong(Constants.DTHEventLifetimeMinutesKey) * 60 * 1000;
        long finishTime = activity.getCreatedAt().getTime() + lifetimeMs;
        long timeLeftMs = finishTime - now;
        boolean expired = activity.getBoolean(Constants.ActivityExpiredKey);
        Date expirationDate = activity.getDate(Constants.ActivityExpirationKey);
        if(expired){
            statusText.setText(R.string.finished);
            color = getResources().getColor(R.color.colorLightAccent);
            textColor = color;
        } else {
            if(now < expirationDate.getTime()) {
                if (activity.getBoolean(Constants.ActivityAcceptedKey)) {
                    color = ContextCompat.getColor(getContext(), R.color.colorPrimary);
                    textColor = color;
                    statusText.setText(R.string.down);
                } else {
                    String timeLeft = Utils.timeMillisToString(timeLeftMs);
                    statusText.setText(String.format(getString(R.string.time_left), timeLeft));
                    textColor = getResources().getColor(R.color.accentDarkRed);
                    color = getResources().getColor(R.color.accentDarkRed);
                    shouldUpdate = true;
                }
            } else {
                statusText.setText(R.string.finished);
                color = getResources().getColor(R.color.colorLightAccent);
                textColor = color;
            }
        }
        statusText.setTextColor(textColor);
        statusColorBar.setBackgroundColor(color);
        return shouldUpdate;
    }

    public interface OnEventSelectedListener {
        void onEventSelected(ParseObject activity);
    }

    public interface OnUserSelectedListener {
        void onUserSelected(ParseUser user);
    }

    public void setOnEventSelectedListener(OnEventSelectedListener listener) {
        this.eventSelectedListener = listener;
    }

    public void setOnUserSelectedListener(OnUserSelectedListener listener){
        this.userSelectedListener = listener;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setVerticalScrollBarEnabled(false);
        getListView().setDrawSelectorOnTop(true);
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ParseObject activity = (ParseObject) parent.getItemAtPosition(position);
                ParseUser user = activity.getParseUser(Constants.ActivityFromUserKey);
                userSelectedListener.onUserSelected(user);
                return true;
            }
        });
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParseObject activity = (ParseObject) parent.getItemAtPosition(position);
                eventSelectedListener.onEventSelected(activity);
            }
        });
        ParseQueryAdapter.QueryFactory<ParseObject> factory =
                new ParseQueryAdapter.QueryFactory<ParseObject>() {
                    public ParseQuery<ParseObject> create() {
                        return getQuery();
                    }
                };

        // Pass the factory into the ParseQueryAdapter's constructor.
        adapter = new ParseQueryAdapter<ParseObject>(getActivity(), factory, R.layout.dt_list_item) {
            @Override
            public View getItemView(ParseObject activity, View v, ViewGroup parent) {
                View view = super.getItemView(activity, v, parent);
                bindView(view, activity);
                return view;
            }

            @Override
            public View getNextPageView(View v, ViewGroup parent) {
                return LayoutInflater.from(getContext()).inflate(R.layout.load_more, parent, false);
            }
        };

        adapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<ParseObject>() {
            public void onLoading() {
                setRefreshing(true);
            }

            @Override
            public void onLoaded(List<ParseObject> objects, Exception e) {
                if(isAdded()) {
                    if (objects != null) {
                       // Toast.makeText(getActivity(), "Loaded " + objects.size() + " events!", Toast.LENGTH_LONG).show();
                    } else if (e != null) {
                        Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    setRefreshing(false);
                }
            }
        });

        adapter.setAutoload(false);
        adapter.setPaginationEnabled(true);
        adapter.setObjectsPerPage(OBJECTS_PER_PAGE);
        setListAdapter(adapter);

        setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                load();
            }
        });
    }
}
