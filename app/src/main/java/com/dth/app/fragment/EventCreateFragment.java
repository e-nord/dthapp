package com.dth.app.fragment;

import android.app.AlarmManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.dth.app.R;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.branch.invite.TabbedInviteBuilder;

public class EventCreateFragment extends Fragment {

    @Bind(R.id.dt_create_spinner)
    AppCompatSpinner spinner;
    @Bind(R.id.dt_create_title)
    TextView title;
    @Bind(R.id.dt_create_edit)
    TextInputEditText edit;
    @Bind(R.id.dt_create_next_button)
    Button next;

    public static EventCreateFragment newInstance() {
        return new EventCreateFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dt_create_view, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String text = edit.getText().toString();
        updateTitle(text);
        updateButton(text);

        edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTitle(s.toString());
                updateButton(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        List<TimePeriod> items = new LinkedList<>();
        items.add(new TimePeriod("15 minutes", AlarmManager.INTERVAL_HOUR/4));
        items.add(new TimePeriod("1 hour", AlarmManager.INTERVAL_HOUR));
        items.add(new TimePeriod("3 hours", AlarmManager.INTERVAL_HOUR*3));
        items.add(new TimePeriod("1 day", AlarmManager.INTERVAL_DAY));
        items.add(new TimePeriod("5 days", AlarmManager.INTERVAL_DAY*5));
        spinner.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.spinner_item, R.id.spinner_text, items));

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TabbedInviteBuilder(getActivity(), "Inviting userID", "Inviting user Name")
                        .setPhoneTabText(R.string.contacts)
                        //.setTabStyle(getActivity().getResources().getDrawable(R.drawable.tab_on), getActivity().getResources().getDrawable(R.drawable.tab_off))
                        .setPositiveButtonStyle(new ColorDrawable(Color.TRANSPARENT),"Invite", Color.BLUE)
                        .setNegativeButtonStyle(new ColorDrawable(Color.TRANSPARENT),"Close", Color.MAGENTA)
                        .setInviterImageUrl("https://s3-us-west-1.amazonaws.com/branchhost/mosaic_og.png")
                        .setInvitationText("Invitation Title", "Invitation Message")
                        .setPhoneTabText("Message")
                        .setEmailTabText("E-mail")
                        //.setTitle("Invite a friend")
                        .setSelectedItemColor(Color.parseColor("#FF0000FF"))
                        .addCustomParams("Custom_Param", "This is a custom param")
                        .create().show();
                getActivity().getSupportFragmentManager().
                        beginTransaction().
                        setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right).
                        addToBackStack("invite").
                        add(R.id.content_main, EventInviteFragment.newInstance()).
                        commit();
            }
        });
    }

    private class TimePeriod {
        private final String text;
        private final long lengthMs;

        private TimePeriod(String text, long lengthMs) {
            this.text = text;
            this.lengthMs = lengthMs;
        }

        public String getText() {
            return text;
        }

        public long getLengthMs() {
            return lengthMs;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private void updateButton(String input){
        next.setVisibility(input.isEmpty() ? View.INVISIBLE : View.VISIBLE);
    }

    private void updateTitle(String input) {
        String firstChar;
        if (input.isEmpty()) {
            firstChar = "_";

        } else {
            firstChar = String.valueOf(input.charAt(0));
        }
        title.setText(String.format(getString(R.string.DT), firstChar.toUpperCase(Locale.getDefault())));
    }
}
