package com.dth.app.fragment;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.HashSet;
import java.util.Set;

public abstract class ContactsListFragment extends ListFragment implements AdapterView.OnItemClickListener {
    private OnContactSelectedListener onContactSelectedListener;
    private Set<Contact> selectedContacts;

    public ContactsListFragment() {}

    public class Contact {
        public String name;

        @Override
        public boolean equals(Object other) {
            return other != null &&
                    other instanceof Contact &&
                    this.name.equals(((Contact) other).name);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedContacts = new HashSet<>();
    }

    public abstract void onQueryTextChanged(String query);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.contact_list_search, container, false);
//        View view = super.onCreateView(inflater, container, savedInstanceState);
//        layout.addView(view);
//        EditText search = (EditText) layout.findViewById(R.id.contact_list_search);
//        search.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                onQueryTextChanged(s.toString());
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
//        return layout;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setOnItemClickListener(this);
        getListView().setFastScrollEnabled(true);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    public void setOnContactSelectedListener(OnContactSelectedListener onContactSelectedListener) {
        this.onContactSelectedListener = onContactSelectedListener;
    }

    public Set<Contact> getSelectedContacts() {
        return selectedContacts;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        Contact contact = (Contact) v.getTag();
        onContactSelectedListener.onContactSelected(contact);
        if (selectedContacts.contains(contact)) {
            selectedContacts.remove(contact);
            getListView().setItemChecked(position, false);
        } else {
            selectedContacts.add(contact);
            getListView().setItemChecked(position, true);
        }
    }

    public interface OnContactSelectedListener {
        void onContactSelected(Contact contact);
    }

}