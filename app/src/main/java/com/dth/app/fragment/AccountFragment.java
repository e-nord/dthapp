package com.dth.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dth.app.Constants;
import com.dth.app.R;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.picasso.transformations.BlurTransformation;

public class AccountFragment extends Fragment {

    @Bind(R.id.account_username)
    TextView username;

    @Bind(R.id.account_profile_pic)
    ImageView profilePic;

    @Bind(R.id.account_background)
    ImageView background;

    @Bind(R.id.account_down_count)
    TextView downCount;

    @Bind(R.id.account_not_down_count)
    TextView notDownCount;

    @Bind(R.id.account_dth_count)
    TextView dthCount;

    public static AccountFragment newInstance(ParseUser user) {
        Bundle args = new Bundle();
        args.putString("objectId", user.getObjectId());
        AccountFragment f = new AccountFragment();
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_view, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        displayUser(ParseUser.getCurrentUser());

//        ParseQuery<ParseUser> query = ParseUser.getQuery();
//        query.whereEqualTo("objectId", getArguments().getString("objectId"));
//        query.findInBackground(new FindCallback<ParseUser>() {
//            @Override
//            public void done(List<ParseUser> objects, ParseException e) {
//                if (e == null) {
//                    if (!objects.isEmpty()) {
//                        displayUser(objects.get(0));
//                    }
//                }
//            }
//        });
    }

    private void displayUser(ParseUser user) {
        String userDisplayName = user.getString("displayName");

        username.setText(userDisplayName);

        String url = user.getString(Constants.UserProfilePicMediumKey);
        if (url != null) {
            Picasso.with(getActivity()).load(url).into(profilePic);
            Picasso.with(getActivity()).load(url).noFade().transform(new BlurTransformation(getContext(), 4)).into(background);
        } else {
            //TODO use default profile pic
        }

        dthCount.setText("0 DTHs");

//        ParseQuery<ParseObject> dthCountQuery = new ParseQuery<>(Constants.DTHEventClassKey);
//        dthCountQuery.whereEqualTo(Constants.DTHEventCreatedByUserKey, user);
//        dthCountQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
//        dthCountQuery.countInBackground(new CountCallback() {
//            @Override
//            public void done(int count, ParseException e) {
//                if (e == null) {
//                    dthCount.setText(String.valueOf(count) + "DTH" + (count == 1 ? "" : "s"));
//                }
//            }
//        });
//
        downCount.setText("0");
//
//        ParseQuery<ParseObject> downCountQuery = new ParseQuery<>(Constants.ActivityClassKey);
//        downCountQuery.whereEqualTo(Constants.ActivityTypeKey, Constants.ActivityTypeInvite);
//        downCountQuery.whereEqualTo(Constants.ActivityAcceptedKey, true;
//        downCountQuery.whereEqualTo(Constants.ActivityToUserKey, user);
//        downCountQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
//        downCountQuery.countInBackground(new CountCallback() {
//            @Override
//            public void done(int count, ParseException e) {
//                if (e == null) {
//                    downCount.setText(String.valueOf(count));
//                }
//            }
//        });

        //TODO query and set not down count?
    }
}
