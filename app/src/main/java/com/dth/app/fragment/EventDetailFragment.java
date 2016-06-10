package com.dth.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dth.app.Constants;
import com.dth.app.R;
import com.dth.app.Utils;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.branch.invite.util.CircularImageView;

public class EventDetailFragment extends Fragment {

    public static EventDetailFragment newInstance() {
        return new EventDetailFragment();
    }

    private ParseObject event;

    @Bind(R.id.dt_detail_finish_in)
    TextView finishIn;

    @Bind(R.id.dt_detail_description)
    TextView description;

    @Bind(R.id.dt_detail_contacts_container)
    LinearLayout guestsContainer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dt_detail_view, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void initCircleTimer(){
        ParseQuery<ParseObject> hostsInviteEndsQuery = new ParseQuery<>(Constants.ActivityClassKey);
        hostsInviteEndsQuery.whereEqualTo(Constants.ActivityTypeKey, Constants.ActivityTypeInvite);
        hostsInviteEndsQuery.whereEqualTo(Constants.ActivityEventKey, event);
        hostsInviteEndsQuery.whereEqualTo(Constants.ActivityToUserKey, event.getString(Constants.DTHEventCreatedByUserKey));
        hostsInviteEndsQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if(e == null) {
                    object.getDate(Constants.ActivityExpirationKey);
                    //TODO show time circle
                }
            }
        });
    }

    private ParseQuery<ParseObject> getGuestListQuery(ParseObject event, boolean includePublic){
        ParseQuery<ParseObject> guestListQuery = new ParseQuery<>(Constants.ActivityClassKey);
        guestListQuery.whereEqualTo(Constants.ActivityTypeKey, Constants.ActivityTypeInvite);
        guestListQuery.whereEqualTo(Constants.ActivityEventKey, event);
        guestListQuery.whereNotEqualTo(Constants.ActivityDeletedKey, true);
        guestListQuery.whereExists(Constants.ActivityFromUserKey);
        guestListQuery.whereExists(Constants.ActivityToUserKey);
        guestListQuery.include(Constants.ActivityToUserKey);
        if(!includePublic) {
            guestListQuery.whereNotEqualTo(Constants.ActivityPublicTagKey, Constants.ActivityPublicTagTypePublic);
        }
        guestListQuery.orderByDescending(Constants.ActivityAcceptedKey);
        guestListQuery.addAscendingOrder("createdAt");
        guestListQuery.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);
        return guestListQuery;
    }

    @OnClick(R.id.down_button)
    public void onDown(){
        Toast.makeText(getActivity(), "Down", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.not_down_button)
    public void onNotDown(){
        Toast.makeText(getActivity(), "Not down", Toast.LENGTH_SHORT).show();
    }

    public void setEvent(final ParseObject event) {
        this.event = event;
        event.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                ParseQuery<ParseObject> guestListQuery = getGuestListQuery(object, true);
                guestListQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        LayoutInflater inflater = LayoutInflater.from(getContext());
                        for (ParseObject invite : objects) {
                            CircularImageView guestProfileIcon = (CircularImageView) inflater.inflate(R.layout.dt_detail_contact_view, guestsContainer, false);
                            ParseUser guest = (ParseUser) invite.get(Constants.ActivityToUserKey);
                            ParseFile file = guest.getParseFile(Constants.UserProfilePicSmallKey);
                            guestsContainer.addView(guestProfileIcon);
                            Picasso.with(getContext()).
                                    load(file.getUrl()).
                                    placeholder(R.drawable.ic_person_24dp).
                                    error(R.drawable.ic_person_24dp).
                                    into(guestProfileIcon);
                        }
                    }
                });

                String eventDescription = object.getString(Constants.DTHEventDescriptionKey);
                // Replace occurances of \n with a line break
                eventDescription = eventDescription.replaceAll("\\n", "\n");
                description.setText(eventDescription);
                long now = System.currentTimeMillis();
                long expirationTime = event.getLong(Constants.ActivityExpirationKey);
                long timeLeftMs = expirationTime - now;
                if(timeLeftMs > 0){
                    finishIn.setText(String.format(getString(R.string.finishes_in), Utils.timeMillisToString(timeLeftMs)));
                } else {
                    finishIn.setText(getString(R.string.finished));
                }
            }
        });
    }
}
