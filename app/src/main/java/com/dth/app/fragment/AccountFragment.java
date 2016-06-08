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
import com.parse.CountCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
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

    @Bind(R.id.account_contacts_invited)
    TextView contactsInvited;

    private ParseUser user;

    public static AccountFragment newInstance() {
        return new AccountFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_view, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        user.fetchIfNeededInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser object, ParseException e) {
                displayUser(object);
            }
        });
    }

    public void setUser(ParseUser user){
        this.user = user;
    }

    private void displayUser(ParseUser user) {
        String userDisplayName = user.getString(Constants.UserDisplayNameKey);

        username.setText(userDisplayName);

        ParseFile profilePicFile = user.getParseFile(Constants.UserProfilePicMediumKey);
        if (profilePicFile != null) {
            Picasso.with(getActivity()).load(profilePicFile.getUrl()).into(profilePic);
            Picasso.with(getActivity()).load(profilePicFile.getUrl()).noFade().transform(new BlurTransformation(getContext(), 5)).into(background);
        } else {
            Picasso.with(getActivity()).load(R.drawable.ic_person_48).into(profilePic);
        }

        dthCount.setText("0 DTHs");

        ParseQuery<ParseObject> dthCountQuery = new ParseQuery<>(Constants.DTHEventClassKey);
        dthCountQuery.whereEqualTo(Constants.DTHEventCreatedByUserKey, user);
        dthCountQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        dthCountQuery.countInBackground(new CountCallback() {
            @Override
            public void done(int count, ParseException e) {
                if (e == null) {
                    dthCount.setText(String.valueOf(count) + " DTH" + (count == 1 ? "" : "s"));
                }
            }
        });
        downCount.setText(String.format(getString(R.string.down_count), 0));
        notDownCount.setText(String.format(getString(R.string.not_down_count), 0));

        ParseQuery<ParseObject> downCountQuery = new ParseQuery<>(Constants.ActivityClassKey);
        downCountQuery.whereEqualTo(Constants.ActivityTypeKey, Constants.ActivityTypeInvite);
        downCountQuery.whereEqualTo(Constants.ActivityAcceptedKey, true);
        downCountQuery.whereEqualTo(Constants.ActivityToUserKey, user);
        downCountQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        downCountQuery.countInBackground(new CountCallback() {
            @Override
            public void done(int count, ParseException e) {
                if (e == null) {
                    downCount.setText(String.format(getString(R.string.down_count), count));
                    notDownCount.setText(String.format(getString(R.string.not_down_count), 0));
                }
            }
        });

        contactsInvited.setText(String.format(getString(R.string.contacts_invite_promo), 0)); //FIXME
    }
}
