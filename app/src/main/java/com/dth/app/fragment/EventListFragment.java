package com.dth.app.fragment;

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
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class EventListFragment extends SwipeRefreshListFragment {

    private ParseQueryAdapter<ParseObject> adapter;

    public static String getTimeRemainingString(long timeLeftMs) {
        long days = TimeUnit.MILLISECONDS.toDays(timeLeftMs);
        timeLeftMs -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(timeLeftMs);
        timeLeftMs -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeftMs);
        timeLeftMs -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeftMs);
        if (days > 0) {
            return days + " days";
        }
        if (hours > 0) {
            return hours + " hours";
        }
        if (minutes > 0) {
            return minutes + " minutes";
        }
        return seconds + " seconds";
    }

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
        try {
            activityUser.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
        long timeLeftMs = finishTime - now;
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
                String timeLeft = getTimeRemainingString(timeLeftMs);
                status.setText(timeLeft + " left");
                textColor = getResources().getColor(R.color.black);
                color = getResources().getColor(R.color.white);
            }
        }

        status.setTextColor(textColor);
        statusColor.setBackgroundColor(color);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setVerticalScrollBarEnabled(false);
        getListView().setDrawSelectorOnTop(true);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getActivity().getSupportFragmentManager().
                        beginTransaction().
                        setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right).
                        addToBackStack("detail").
                        replace(R.id.content_main, EventDetailFragment.newInstance()).
                        commit();
            }
        });
        ParseQueryAdapter.QueryFactory<ParseObject> factory =
                new ParseQueryAdapter.QueryFactory<ParseObject>() {
                    public ParseQuery<ParseObject> create() {
                        return getQuery();
                    }
                };

        // Pass the factory into the ParseQueryAdapter's constructor.
        adapter = new ParseQueryAdapter<ParseObject>(getActivity(), factory) {
            @Override
            public View getItemView(ParseObject activity, View v, ViewGroup parent) {
                if (v == null) {
                    v = LayoutInflater.from(getContext()).inflate(R.layout.dt_list_item, parent, false);
                }
                super.getItemView(activity, v, parent);
                bindView(v, activity);
                return v;
            }
        };

        adapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<ParseObject>() {
            public void onLoading() {
                Toast.makeText(getContext(), "Loading!", Toast.LENGTH_SHORT).show();
                setRefreshing(true);
            }

            @Override
            public void onLoaded(List<ParseObject> objects, Exception e) {
                if (e == null) {
                    Toast.makeText(getContext(), "Loaded " + objects.size() + " events!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                setRefreshing(false);
            }
        });

        adapter.setAutoload(false);
        adapter.setPaginationEnabled(true);
        adapter.setObjectsPerPage(20);
        setListAdapter(adapter);
        setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setRefreshing(false);
                    }
                }, 5000);
            }
        });
    }
}
