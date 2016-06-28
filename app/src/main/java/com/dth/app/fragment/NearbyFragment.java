package com.dth.app.fragment;

import android.os.Bundle;
import android.view.View;

import com.dth.app.Constants;
import com.dth.app.Location;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class NearbyFragment extends EventListFragment {

    private static final int NEARBY_EVENT_RADIUS_MILES = 20;

    public static NearbyFragment newInstance() {
        return new NearbyFragment();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().getParseGeoPoint(Constants.CURRENT_LOCATION) == null){
            Location.updateUserLocation(new Location.UserLocationUpdateListener() {
                @Override
                public void onUserLocationUpdated(ParseGeoPoint geoPoint) {
                    load();
                }

                @Override
                public void onUserLocationError(Exception e) {
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
            ParseGeoPoint geoPoint = user.getParseGeoPoint(Constants.CURRENT_LOCATION);
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
            masterQuery.include(Constants.ActivityFromUserKey);
            masterQuery.include(Constants.ActivityReferringUserKey);
            masterQuery.include(Constants.ActivityEventKey);

            // Do not show deleted events
            masterQuery.whereNotEqualTo(Constants.ActivityDeletedKey, true);

            masterQuery.orderByDescending(Constants.CREATED_AT);
            masterQuery.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);

            return masterQuery;
        }

        return query;
    }
}
