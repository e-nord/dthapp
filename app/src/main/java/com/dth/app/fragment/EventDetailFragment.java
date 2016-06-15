package com.dth.app.fragment;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.dth.app.Constants;
import com.dth.app.R;
import com.dth.app.Utils;
import com.mikhaellopez.circularimageview.CircularImageView;
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
import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.HListView;

public class EventDetailFragment extends Fragment {

    public static EventDetailFragment newInstance() {
        return new EventDetailFragment();
    }

    private ParseObject event;

    private EventListFragment.OnUserSelectedListener listener;

    @Bind(R.id.dt_event_detail_finish_in)
    TextView finishIn;

    @Bind(R.id.dt_event_detail_description)
    TextView description;

    @Bind(R.id.dt_event_detail_guest_list)
    HListView guestsList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dt_event_detail_view, container, false);
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

    @OnClick(R.id.dt_event_detail_down_button)
    public void onDown(){
        Toast.makeText(getActivity(), "Down", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.dt_event_detail_not_down_button)
    public void onNotDown(){
        Toast.makeText(getActivity(), "Not down", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        event.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject event, ParseException e) {
                displayEvent(event);
            }
        });
    }

    private void displayGuestList(final List<ParseObject> invites){
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        guestsList.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return invites.size();
            }

            @Override
            public Object getItem(int position) {
                return invites.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = inflater.inflate(R.layout.dt_guest_icon, parent, false);
                }

                CircularImageView guestProfileIcon = (CircularImageView) convertView;
                final long now = System.currentTimeMillis();

                ParseObject invite = (ParseObject) getItem(position);
                final ParseUser guest = (ParseUser) invite.get(Constants.ActivityToUserKey);
                convertView.setTag(guest);
                ParseFile file = guest.getParseFile(Constants.UserProfilePicSmallKey);
                boolean accepted = guest.getBoolean(Constants.ActivityAcceptedKey);
                if(accepted &&  now < guest.getLong(Constants.ActivityExpirationKey)){
                    guestProfileIcon.setColorFilter(new PorterDuffColorFilter(guestsList.getResources().getColor(R.color.fadedWhite), PorterDuff.Mode.SRC_IN));
                } else if(!accepted){
                    guestProfileIcon.setColorFilter(new PorterDuffColorFilter(guestsList.getResources().getColor(R.color.accentRed), PorterDuff.Mode.SRC_IN));
                } else {
                    guestProfileIcon.setColorFilter(null);
                }
                Picasso.with(getContext()).
                        load(file.getUrl()).
                        placeholder(R.drawable.ic_person_24dp).
                        error(R.drawable.ic_person_24dp).
                        into(guestProfileIcon);
                return convertView;
            }
        });
        guestsList.setDividerWidth(50);
        guestsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParseUser guest = (ParseUser) view.getTag();
                listener.onUserSelected(guest);
            }
        });
    }

    private void displayEvent(ParseObject event){
        ParseQuery<ParseObject> guestListQuery = getGuestListQuery(event, true);
        guestListQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                displayGuestList(objects);
            }
        });

        String eventDescription = event.getString(Constants.DTHEventDescriptionKey);
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

    private void bindView(View v, ParseObject comment){
        String commentText = comment.getString(Constants.ActivityContentKey);
        ParseUser author = comment.getParseUser(Constants.ActivityFromUserKey);
        String authorDisplayName = author.getString(Constants.UserDisplayNameKey);
    }

    private ParseQuery<ParseObject> getCommentsQuery(ParseObject event){
        ParseQuery<ParseObject> commentsQuery = new ParseQuery<>(Constants.ActivityClassKey);
        commentsQuery.whereEqualTo(Constants.ActivityEventKey, event);
        commentsQuery.whereEqualTo(Constants.ActivityTypeKey, Constants.ActivityTypeComment);
        commentsQuery.include(Constants.ActivityFromUserKey);
        commentsQuery.orderByAscending("createdAt");
        commentsQuery.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        return commentsQuery;
    }

    public void setEvent(final ParseObject event) {
        this.event = event;
    }

    public void setOnUserSelectedListener(EventListFragment.OnUserSelectedListener listener) {
        this.listener = listener;
    }
}
