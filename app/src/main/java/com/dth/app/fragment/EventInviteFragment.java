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
import android.widget.Toast;

import com.dth.app.R;

import java.util.ArrayList;
import java.util.List;

public class EventInviteFragment extends Fragment {

    private ContactsListFragment contactsList;
    private ContactsListFragment friendsList;
    private FloatingActionButton sendButton;

    public static EventInviteFragment newInstance() {
        return new EventInviteFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ContactsFragmentPagerAdapter adapter = new ContactsFragmentPagerAdapter(getChildFragmentManager());
        View view = inflater.inflate(R.layout.contacts_view, container, false);

        sendButton = (FloatingActionButton) view.findViewById(R.id.contacts_send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        ContactsListFragment.OnContactSelectedListener listener = new ContactsListFragment.OnContactSelectedListener() {
            @Override
            public void onContactSelected(ContactsListFragment.Contact contact) {
                Toast.makeText(getActivity(), "Selected contact - " + contact, Toast.LENGTH_SHORT).show();
                updateSendButton();
            }

            @Override
            public void onContactUnselected(ContactsListFragment.Contact contact) {
                updateSendButton();
            }
        };
        contactsList = PhoneContactListFragment.newInstance();
        contactsList.setOnContactSelectedListener(listener);
        friendsList = FriendsListFragment.newInstance();
        friendsList.setOnContactSelectedListener(listener);
        adapter.addFragment(friendsList, getActivity().getString(R.string.friends));
        adapter.addFragment(contactsList, getActivity().getString(R.string.contacts));

        ViewPager pager = (ViewPager) view.findViewById(R.id.contacts_pager);
        pager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.contacts_tabs);
        tabLayout.setupWithViewPager(pager);

        return view;
    }

    private void updateSendButton(){
        if(!friendsList.getSelectedContacts().isEmpty() || !contactsList.getSelectedContacts().isEmpty()){
            sendButton.setVisibility(View.VISIBLE);
        } else {
            sendButton.setVisibility(View.GONE);
        }
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
