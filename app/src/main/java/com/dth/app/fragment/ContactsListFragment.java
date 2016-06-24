package com.dth.app.fragment;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.dth.app.R;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.HashSet;
import java.util.Set;

public abstract class ContactsListFragment extends ListFragment implements AdapterView.OnItemClickListener {
    private OnContactSelectedListener onContactSelectedListener;
    private Set<Contact> selectedContacts;

    public ContactsListFragment() {}

    public class Contact {
        private final String name;
        private final String profilePicUrl;
        private final String phoneNumber;

        public Contact(String name, String profilePicUrl, String phoneNumber) {
            this.name = name;
            this.profilePicUrl = profilePicUrl;
            this.phoneNumber = phoneNumber;
        }

        public String getName() {
            return name;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public String getProfilePicUrl() {
            return profilePicUrl;
        }

        @Override
        public boolean equals(Object other) {
            return other != null &&
                    other instanceof Contact &&
                    this.name.equals(((Contact) other).name);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedContacts = new HashSet<>();
    }

    protected void onContactSelected(Contact contact){

    }

    protected void onContactUnselected(Contact contact){

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

    protected void bindView(Contact contact, View view){
        TextView displayName = (TextView) view.findViewById(R.id.contact_list_item_name);
        TextView phoneNumberLabel = (TextView) view.findViewById(R.id.contact_list_item_number);
        CircularImageView icon = (CircularImageView) view.findViewById(R.id.contact_list_item_pic);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.contact_list_item_check);
        displayName.setText(contact.getName());
        if (contact.getProfilePicUrl() != null) {
            Picasso.with(getContext()).load(contact.getProfilePicUrl()).into(icon);
        }
        if(contact.getPhoneNumber() != null){
            phoneNumberLabel.setText(PhoneNumberUtils.formatNumber(contact.phoneNumber));
            phoneNumberLabel.setVisibility(View.VISIBLE);
        } else {
            phoneNumberLabel.setVisibility(View.GONE);
        }
        checkBox.setChecked(getSelectedContacts().contains(contact));
        view.setTag(contact);
    }

    public void setOnContactSelectedListener(OnContactSelectedListener onContactSelectedListener) {
        this.onContactSelectedListener = onContactSelectedListener;
    }

    public Set<Contact> getSelectedContacts() {
        return selectedContacts;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        handleClick(v);
    }

    private void handleClick(View v){
        Contact contact = (Contact) v.getTag();
        if (selectedContacts.contains(contact)) {
            selectedContacts.remove(contact);
            ((CheckBox)v.findViewById(R.id.contact_list_item_check)).setChecked(false);
            if(onContactSelectedListener != null) {
                onContactSelectedListener.onContactUnselected(contact);
            }
        } else {
            selectedContacts.add(contact);
            ((CheckBox)v.findViewById(R.id.contact_list_item_check)).setChecked(true);
            if(onContactSelectedListener != null) {
                onContactSelectedListener.onContactSelected(contact);
            }
        }
    }

    public interface OnContactSelectedListener {
        void onContactSelected(Contact contact);
        void onContactUnselected(Contact contact);
    }

}