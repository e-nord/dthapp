package com.dth.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dth.app.Constants;
import com.dth.app.R;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class EventInviteFragment extends Fragment {

    private ContactsListFragment contactsList;
    private ContactsListFragment friendsList;
    private FloatingActionButton sendButton;

    private OnUsersInvitedListener inviteListener;

    public static EventInviteFragment newInstance() {
        return new EventInviteFragment();
    }

    public void setOnUsersInvitedListener(OnUsersInvitedListener listener) {
        this.inviteListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        ContactsFragmentPagerAdapter adapter = new ContactsFragmentPagerAdapter(getChildFragmentManager());
        View view = inflater.inflate(R.layout.event_invite, container, false);

        final ContactsListFragment.OnContactListener listener = new ContactsListFragment.OnContactListener() {
            @Override
            public void onContactSelected(ContactsListFragment.Contact contact) {
                updateSendButton();
            }

            @Override
            public void onContactUnselected(ContactsListFragment.Contact contact) {
                updateSendButton();
            }
        };
        contactsList = PhoneContactListFragment.newInstance();
        contactsList.setContactListener(listener);
        friendsList = FriendsListFragment.newInstance();
        friendsList.setContactListener(listener);
        adapter.addFragment(friendsList, getActivity().getString(R.string.friends));
        adapter.addFragment(contactsList, getActivity().getString(R.string.contacts));

        ViewPager pager = (ViewPager) view.findViewById(R.id.contacts_pager);
        pager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.contacts_tabs);
        tabLayout.setupWithViewPager(pager);

        sendButton = (FloatingActionButton) view.findViewById(R.id.contacts_send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<ParseUser> users = new LinkedList<>();
                final List<ContactsListFragment.Contact> contacts = new LinkedList<>();
                boolean isPublic = false;
                for (ContactsListFragment.Contact contact : friendsList.getSelectedContacts()) {
                    if (contact.getUser() != null) {
                        if (contact.getUser().getString(Constants.UserDisplayNameKey).equals(Constants.DTHNearbyPublicLabel)) {
                            isPublic = true;
                        } else {
                            users.add(contact.getUser());
                        }
                    } else {
                        contacts.add(contact);
                    }
                }
                users.add(ParseUser.getCurrentUser());
                inviteListener.onUsersInvited(users, contacts, isPublic);
            }
        });

        return view;
    }

    private void updateSendButton() {
        if (!friendsList.getSelectedContacts().isEmpty() || !contactsList.getSelectedContacts().isEmpty()) {
            sendButton.setVisibility(View.VISIBLE);
        } else {
            sendButton.setVisibility(View.GONE);
        }
    }

    public interface OnUsersInvitedListener {
        void onUsersInvited(List<ParseUser> users, List<ContactsListFragment.Contact> contacts, boolean isPublic);
    }

    private static final class ContactsFragmentPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragments = new ArrayList<>();
        private final List<String> sectionTitles = new ArrayList<>();

        public ContactsFragmentPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            sectionTitles.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return sectionTitles.get(position);
        }
    }
}
