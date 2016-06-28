package com.dth.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dth.app.Constants;
import com.dth.app.R;
import com.parse.CountCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ContactsInviteFragment extends Fragment {

    @Bind(R.id.contacts_invite_selected_count)
    TextView contactsSelected;

    @Bind(R.id.contacts_invite_invited_count)
    TextView contactsInvited;

    @Bind(R.id.contacts_invite_send_button)
    FloatingActionButton sendButton;

    private ContactsListFragment contactsList;

    public static ContactsInviteFragment newInstance() {
        return new ContactsInviteFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.contacts_invite_fragment, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        contactsList = (ContactsListFragment) getChildFragmentManager().findFragmentById(R.id.contacts_invite_list);
        contactsList.setContactListener(new ContactsListFragment.OnContactListener() {
            @Override
            public void onContactSelected(ContactsListFragment.Contact contact) {
                int selectedCount = Integer.parseInt(contactsSelected.getText().toString());
                contactsSelected.setText(String.valueOf(++selectedCount));
                updateSendButton();
            }

            @Override
            public void onContactUnselected(ContactsListFragment.Contact contact) {
                int selectedCount = Integer.parseInt(contactsSelected.getText().toString());
                contactsSelected.setText(String.valueOf(--selectedCount));
                updateSendButton();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO send text invites
            }
        });
    }

    private void setStickersPrize(){
        ParseQuery<ParseObject> queryDefaultStickersPrize = new ParseQuery<>("Defaults");
        queryDefaultStickersPrize.whereEqualTo("key", "stickersPrize");
        queryDefaultStickersPrize.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                String text = object.getString("value");
                //TODO
            }
        });
    }

    private void setReferrals(){
        ParseQuery<ParseObject> referralQuery = new ParseQuery<>(Constants.ActivityClassKey);
        referralQuery.whereEqualTo(Constants.ActivityTypeKey, Constants.ActivityTypeReferral);
        referralQuery.whereEqualTo(Constants.ActivityToUserKey, ParseUser.getCurrentUser());
        referralQuery.countInBackground(new CountCallback() {
            @Override
            public void done(int count, ParseException e) {
                if(e == null) {
                    contactsInvited.setText(String.valueOf(count));

                    if (count >= 20) {
                        setStickersPrize();
                    }

                } else {
                    contactsInvited.setText(String.valueOf(0));
                }
            }
        });
    }

    private void setStickersPromo(){
        ParseQuery<ParseObject> queryDefaultStickers = new ParseQuery<>("Defaults");
        queryDefaultStickers.whereEqualTo("key", "stickersPromo");
        queryDefaultStickers.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                String text = object.getString("value");
                //TODO
            }
        });
    }


    private void setInviteFriendsPromo(){
        ParseQuery<ParseObject> queryDefaultInviteFriendsPromo = new ParseQuery<>("Defaults");
        queryDefaultInviteFriendsPromo.whereEqualTo("key", "inviteFriendsPromo");
        queryDefaultInviteFriendsPromo.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                String text = object.getString("value");
                //TODO
            }
        });
    }

    private void setTshirtPromo(){
        ParseQuery<ParseObject> queryDefaultTshirt = new ParseQuery<>("Defaults");
        queryDefaultTshirt.whereEqualTo("key", "tshirtPromo");
        queryDefaultTshirt.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                String text = object.getString("value");
                //TODO
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setInviteFriendsPromo();
        setStickersPromo();
        setTshirtPromo();
        setReferrals();
        contactsSelected.setText(String.valueOf(0));
    }

    private void updateSendButton(){
        if(!contactsList.getSelectedContacts().isEmpty()){
            sendButton.setVisibility(View.VISIBLE);
        } else {
            sendButton.setVisibility(View.GONE);
        }
    }

}
