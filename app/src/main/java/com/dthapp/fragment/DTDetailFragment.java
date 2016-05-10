package com.dthapp.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dthapp.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DTDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<String> {

    public static DTDetailFragment newInstance(){
        return new DTDetailFragment();
    }

    @Bind(R.id.dt_detail_finish_in)
    TextView finishIn;

    @Bind(R.id.dt_detail_description)
    TextView description;

    @Bind(R.id.dt_detail_contacts_container)
    LinearLayout contactsContainer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dt_detail_view, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLoaderManager().initLoader(0, null, this).forceLoad();
    }

    @OnClick(R.id.down_button)
    public void onDown(){
        Toast.makeText(getActivity(), "Down", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.not_down_button)
    public void onNotDown(){
        Toast.makeText(getActivity(), "Not down", Toast.LENGTH_SHORT).show();
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<String>(getContext()) {
            @Override
            public String loadInBackground() {
                System.out.println("BACKGROUND LOADING WOOP WOOP");
                return null;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        description.setText("Go kart racing at 6pm meet me at the racetrack");
        finishIn.setText("finishes in 6 days");
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (int i = 0; i < 4; i++) {
           View icon = inflater.inflate(R.layout.dt_detail_contact_view, contactsContainer, false);
            contactsContainer.addView(icon);
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }
}
