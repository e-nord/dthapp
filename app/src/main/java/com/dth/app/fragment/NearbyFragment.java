package com.dth.app.fragment;

import android.os.Bundle;
import android.view.View;

import com.dth.app.Constants;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class NearbyFragment extends EventListFragment {

    private static final int NEARBY_EVENT_RADIUS_MILES = 20;
    private static final int LOCATION_FETCH_TIMEOUT_MS = 30000;

    public static NearbyFragment newInstance() {
        return new NearbyFragment();
    }

    public interface UserLocationUpdateListener {
        void onUserLocationUpdated();
    }


    private void setUserLocation(ParseGeoPoint geoPoint, final UserLocationUpdateListener listener){
        ParseUser user = ParseUser.getCurrentUser();
        user.put("currentLocation", geoPoint);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    listener.onUserLocationUpdated();
                }
            }
        });
    }

    public void updateUserLocation(final UserLocationUpdateListener listener){
//        Criteria criteria = new Criteria();
//        criteria.setPowerRequirement(Criteria.POWER_HIGH);
//        criteria.setAccuracy(Criteria.ACCURACY_LOW);
//        criteria.setAltitudeRequired(false);
//        criteria.setSpeedRequired(false);
//        criteria.setBearingRequired(false);
//        ParseGeoPoint.getCurrentLocationInBackground(LOCATION_FETCH_TIMEOUT_MS, criteria, new LocationCallback() {
//            @Override
//            public void done(ParseGeoPoint geoPoint, ParseException e) {
//                if(e == null){
//                    setUserLocation(geoPoint, listener);
//                    refresh();
//                } else {
//                    Log.e("NearbyFragment", "Location update error", e);
//                }
//            }
//        });
        ParseGeoPoint fakePoint = new ParseGeoPoint(47.6516580, -122.3422210);
        setUserLocation(fakePoint, listener);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().get("currentLocation") == null){
            updateUserLocation(new UserLocationUpdateListener() {
                @Override
                public void onUserLocationUpdated() {
                    load();
                }
            });
        } else {
            load();
        }
    }

    @Override
    public ParseQuery<ParseObject> getQuery() {
        ParseQuery<ParseObject> query = new ParseQuery<>(Constants.ActivityClassKey);
        ParseUser user = ParseUser.getCurrentUser();
        if (user == null) {
            query.setLimit(0);
        } else {

            // Ok, so we want Activities whose events are public and have a GeoLocation with 20 miles.
            ParseQuery<ParseObject> innerQuery = new ParseQuery<>(Constants.DTHEventClassKey);
            innerQuery.whereExists(Constants.DTHEventTypeKey);
            innerQuery.whereEqualTo(Constants.DTHEventTypeKey, Constants.DTHEventTypePublic);
            innerQuery.whereExists(Constants.DTHEventLocationKey);
            ParseGeoPoint geoPoint = (ParseGeoPoint) user.get("currentLocation");
            innerQuery.whereWithinMiles(Constants.DTHEventLocationKey, geoPoint, NEARBY_EVENT_RADIUS_MILES);

            query.whereEqualTo(Constants.ActivityTypeKey, Constants.ActivityTypeInvite);

            // Limit to public events within 20 miles!
            query.whereMatchesQuery(Constants.ActivityEventKey, innerQuery);

            // Call out the public tag
            query.whereExists(Constants.ActivityPublicTagKey);
            query.whereEqualTo(Constants.ActivityPublicTagKey, Constants.ActivityPublicTagTypePublic);

            // Query for events that I've already accepted!
            ParseQuery<ParseObject> publicInviteQuery = new ParseQuery<>(Constants.ActivityClassKey);
            publicInviteQuery.whereEqualTo(Constants.ActivityToUserKey, user);
            publicInviteQuery.whereEqualTo(Constants.ActivityPublicTagKey, Constants.ActivityPublicTagTypePublicInvite);
            publicInviteQuery.whereEqualTo(Constants.ActivityTypeKey, Constants.ActivityTypeInvite);
            publicInviteQuery.whereExists(Constants.ActivityFromUserKey);

            // Do not show events that I've already responded to!
            query.whereDoesNotMatchKeyInQuery(Constants.ActivityEventKey, Constants.ActivityEventKey, publicInviteQuery);

            // Create an orQuery for "public" activities that I have not responded to OR
            // "public_invite" activities that I have responded to
            List<ParseQuery<ParseObject>> queries = new LinkedList<>();
            Collections.addAll(queries, query, publicInviteQuery);
            ParseQuery<ParseObject> masterQuery = ParseQuery.or(queries);

            // Things to include
            masterQuery.include(Constants.ActivityToUserKey);
            //masterQuery.include(Constants.ActivityFromUserKey);
            masterQuery.include(Constants.ActivityReferringUserKey);
            masterQuery.include(Constants.ActivityEventKey);
            masterQuery.include(Constants.ActivityFromUserKey + "." + Constants.UserDisplayNameKey);

            // Do not show deleted events
            masterQuery.whereNotEqualTo(Constants.ActivityDeletedKey, true);

            masterQuery.orderByDescending("createdAt");
            masterQuery.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);
        }

        return query;
    }
}
