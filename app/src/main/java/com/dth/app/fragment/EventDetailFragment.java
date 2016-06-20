package com.dth.app.fragment;

import android.app.AlarmManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dth.app.Constants;
import com.dth.app.R;
import com.dth.app.Utils;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.HListView;
import jp.wasabeef.picasso.transformations.ColorFilterTransformation;
import timber.log.Timber;

public class EventDetailFragment extends Fragment {

    @Bind(R.id.dt_event_detail_finish_in)
    TextView finishIn;
    @Bind(R.id.dt_event_detail_description)
    TextView description;
    @Bind(R.id.dt_event_detail_guest_list)
    HListView guestsList;
    @Bind(R.id.dt_event_detail_button_container)
    ViewGroup buttonContainer;
    @Bind(R.id.dt_event_detail_comments_container)
    ViewGroup commentsContainer;
    @Bind(R.id.dt_event_detail_comments_list)
    ListView commentsList;
    @Bind(R.id.dt_event_detail_comment_edit)
    EditText commentEdit;

    private static final int COMMENTS_PER_PAGE = 10;

    private ParseQueryAdapter<ParseObject> commentAdapter;
    private ParseObject activity;
    private ParseObject event;
    private ParseUser creator;
    private EventListFragment.OnUserSelectedListener listener;

    public static EventDetailFragment newInstance() {
        return new EventDetailFragment();
    }

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
        ParseQueryAdapter.QueryFactory<ParseObject> factory = new ParseQueryAdapter.QueryFactory<ParseObject>() {
            @Override
            public ParseQuery create() {
                return getCommentsQuery(event);
            }
        };
        commentAdapter = new ParseQueryAdapter<ParseObject>(getActivity(), factory, R.layout.comment_view) {
            @Override
            public View getNextPageView(View v, ViewGroup parent) {
                return LayoutInflater.from(getContext()).inflate(R.layout.load_more_small, parent, false);
            }

            @Override
            public View getItemView(ParseObject comment, View v, ViewGroup parent) {
                View view = super.getItemView(comment, v, parent);
                bindCommentView(view, comment);
                return view;
            }
        };
        commentAdapter.setObjectsPerPage(COMMENTS_PER_PAGE);
        commentAdapter.setPaginationEnabled(true);
        commentAdapter.setAutoload(false);
        commentsList.setAdapter(commentAdapter);
    }

    private void initCircleTimer() {
        ParseQuery<ParseObject> hostsInviteEndsQuery = new ParseQuery<>(Constants.ActivityClassKey);
        hostsInviteEndsQuery.whereEqualTo(Constants.ActivityTypeKey, Constants.ActivityTypeInvite);
        hostsInviteEndsQuery.whereEqualTo(Constants.ActivityEventKey, event);
        hostsInviteEndsQuery.whereEqualTo(Constants.ActivityToUserKey, event.getString(Constants.DTHEventCreatedByUserKey));
        hostsInviteEndsQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    object.getDate(Constants.ActivityExpirationKey);
                    //TODO show time circle
                }
            }
        });
    }

    private ParseQuery<ParseObject> getGuestListQuery(ParseObject event, boolean includePublic) {
        ParseQuery<ParseObject> guestListQuery = new ParseQuery<>(Constants.ActivityClassKey);
        guestListQuery.whereEqualTo(Constants.ActivityTypeKey, Constants.ActivityTypeInvite);
        guestListQuery.whereEqualTo(Constants.ActivityEventKey, event);
        guestListQuery.whereNotEqualTo(Constants.ActivityDeletedKey, true);
        guestListQuery.whereExists(Constants.ActivityFromUserKey);
        guestListQuery.whereExists(Constants.ActivityToUserKey);
        guestListQuery.include(Constants.ActivityToUserKey);
        if (!includePublic) {
            guestListQuery.whereNotEqualTo(Constants.ActivityPublicTagKey, Constants.ActivityPublicTagTypePublic);
        }
        guestListQuery.orderByDescending(Constants.ActivityAcceptedKey);
        guestListQuery.addAscendingOrder(Constants.CREATED_AT);
        guestListQuery.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);
        return guestListQuery;
    }

    private void expireInviteEventually(final ParseObject activity, final SaveCallback callback) {
        final ParseUser user = activity.getParseUser(Constants.ActivityToUserKey);
        // Set expired to YES
        activity.put(Constants.ActivityExpiredKey, true);

        // Set expires time to now
        activity.put(Constants.ActivityExpirationKey, System.currentTimeMillis());

        activity.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    ParseUser currentUser = ParseUser.getCurrentUser();
                    // Create and send expire Activity/Push Notification
                    ParseObject expireActivity = new ParseObject(Constants.ActivityClassKey);
                    expireActivity.put(Constants.ActivityTypeKey, Constants.ActivityTypeExpire);
                    expireActivity.put(Constants.ActivityFromUserKey, currentUser);
                    expireActivity.put(Constants.ActivityActivityKey, activity);
                    expireActivity.put(Constants.ActivityToUserKey, user);

                    ParseACL dthACL = new ParseACL(currentUser);
                    dthACL.setPublicReadAccess(true);
                    expireActivity.setACL(dthACL);

                    // Call completionBlock after sending expire push notification
                    expireActivity.saveEventually(callback);
                } else {
                    Timber.e(e, "Failed to expire invite");
                }
            }
        });
    }

    private void acceptInviteEventually(ParseUser user, ParseObject event, SaveCallback callback) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (user.getObjectId().equals(currentUser.getObjectId())) {
            return;
        }

        ParseObject acceptActivity = new ParseObject(Constants.ActivityClassKey);
        acceptActivity.put(Constants.ActivityTypeKey, Constants.ActivityTypeAccept);
        acceptActivity.put(Constants.ActivityFromUserKey, currentUser);
        acceptActivity.put(Constants.ActivityEventKey, event);

        // Set the DT_ for Activity
        acceptActivity.put(Constants.ActivityDTKey, event.getString(Constants.DTHEventDTKey));

        String eventDescription = event.getString(Constants.DTHEventDescriptionKey);
        if (eventDescription != null) {
            acceptActivity.put(Constants.ActivityContentKey, eventDescription);
        }

        acceptActivity.put(Constants.ActivityToUserKey, user);

        ParseACL dthACL = new ParseACL(currentUser);
        dthACL.setPublicReadAccess(true);
        acceptActivity.setACL(dthACL);

        acceptActivity.saveEventually(callback);
    }

    private void setDown(boolean isDown) {
        hideButtons();

        if (activity.getString(Constants.ActivityPublicTagKey).equals(Constants.ActivityPublicTagTypePublic)) {
            ParseObject inviteActivity = new ParseObject(Constants.ActivityClassKey);
            respondToInvite(event, inviteActivity, isDown, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        loadGuestList(event);
                        loadComments();
                        showComments();
                    }  else {
                        Timber.e(e, "Failed to respond to invite");
                    }
                }
            });

        } else {

            if (isDown) {

                activity.put(Constants.ActivityAcceptedKey, true);

                long expirationTimeMs = event.getCreatedAt().getTime() + AlarmManager.INTERVAL_DAY * 7;
                activity.put(Constants.ActivityExpirationKey, expirationTimeMs);
                activity.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            loadGuestList(event);
                            loadComments();
                            showComments();
                        } else {
                            Timber.e(e, "Failed to save activity expiration");
                        }
                    }
                });

                acceptInviteEventually(creator, event, new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {

                        } else {
                            Timber.e(e, "Failed to accept invite");
                        }
                    }
                });
            } else {
                expireInviteEventually(activity, new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            loadGuestList(event);
                            loadComments();
                            showComments();
                        } else {
                            Timber.e(e, "Failed to expire invite");
                        }
                    }
                });
            }
        }
    }

    @OnClick(R.id.dt_event_detail_down_button)
    public void onDown() {
        Toast.makeText(getActivity(), "Down", Toast.LENGTH_SHORT).show();
        setDown(true);
    }

    @OnClick(R.id.dt_event_detail_not_down_button)
    public void onNotDown() {
        Toast.makeText(getActivity(), "Not down", Toast.LENGTH_SHORT).show();
        setDown(false);
    }

    private void showComments() {
        commentsContainer.setVisibility(View.VISIBLE);
    }

    private void hideComments() {
        commentsContainer.setVisibility(View.GONE);
    }

    private void disableComments() {
        commentEdit.setVisibility(View.GONE);
    }

    private void showButtons() {
        buttonContainer.setVisibility(View.VISIBLE);
    }

    private void hideButtons() {
        buttonContainer.setVisibility(View.GONE);
    }

    private void respondToInvite(ParseObject event, ParseObject activity, boolean isDown, SaveCallback callback) {
        if (event.getString(Constants.DTHEventTypeKey).equals(Constants.DTHEventTypePublic)) {
            return;
        }

        ParseUser currentUser = ParseUser.getCurrentUser();

        activity.put(Constants.ActivityTypeInvite, Constants.ActivityTypeInvite);
        activity.put(Constants.ActivityToUserKey, event.getParseUser(Constants.DTHEventCreatedByUserKey));
        activity.put(Constants.ActivityToUserKey, ParseUser.getCurrentUser());
        activity.put(Constants.ActivityExpiredKey, false);
        activity.put(Constants.ActivityEventKey, event);

        // Set the DT_ for Activity
        activity.put(Constants.DTHEventDTKey, event.getString(Constants.DTHEventDTKey));

        // Set activity accepted or unaccepted
        activity.put(Constants.ActivityAcceptedKey, isDown);

        if (isDown) {
            // Set expiration for 1 week from end of lifetime
            long eventLifeTimeMs = event.getLong(Constants.DTHEventLifetimeMinutesKey) * 60 * 1000;
            long extensionTimeMs = eventLifeTimeMs + AlarmManager.INTERVAL_DAY * 7;
            long newExpirationTime = event.getCreatedAt().getTime() + extensionTimeMs;
            activity.put(Constants.ActivityExpirationKey, newExpirationTime);
        } else {
            activity.put(Constants.ActivityExpirationKey, System.currentTimeMillis());
        }

        // Set PublicTag to PublicInvite
        activity.put(Constants.ActivityPublicTagKey, Constants.ActivityPublicTagTypePublicInvite);

        ParseACL inviteACL = new ParseACL(currentUser);
        // Allow the toUser to write to their invite
        inviteACL.setWriteAccess(event.getParseUser(Constants.DTHEventCreatedByUserKey), true);
        inviteACL.setPublicReadAccess(true);
        activity.setACL(inviteACL);

        activity.saveEventually(callback);
    }

    @Override
    public void onResume() {
        super.onResume();
        List<ParseObject> objects = new LinkedList<>();
        if (event != null) {
            objects.add(event);
        }
        if (activity != null) {
            objects.add(activity);
        }
        if (creator != null) {
            objects.add(creator);
        }
        if (!objects.isEmpty()) {
            ParseObject.fetchAllIfNeededInBackground(objects, new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        displayEvent(event);
                    } else {
                        Timber.e(e, "Failed to fetch event details");
                    }
                }
            });
        }
    }

    private void displayGuestList(final List<ParseObject> invites) {
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
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.dt_guest_icon, parent, false);
                }

                CircularImageView guestProfileIcon = (CircularImageView) convertView;
                final long now = System.currentTimeMillis();

                ParseObject invite = (ParseObject) getItem(position);
                final ParseUser guest = invite.getParseUser(Constants.ActivityToUserKey);
                convertView.setTag(guest);
                ParseFile file = guest.getParseFile(Constants.UserProfilePicSmallKey);
                boolean accepted = invite.getBoolean(Constants.ActivityAcceptedKey);
                int fadeColor = getResources().getColor(R.color.whiteOverlay);
                Date expirationDate = invite.getDate(Constants.ActivityExpirationKey);
                if (accepted && now < expirationDate.getTime()) {
                    fadeColor = getResources().getColor(android.R.color.transparent);
                } else if (!accepted && now >= expirationDate.getTime()) {
                    fadeColor = getResources().getColor(R.color.accentRedOverlay);
                }
                Picasso.with(getContext()).
                        load(file.getUrl()).
                        placeholder(R.drawable.ic_person_24dp).
                        error(R.drawable.ic_person_24dp).
                        transform(new ColorFilterTransformation(fadeColor)).
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

    private void loadGuestList(ParseObject event) {
        ParseQuery<ParseObject> guestListQuery = getGuestListQuery(event, true);
        guestListQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> invites, ParseException e) {
                displayGuestList(invites);
            }
        });
    }

    private void setFinishInText(ParseObject activity){
        long now = System.currentTimeMillis();
        Date expirationDate = activity.getDate(Constants.ActivityExpirationKey);
        long timeLeftMs = expirationDate.getTime() - now;
        if (timeLeftMs > 0) {
            finishIn.setText(String.format(getString(R.string.finishes_in), Utils.timeMillisToString(timeLeftMs)));
        } else {
            finishIn.setText(getString(R.string.finished));
        }
    }

    private void setDescriptionText(ParseObject event){
        String eventDescription = event.getString(Constants.DTHEventDescriptionKey);
        // Replace occurances of \n with a line break
        eventDescription = eventDescription.replaceAll("\\n", "\n");
        description.setText(eventDescription);
    }

    private void displayEvent(final ParseObject event) {
        setDescriptionText(event);
        setFinishInText(activity);
        loadGuestList(event);

        // Decide whether to show yes/no buttons
        String publicTag = activity.getString(Constants.ActivityPublicTagKey);
        if (publicTag != null && publicTag.equals(Constants.ActivityPublicTagTypePublic) &&
                !event.getParseUser(Constants.DTHEventCreatedByUserKey).getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
            // If it's a public event, show Down/Not Down and don't allow comments (unless this is the event creator)
            hideComments();
            showButtons();
        } else if (activity.getBoolean(Constants.ActivityAcceptedKey) || activity.getBoolean(Constants.ActivityExpiredKey)) {
            // User has accepted or declined the invite!
            hideButtons();
            loadComments();
            showComments();
        }

        // If the lifetime has expired, do not init the down/not down buttons
        // (opened a finished event)
        long needToKnowTime = event.getCreatedAt().getTime() + AlarmManager.INTERVAL_DAY * 7;
        if (System.currentTimeMillis() >= needToKnowTime) {
            hideButtons();
            disableComments();
        }

        commentEdit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    postComment(event);
                    return true;
                }
                return false;
            }
        });
    }

    private void postComment(final ParseObject event){
        String commentText = commentEdit.getText().toString().trim();
        ParseUser creator = event.getParseUser(Constants.DTHEventCreatedByUserKey);
        if (!TextUtils.isEmpty(commentText) && commentText.length() < 1024 && creator != null) {
            ParseObject comment = new ParseObject(Constants.ActivityClassKey);
            ParseUser currentUser = ParseUser.getCurrentUser();
            comment.put(Constants.ActivityContentKey, commentText);
            comment.put(Constants.ActivityToUserKey, creator);
            comment.put(Constants.ActivityFromUserKey, currentUser);
            comment.put(Constants.ActivityTypeKey, Constants.ActivityTypeComment);
            comment.put(Constants.ActivityEventKey, event);

            // Set the kPAPActivityDTKey
            comment.put(Constants.ActivityDTKey, event.getString(Constants.ActivityDTKey));

            ParseACL acl = new ParseACL(currentUser);
            acl.setPublicReadAccess(true);
            acl.setWriteAccess(creator, true);
            comment.setACL(acl);
            comment.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){
                        loadComments();
                        commentEdit.setText("");
                    } else {
                        Timber.e(e, "Failed to post comment");
                    }
                }
            });
        }
    }

    private void bindCommentView(View v, ParseObject comment) {
        CircularImageView commentProfilePic = (CircularImageView) v.findViewById(R.id.comment_view_profile_pic);
        ParseUser author = comment.getParseUser(Constants.ActivityFromUserKey);
        ParseFile smallPic = author.getParseFile(Constants.UserProfilePicSmallKey);
        Picasso.with(getActivity()).load(smallPic.getUrl()).placeholder(R.drawable.ic_person_24dp).into(commentProfilePic);

        TextView commentText = (TextView) v.findViewById(R.id.comment_view_text);
        String commentContent = comment.getString(Constants.ActivityContentKey);
        commentText.setText(commentContent);

        TextView commentAuthor = (TextView) v.findViewById(R.id.comment_view_user);
        String authorDisplayName = author.getString(Constants.UserDisplayNameKey);
        commentAuthor.setText(authorDisplayName);
    }

    private void loadComments() {
        commentAdapter.loadObjects();
    }

    private ParseQuery<ParseObject> getCommentsQuery(ParseObject event) {
        ParseQuery<ParseObject> commentsQuery = new ParseQuery<>(Constants.ActivityClassKey);
        commentsQuery.whereEqualTo(Constants.ActivityEventKey, event);
        commentsQuery.whereEqualTo(Constants.ActivityTypeKey, Constants.ActivityTypeComment);
        commentsQuery.include(Constants.ActivityFromUserKey);
        commentsQuery.orderByAscending(Constants.CREATED_AT);
        commentsQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        return commentsQuery;
    }

    public void setActivity(ParseObject activity) {
        this.activity = activity;
        this.event = activity.getParseObject(Constants.ActivityEventKey);
        this.creator = event.getParseUser(Constants.DTHEventCreatedByUserKey);
    }

    public void setOnUserSelectedListener(EventListFragment.OnUserSelectedListener listener) {
        this.listener = listener;
    }
}
