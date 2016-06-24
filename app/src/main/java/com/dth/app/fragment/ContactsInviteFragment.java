package com.dth.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dth.app.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ContactsInviteFragment extends Fragment {

    @Bind(R.id.account_contacts_invited)
    TextView contactsInvited;

    public static ContactsInviteFragment newInstance() {
        return new ContactsInviteFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.contacts_invite_fragment, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        contactsInvited.setText(String.format(getString(R.string.contacts_invite_promo), 0));
    }
}
