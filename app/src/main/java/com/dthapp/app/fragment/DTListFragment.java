package com.dthapp.app.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dthapp.app.DT;
import com.dthapp.R;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

public class DTListFragment extends SwipeRefreshListFragment {

    public static DTListFragment newInstance() {
        return new DTListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
//        ParseQueryAdapter<DT> adapter = new ParseQueryAdapter<>(getActivity(), "DT");
//        adapter.setTextKey("text");
//        setListAdapter(adapter);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setVerticalScrollBarEnabled(false);
        getListView().setDrawSelectorOnTop(true);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getActivity().getSupportFragmentManager().
                        beginTransaction().
                        setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right).
                        addToBackStack("detail").
                        replace(R.id.content_main, DTDetailFragment.newInstance()).
                        commit();
            }
        });
        setListAdapter(new MockAdapter(getContext()));
        setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setRefreshing(false);
                    }
                }, 5000);
            }
        });
    }

    private static class MockAdapter extends BaseAdapter {

        private static final DT[] ITEMS = new DT[]{
                new DT("DTH", "Mr Sparkles", "https://scontent.fsjc1-3.fna.fbcdn.net/hprofile-xtp1/v/t1.0-1/p160x160/12195829_10153400423048323_4834901019021292429_n.jpg?oh=3d8194c422d12e073bc9f1b67c5ccbc2&oe=57984008", System.currentTimeMillis() + 30000, false),
                new DT("DTH", "Mr Sparkles", "https://scontent.fsjc1-3.fna.fbcdn.net/hprofile-xtp1/v/t1.0-1/p160x160/12195829_10153400423048323_4834901019021292429_n.jpg?oh=3d8194c422d12e073bc9f1b67c5ccbc2&oe=57984008", System.currentTimeMillis() + 3000 * 60, false),
                new DT("DTH", "Mr Sparkles", "https://scontent.fsjc1-3.fna.fbcdn.net/hprofile-xtp1/v/t1.0-1/p160x160/12195829_10153400423048323_4834901019021292429_n.jpg?oh=3d8194c422d12e073bc9f1b67c5ccbc2&oe=57984008", System.currentTimeMillis() + 1000 * 60 * 60, false),
                new DT("DTH", "Mr Sparkles", "https://scontent.fsjc1-3.fna.fbcdn.net/hprofile-xtp1/v/t1.0-1/p160x160/12195829_10153400423048323_4834901019021292429_n.jpg?oh=3d8194c422d12e073bc9f1b67c5ccbc2&oe=57984008", System.currentTimeMillis() + 3000 * 60, true),
                new DT("DTH", "Mr Sparkles", "https://scontent.fsjc1-3.fna.fbcdn.net/hprofile-xtp1/v/t1.0-1/p160x160/12195829_10153400423048323_4834901019021292429_n.jpg?oh=3d8194c422d12e073bc9f1b67c5ccbc2&oe=57984008", System.currentTimeMillis() + 5000 * 60, true),
                new DT("DTH", "Mr Sparkles", "https://scontent.fsjc1-3.fna.fbcdn.net/hprofile-xtp1/v/t1.0-1/p160x160/12195829_10153400423048323_4834901019021292429_n.jpg?oh=3d8194c422d12e073bc9f1b67c5ccbc2&oe=57984008", System.currentTimeMillis() + 5000 * 60, false),
                new DT("DTH", "Mr Sparkles", "https://scontent.fsjc1-3.fna.fbcdn.net/hprofile-xtp1/v/t1.0-1/p160x160/12195829_10153400423048323_4834901019021292429_n.jpg?oh=3d8194c422d12e073bc9f1b67c5ccbc2&oe=57984008", System.currentTimeMillis(), false),
                new DT("DTH", "Mr Sparkles", "https://scontent.fsjc1-3.fna.fbcdn.net/hprofile-xtp1/v/t1.0-1/p160x160/12195829_10153400423048323_4834901019021292429_n.jpg?oh=3d8194c422d12e073bc9f1b67c5ccbc2&oe=57984008", System.currentTimeMillis() - 3000, false),
                new DT("DTH", "Mr Sparkles", "https://scontent.fsjc1-3.fna.fbcdn.net/hprofile-xtp1/v/t1.0-1/p160x160/12195829_10153400423048323_4834901019021292429_n.jpg?oh=3d8194c422d12e073bc9f1b67c5ccbc2&oe=57984008", System.currentTimeMillis() - 1000 * 60, false),
                new DT("DTH", "Mr Sparkles", "https://scontent.fsjc1-3.fna.fbcdn.net/hprofile-xtp1/v/t1.0-1/p160x160/12195829_10153400423048323_4834901019021292429_n.jpg?oh=3d8194c422d12e073bc9f1b67c5ccbc2&oe=57984008", System.currentTimeMillis() + 8500 * 60 * 60, false),
        };
        private final Context context;

        public MockAdapter(Context context) {
            this.context = context;
        }

        public static String getTimeRemainingString(long timeLeftMs) {
            long days = TimeUnit.MILLISECONDS.toDays(timeLeftMs);
            timeLeftMs -= TimeUnit.DAYS.toMillis(days);

            long hours = TimeUnit.MILLISECONDS.toHours(timeLeftMs);
            timeLeftMs -= TimeUnit.HOURS.toMillis(hours);

            long minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeftMs);
            timeLeftMs -= TimeUnit.MINUTES.toMillis(minutes);

            long seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeftMs);

            if(days > 0){
                return days + " days";
            }

            if(hours > 0){
                return hours + " hours";
            }

            if(minutes > 0){
                return minutes + " minutes";
            }

            return seconds + " seconds";

        }

        @Override
        public int getCount() {
            return ITEMS.length;
        }

        @Override
        public Object getItem(int position) {
            return ITEMS[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.dt_list_item, parent, false);
            }

            DT event = (DT) getItem(position);
            TextView name = (TextView) convertView.findViewById(R.id.dt_name);
            TextView hostname = (TextView) convertView.findViewById(R.id.dt_hostname);
            ImageView pic = (ImageView) convertView.findViewById(R.id.dt_profile_pic);
            TextView status = (TextView) convertView.findViewById(R.id.dt_status_text);
            View statusColor = convertView.findViewById(R.id.dt_status_color);

            name.setText(event.getName() + " - ");
            hostname.setText(event.getHostname());
            Picasso.with(context).load(event.getHostProfilePic()).into(pic);

            int textColor;
            int color;
            if(event.isUserDown()){
                color = ContextCompat.getColor(context, R.color.colorPrimary);
                textColor = color;
                status.setText(R.string.down);
            } else {
                long now = System.currentTimeMillis();
                long timeLeftMs = event.getFinishTimeUTC() - now;
                if (timeLeftMs <= 0) {
                    status.setText(R.string.finished);
                    color = Color.LTGRAY;
                    textColor = color;
                } else {
                    String timeLeft = getTimeRemainingString(timeLeftMs);
                    status.setText(timeLeft + " left");
                    textColor = Color.BLACK;
                    color = Color.WHITE;
                }
            }

            status.setTextColor(textColor);
            statusColor.setBackgroundColor(color);
            return convertView;
        }
    }
}
