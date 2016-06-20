package com.dth.app.fragment;

import android.app.AlarmManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dth.app.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class EventCreateFragment extends Fragment {

    @Bind(R.id.dt_create_spinner)
    AppCompatSpinner spinner;
    @Bind(R.id.dt_create_title)
    TextView title;
    @Bind(R.id.dt_create_edit)
    EditText edit;
    @Bind(R.id.dt_create_next_button)
    Button next;
    @Bind(R.id.dt_create_suggestions_container)
    ViewGroup suggestionsContainer;

    public static EventCreateFragment newInstance() {
        return new EventCreateFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dt_create_view, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public void getDefaultSuggestions(){
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Defaults");
        query.whereEqualTo("type", "defaultDT");
        query.orderByAscending("order");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(objects != null){

                } else if(e != null){

                }
            }
        });
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        KeyboardVisibilityEvent.setEventListener(getActivity(), new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean isOpen) {
                if(isOpen){
                    suggestionsContainer.setVisibility(View.VISIBLE);
                } else {
                    suggestionsContainer.setVisibility(View.GONE);
                }
            }
        });

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
        items.add(new TimePeriod("15 minutes", AlarmManager.INTERVAL_HOUR / 4));
        items.add(new TimePeriod("1 hour", AlarmManager.INTERVAL_HOUR));
        items.add(new TimePeriod("3 hours", AlarmManager.INTERVAL_HOUR * 3));
        items.add(new TimePeriod("1 day", AlarmManager.INTERVAL_DAY));
        items.add(new TimePeriod("5 days", AlarmManager.INTERVAL_DAY * 5));
        spinner.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.spinner_item, R.id.spinner_text, items));

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().
                        beginTransaction().
                        setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right).
                        addToBackStack("invite").
                        add(R.id.activity_main_content_main, EventInviteFragment.newInstance()).
                        commit();
            }
        });
    }

    private void updateButton(String input) {
        next.setVisibility(input.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void updateTitle(String input) {
        String firstChar;
        if (input.isEmpty()) {
            firstChar = "";
        } else {
            int codepoint = input.codePointAt(0);
            char[] ch = Character.toChars(codepoint);
            firstChar = new String(ch).toUpperCase();
        }
        title.setText(String.format(getString(R.string.DT), firstChar));
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

}
