package com.dth.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import com.dth.app.Constants;
import com.dth.app.R;
import com.orhanobut.hawk.Hawk;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ParseQueryAdapter.QueryFactory<ParseUser> factory = new ParseQueryAdapter.QueryFactory<ParseUser>() {
            @Override
            public ParseQuery<ParseUser> create() {
                ParseQuery<ParseUser> friendsQuery = ParseUser.getQuery();
                // Use cached facebook friend ids
                List<String> facebookFriendIds =  Hawk.get(Constants.UserFacebookFriendsKey);

                // Query for all friends you have on facebook and who are using the app
                friendsQuery.whereContainedIn(Constants.UserFacebookIDKey, facebookFriendIds);
                friendsQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
                friendsQuery.orderByAscending(Constants.UserDisplayNameKey);
                friendsQuery.setLimit(1000);
                return friendsQuery;
            }
        };
        adapter = new FriendsAdapter(getActivity(), factory, R.layout.contact_list_item){
            @Override
            public View getItemView(ParseUser user, View v, ViewGroup parent) {
                View view = super.getItemView(user, v, parent);
                Contact contact = new Contact(user.getString(Constants.UserDisplayNameKey),
                        user.getParseFile(Constants.UserProfilePicSmallKey).getUrl(), null);
                bindView(contact, view);
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

    @Override
    protected void onContactSelected(Contact contact) {

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
