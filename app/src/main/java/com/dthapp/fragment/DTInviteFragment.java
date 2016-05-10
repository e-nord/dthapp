package com.dthapp.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dthapp.R;

import java.util.ArrayList;
import java.util.List;

public class DTInviteFragment extends Fragment {

    private ContactsListFragment mContactsList;
    private ContactsListFragment mFriendsList;

    public static DTInviteFragment newInstance() {
        return new DTInviteFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ContactsFragmentPagerAdapter adapter = new ContactsFragmentPagerAdapter(getChildFragmentManager());
        View view = inflater.inflate(R.layout.contacts_view, container, false);

        mContactsList = new ContactsListFragment();
        mContactsList.setOnContactSelectedListener(new ContactsListFragment.OnContactsInteractionListener() {
            @Override
            public void onContactSelected(String phoneNumber) {
                Toast.makeText(getActivity(), "Selected contact - " + phoneNumber, Toast.LENGTH_SHORT).show();
            }
        });
        mFriendsList = new ContactsListFragment();
        mFriendsList.setOnContactSelectedListener(new ContactsListFragment.OnContactsInteractionListener() {
            @Override
            public void onContactSelected(String phoneNumber) {
                Toast.makeText(getActivity(), "Selected contact - " + phoneNumber, Toast.LENGTH_SHORT).show();
            }
        });
        adapter.addFragment(mFriendsList, "Friends");
        adapter.addFragment(mContactsList, "Contacts");

        ViewPager pager = (ViewPager) view.findViewById(R.id.contacts_pager);
        pager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.contacts_tabs);
        tabLayout.setupWithViewPager(pager);

        return view;
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
