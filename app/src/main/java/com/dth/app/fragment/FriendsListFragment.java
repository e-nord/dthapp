package com.dth.app.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.dth.app.Constants;
import com.dth.app.R;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.orhanobut.hawk.Hawk;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FriendsListFragment extends ContactsListFragment {

    private ParseQueryAdapter<ParseUser> adapter;

    public class FacebookContact extends Contact {
        public ParseUser user;

        public FacebookContact(ParseUser user) {
            this.user = user;
        }

        @Override
        public boolean equals(Object other) {
            return other != null &&
                    other instanceof FacebookContact &&
                    this.user.equals(((FacebookContact) other).user);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ParseQueryAdapter.QueryFactory<ParseUser> factory = new ParseQueryAdapter.QueryFactory<ParseUser>() {
            @Override
            public ParseQuery<ParseUser> create() {
                ParseQuery<ParseUser> query = ParseUser.getQuery();
                // Use cached facebook friend ids
                List<String> facebookFriendIds =  Hawk.get(Constants.UserFacebookFriendsKey);

                // Query for all friends you have on facebook and who are using the app
                query.whereContainedIn(Constants.UserFacebookIDKey, facebookFriendIds);
                query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);
                query.orderByAscending(Constants.UserDisplayNameKey);
                return query;
            }
        };
        adapter = new ParseQueryAdapter<ParseUser>(getActivity(), factory, R.layout.contact_list_item){
            @Override
            public View getItemView(ParseUser user, View v, ViewGroup parent) {
                View view = super.getItemView(user, v, parent);
                bindView(user, view);
                return view;
            }
        };
        adapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<ParseUser>() {
            public void onLoading() {
                setListShown(false);
            }

            @Override
            public void onLoaded(List<ParseUser> objects, Exception e) {
                if(isAdded() && isResumed()) {
                    setListShown(true);
                    if (objects != null) {
                        Toast.makeText(getActivity(), "Loaded " + objects.size() + " events!", Toast.LENGTH_LONG).show();
                    } else if (e != null) {
                        Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        adapter.setAutoload(false);
    }

    private void bindView(ParseUser user, View view) {
        Contact contact = new FacebookContact(user);
        view.setTag(view);
        TextView displayName = (TextView) view.findViewById(R.id.contact_list_item_name);
        CircularImageView icon = (CircularImageView) view.findViewById(R.id.contact_list_item_pic);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.contact_list_item_check);
        displayName.setText(user.getString(Constants.UserDisplayNameKey));
        Picasso.with(getContext()).load(user.getParseFile(Constants.UserProfilePicSmallKey).getUrl()).into(icon);
        checkBox.setChecked(getSelectedContacts().contains(contact));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListAdapter(adapter);
        adapter.loadObjects();
    }

    public static FriendsListFragment newInstance() {
        return new FriendsListFragment();
    }
}
