package com.dth.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class FriendsListFragment extends ContactsListFragment {

    private FriendsAdapter adapter;

    private static class FriendsAdapter extends ParseQueryAdapter<ParseUser> implements Filterable {

        private final FriendsFilter filter;

        public FriendsAdapter(Context context, QueryFactory<ParseUser> queryFactory, int itemViewResource) {
            super(context, queryFactory, itemViewResource);
            filter = new FriendsFilter();
        }

        @Override
        public Filter getFilter() {
            return filter;
        }

        private class FriendsFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<ParseUser> items = new LinkedList<>();
                for(int i = 0; i < getCount(); i++){
                    items.add(getItem(i));
                }
                if (!TextUtils.isEmpty(constraint)) {
                    Iterator<ParseUser> iterator = items.iterator();
                    while(iterator.hasNext()){
                        if(!iterator.next().getString(Constants.UserDisplayNameKey).toUpperCase().startsWith(constraint.toString().toUpperCase())){
                            iterator.remove();
                        }
                    }
                }
                results.values = items;
                results.count = items.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.count == 0)
                    notifyDataSetInvalidated();
                else {
                    notifyDataSetChanged();
                }
            }
        }
    }

    public class FacebookContact extends Contact {
        public ParseUser user;

        public FacebookContact(ParseUser user) {
            this.user = user;
            this.name = user.getString(Constants.UserDisplayNameKey);
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
                query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
                query.orderByAscending(Constants.UserDisplayNameKey);
                return query;
            }
        };
        adapter = new FriendsAdapter(getActivity(), factory, R.layout.contact_list_item){
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
                        //Toast.makeText(getActivity(), "Loaded " + objects.size() + " events!", Toast.LENGTH_LONG).show();
                    } else if (e != null) {
                        Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        adapter.setAutoload(false);
    }

    @Override
    public void onQueryTextChanged(String query) {
        adapter.getFilter().filter(query);
    }

    private void bindView(ParseUser user, View view) {
        Contact contact = new FacebookContact(user);
        view.setTag(contact);
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
