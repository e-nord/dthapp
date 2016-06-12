package com.dth.app.fragment;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
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

import java.util.List;

public abstract class EventListFragment extends SwipeRefreshListFragment {

    private ParseQueryAdapter<ParseObject> adapter;
    private OnEventSelectedListener listener;

    public abstract ParseQuery<ParseObject> getQuery();

    public void refresh() {
        adapter.notifyDataSetChanged();
    }

    public void load(){
        adapter.loadObjects();
    }

    public void bindView(View v, ParseObject activity) {
        TextView eventName = (TextView) v.findViewById(R.id.dt_name);
        ImageView pic = (ImageView) v.findViewById(R.id.dt_profile_pic);
        TextView status = (TextView) v.findViewById(R.id.dt_status_text);
        View statusColor = v.findViewById(R.id.dt_status_color);
        TextView nearbyLabel = (TextView) v.findViewById(R.id.dt_nearby_label);

        String currentUserDisplayName = ParseUser.getCurrentUser().getString(Constants.UserDisplayNameKey);
        ParseUser activityUser = (ParseUser) activity.get(Constants.ActivityFromUserKey);
        String activityName = activity.getString(Constants.ActivityDTKey);
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

        boolean isPublicEvent = true; //FIXME
        if (isPublicEvent ||
                activity.getString(Constants.ActivityPublicTagKey).equals(Constants.ActivityPublicTagTypePublicInvite) ||
                activity.getString(Constants.ActivityPublicTagKey).equals(Constants.ActivityPublicTagTypePublic)) {
            nearbyLabel.setVisibility(View.INVISIBLE);
        } else {
            nearbyLabel.setVisibility(View.VISIBLE);
        }

        int textColor;
        int color;
        long now = System.currentTimeMillis();
        long lifetimeSeconds = activity.getLong(Constants.DTHEventLifetimeKey) * 60;
        long finishTime = activity.getCreatedAt().getTime() + lifetimeSeconds;
        long timeLeftMs = now - finishTime;
        if (timeLeftMs <= 0) {
            status.setText(R.string.finished);
            color = getResources().getColor(R.color.colorLightAccent);
            textColor = color;
        } else {
            if (activity.getBoolean(Constants.ActivityAcceptedKey)) {
                color = ContextCompat.getColor(getContext(), R.color.colorPrimary);
                textColor = color;
                status.setText(R.string.down);
            } else {
                String timeLeft = Utils.timeMillisToString(timeLeftMs);
                status.setText(timeLeft + " left");
                textColor = getResources().getColor(R.color.black);
                color = getResources().getColor(R.color.white);
            }
        }

        status.setTextColor(textColor);
        statusColor.setBackgroundColor(color);
    }

    public interface OnEventSelectedListener {
        void onEventSelected(ParseObject event);
    }

    public void setOnEventSelectedListener(OnEventSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setVerticalScrollBarEnabled(false);
        getListView().setDrawSelectorOnTop(true);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParseObject activity = (ParseObject) parent.getItemAtPosition(position);
                listener.onEventSelected(activity.getParseObject(Constants.ActivityEventKey));
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

        };

        adapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<ParseObject>() {
            public void onLoading() {
                setRefreshing(true);
            }

            @Override
            public void onLoaded(List<ParseObject> objects, Exception e) {
                if(isAdded()) {
                    if (objects != null) {
                        Toast.makeText(getActivity(), "Loaded " + objects.size() + " events!", Toast.LENGTH_LONG).show();
                    } else if (e != null) {
                        Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    setRefreshing(false);
                }
            }
        });

        adapter.setAutoload(false);
        adapter.setPaginationEnabled(true);
        adapter.setObjectsPerPage(20);
        setListAdapter(adapter);
    }
}
