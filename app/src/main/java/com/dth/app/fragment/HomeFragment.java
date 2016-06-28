package com.dth.app.fragment;

import android.os.Bundle;
import android.view.View;

import com.dth.app.Constants;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class HomeFragment extends EventListFragment {

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        load();
    }

    @Override
    public ParseQuery<ParseObject> getQuery() {
        ParseQuery<ParseObject> query = new ParseQuery<>(Constants.ActivityClassKey);
        ParseUser user = ParseUser.getCurrentUser();
        if (user == null) {
            query.setLimit(0);
        } else {
            query.whereEqualTo(Constants.ActivityToUserKey, user);

            //query.whereEqualTo(Constants.ActivityFromUserKey, user);

            query.whereEqualTo(Constants.ActivityTypeKey, Constants.ActivityTypeInvite);
            query.whereExists(Constants.ActivityFromUserKey);
            query.include(Constants.ActivityFromUserKey);
            query.include(Constants.ActivityReferringUserKey);
            query.include(Constants.ActivityEventKey);

            query.whereNotEqualTo(Constants.ActivityDeletedKey, true);

            query.orderByDescending(Constants.CREATED_AT);

            query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);

            //TODO 'reachability' check?
        }
        return query;
    }
}
