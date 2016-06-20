package com.dth.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dth.app.R;

import butterknife.ButterKnife;

public class FriendInviteFragment extends Fragment {

    public static FriendInviteFragment newInstance() {
        return new FriendInviteFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.contacts_invite_fragment, container, false);
        ButterKnife.bind(this, v);
        return v;
    }
}
