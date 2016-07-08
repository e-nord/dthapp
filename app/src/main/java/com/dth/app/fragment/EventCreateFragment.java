package com.dth.app.fragment;

import android.app.AlarmManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dth.app.Constants;
import com.dth.app.InviteUtils;
import com.dth.app.Location;
import com.dth.app.R;
import com.dth.app.activity.MainActivity;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.HListView;
import timber.log.Timber;

public class EventCreateFragment extends Fragment {

    @Bind(R.id.event_create_spinner)
    AppCompatSpinner spinner;
    @Bind(R.id.event_create_title)
    TextView eventTitle;
    @Bind(R.id.event_create_edit)
    EditText eventDescriptionEdit;
    @Bind(R.id.event_create_next_button)
    Button next;
    @Bind(R.id.event_create_suggestions_list)
    HListView suggestionsList;

    private OnEventCreatedListener listener;

    public static EventCreateFragment newInstance() {
        return new EventCreateFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_create, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public ParseQuery<ParseObject> getDefaultSuggestionsQuery() {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Defaults");
        query.whereEqualTo("type", "defaultDT");
        query.orderByAscending("order");
        return query;
    }

    private void loadSuggestions(){
        ParseQueryAdapter.QueryFactory<ParseObject> factory = new ParseQueryAdapter.QueryFactory<ParseObject>() {
            @Override
            public ParseQuery<ParseObject> create() {
                return getDefaultSuggestionsQuery();
            }
        };
        ParseQueryAdapter<ParseObject> adapter = new ParseQueryAdapter<ParseObject>(getActivity(), factory, R.layout.suggestion_view) {
            @Override
            public View getNextPageView(View v, ViewGroup parent) {
                return LayoutInflater.from(getContext()).inflate(R.layout.load_more_small, parent, false);
            }
            @Override
            public View getItemView(ParseObject comment, View v, ViewGroup parent) {
                View view = super.getItemView(comment, v, parent);
                bindSuggestionView(view, comment);
                return view;
            }
        };
        adapter.setPaginationEnabled(true);
        adapter.setAutoload(true);
        suggestionsList.setAdapter(adapter);
        suggestionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParseObject suggestion = (ParseObject) parent.getItemAtPosition(position);
                eventDescriptionEdit.setText(suggestion.getString("value"));
            }
        });
    }

    private void bindSuggestionView(View view, ParseObject suggestion) {
        TextView text = (TextView) view.findViewById(R.id.suggestion_view_text);
        text.setText(suggestion.getString("key"));
        String colorHex = suggestion.getString("color").replaceFirst("0x", "#");
        int color = Color.parseColor(colorHex);
        DrawableCompat.setTint(text.getBackground(), color);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        KeyboardVisibilityEvent.setEventListener(getActivity(), new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean isOpen) {
                if (isOpen) {
                    ((MainActivity)getActivity()).getBottomBar().hide();
                    suggestionsList.setVisibility(View.VISIBLE);
                } else {
                    suggestionsList.setVisibility(View.GONE);
                    ((MainActivity)getActivity()).getBottomBar().show();
                }
            }
        });

        String text = eventDescriptionEdit.getText().toString();
        updateTitle(text);
        updateButton(text);

        eventDescriptionEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTitle(s.toString());
                updateButton(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        List<TimePeriod> items = new LinkedList<>();
        items.add(new TimePeriod("15 minutes", 15));
        items.add(new TimePeriod("1 hour", 60));
        items.add(new TimePeriod("3 hours", 3 * 60));
        items.add(new TimePeriod("1 day", 24 * 60));
        items.add(new TimePeriod("5 days", 5 * 24 * 60));
        spinner.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.spinner_item, R.id.spinner_text, items));

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EventInviteFragment fragment = EventInviteFragment.newInstance();
                fragment.setOnUsersInvitedListener(new EventInviteFragment.OnUsersInvitedListener() {
                    @Override
                    public void onUsersInvited(final List<ParseUser> users, final List<ContactsListFragment.Contact> contacts, boolean isPublic) {
                        final String eventDescription = eventDescriptionEdit.getText().toString();
                        final String dtText = eventTitle.getText().toString();
                        final String eventType = isPublic ? Constants.DTHEventTypePublic : Constants.DTHEventTypePrivate;
                        final long lifeTimeMinutes = ((TimePeriod) spinner.getSelectedItem()).getLengthMinutes();

                        final ParseGeoPoint geoPoint = Location.getLastGeoPoint();
                        final OnEventCreatedListener eventCreatedListener = new OnEventCreatedListener() {
                            @Override
                            public void onEventCreated(final ParseObject event) {
                                final SaveCallback callback = new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if(e == null){
                                            if(listener != null){
                                                listener.onEventCreated(event);
                                            }
                                            inviteContacts(contacts);
                                            getActivity().getSupportFragmentManager().
                                                    beginTransaction().
                                                    setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right).
                                                    remove(fragment).
                                                    commit();
                                        } else {
                                            if(listener != null){
                                                listener.onEventCreationFailure(e);
                                            }
                                            Timber.e(e, "Failed to invite users");
                                        }
                                    }
                                };
                                inviteUsers(users, event, callback);
                            }

                            @Override
                            public void onEventCreationFailure(Exception e) {
                                Timber.e(e, "Failed to create event");
                                if(getView() != null) {
                                    Snackbar snackbar = Snackbar.make(getView(), "Failed to create event!", Snackbar.LENGTH_LONG);
                                    snackbar.show();
                                }
                            }
                        };
                        createEvent(eventType, eventDescription, dtText, geoPoint, lifeTimeMinutes, eventCreatedListener);
                    }
                });

                getActivity().getSupportFragmentManager().
                        beginTransaction().
                        setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right).
                        addToBackStack("invite").
                        add(R.id.activity_main_content_main, fragment).
                        commit();
            }
        });

        loadSuggestions();
    }

    private void inviteContacts(List<ContactsListFragment.Contact> contacts){
        for(ContactsListFragment.Contact contact : contacts) {
            if (contact.getPhoneNumber() != null) {
                InviteUtils.inviteContact(contact.getPhoneNumber(), new FunctionCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        if(e == null) {
                            Toast.makeText(getContext(), "SMS invitation sent!", Toast.LENGTH_LONG).show();
                        } else {
                            Timber.e(e, "Failed to invite an SMS contact");
                            Toast.makeText(getContext(), "Failed to invite one or more SMS contacts", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }
    }

    private void inviteUsers(final List<ParseUser> users, ParseObject event, SaveCallback callback) {
        for (ParseUser user : users) {

            // Generate expiration date lifetime minutes in the future
            long lifeTimeMinutes = event.getLong(Constants.DTHEventLifetimeMinutesKey);
            long lifeTimeMs = lifeTimeMinutes * 60 * 1000;

            Date expirationDate = new Date(event.getCreatedAt().getTime() + lifeTimeMs);

            ParseObject invite = new ParseObject(Constants.ActivityClassKey);
            invite.put(Constants.ActivityTypeKey, Constants.ActivityTypeInvite);

            // Invite fromUser will always be the event creator
            ParseUser creator = event.getParseUser(Constants.DTHEventCreatedByUserKey);
            invite.put(Constants.ActivityFromUserKey, creator);
            invite.put(Constants.ActivityToUserKey, user);

            ParseUser currentUser = ParseUser.getCurrentUser();
            // If event creator is different from currentUser, set referring user to currentUser

            if (!currentUser.getObjectId().equals(creator.getObjectId())) {
                invite.put(Constants.ActivityReferringUserKey, currentUser);
            }

            invite.put(Constants.ActivityExpiredKey, false);
            invite.put(Constants.ActivityEventKey, event);
            // Set the DT_ for Activity
            invite.put(Constants.ActivityDTKey, event.getString(Constants.DTHEventDTKey));

            // If it's the current user, create an automatically accepted invite that expires 24 hours after lifetime
            if (user.getObjectId().equals(currentUser.getObjectId())) {
                invite.put(Constants.ActivityAcceptedKey, true);

                // Set expiration for 1 week from end of lifetime
                long newExpirationDate = event.getCreatedAt().getTime() + lifeTimeMs + AlarmManager.INTERVAL_DAY * 7;
                invite.put(Constants.ActivityExpirationKey, new Date(newExpirationDate));

                // If the event is public, and this user created it, tag their invite to be shown in the public feed
                if (event.getString(Constants.DTHEventTypeKey).equals(Constants.DTHEventTypePublic)) {
                    invite.put(Constants.ActivityPublicTagKey, Constants.ActivityPublicTagTypePublic);
                }
            } else {
                invite.put(Constants.ActivityAcceptedKey, false);
                invite.put(Constants.ActivityExpirationKey, expirationDate);

                // If it's a public event that I'm being invited to, set the publicInvite tag
                if (event.getString(Constants.DTHEventTypeKey).equals(Constants.DTHEventTypePublic)) {
                    invite.put(Constants.ActivityPublicTagKey, Constants.ActivityPublicTagTypePublicInvite);
                }
            }

            invite.put(Constants.ActivityContentKey, "You've been invited!"); //FIXME?

            ParseACL acl = new ParseACL(currentUser);
            // Allow the event creator to write to the invite
            acl.setWriteAccess(creator, true);
            // Allow the toUser to write to their invite
            acl.setWriteAccess(user, true);
            acl.setPublicReadAccess(true);
            invite.saveInBackground(callback);
        }
    }

    private void createEvent(String eventType, String description, String DTString, ParseGeoPoint geoPoint, final long lifeTimeMinutes, final OnEventCreatedListener listener) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        final ParseObject event = new ParseObject(Constants.DTHEventClassKey);
        event.put(Constants.DTHEventCreatedByUserKey, currentUser);

        String uniqueId = UUID.randomUUID().toString();

        if (geoPoint != null) {
            event.put(Constants.DTHEventLocationKey, geoPoint);
        }

        event.put(Constants.DTHEventTypeKey, eventType);
        event.put(Constants.DTHEventDescriptionKey, description);
        event.put(Constants.DTHEventUUIDKey, uniqueId);
        event.put(Constants.DTHEventLifetimeMinutesKey, lifeTimeMinutes);

        event.put(Constants.DTHEventDTKey, DTString);

        ParseACL acl = new ParseACL(currentUser);
        acl.setPublicReadAccess(true);
        event.setACL(acl);

        event.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    listener.onEventCreationFailure(e);
                } else {
                    listener.onEventCreated(event);
                }
            }
        });
    }

    private void initCircleTimer(ParseObject event) {
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

    private void updateButton(String input) {
        next.setVisibility(input.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void updateTitle(String input) {
        String firstChar;
        if (input.isEmpty()) {
            firstChar = "";
        } else {
            int codepoint = input.codePointAt(0);
            char[] ch = Character.toChars(codepoint);
            firstChar = new String(ch).toUpperCase();
        }
        eventTitle.setText(String.format(getString(R.string.DT), firstChar));
    }

    public void setOnEventCreatedListener(OnEventCreatedListener listener) {
        this.listener = listener;
    }

    public interface OnEventCreatedListener {
        void onEventCreated(ParseObject event);

        void onEventCreationFailure(Exception e);
    }

    private class TimePeriod {
        private final String text;
        private final long lengthMinutes;

        private TimePeriod(String text, long lengthMinutes) {
            this.text = text;
            this.lengthMinutes = lengthMinutes;
        }

        public String getText() {
            return text;
        }

        public long getLengthMinutes() {
            return lengthMinutes;
        }

        @Override
        public String toString() {
            return text;
        }
    }


}
