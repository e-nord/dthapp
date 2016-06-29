package com.dth.app.fragment;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.dth.app.R;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.HashSet;
import java.util.Set;

public abstract class ContactsListFragment extends ListFragment implements AdapterView.OnItemClickListener {
    private OnContactListener contactListener;
    private Set<Contact> selectedContacts;

    public ContactsListFragment() {
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
        getListView().setSelector(R.drawable.list_selector);
    }

    protected void bindView(Contact contact, View view) {
        TextView displayName = (TextView) view.findViewById(R.id.contact_list_item_name);
        TextView phoneNumberLabel = (TextView) view.findViewById(R.id.contact_list_item_number);
        CircularImageView icon = (CircularImageView) view.findViewById(R.id.contact_list_item_pic);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.contact_list_item_check);
        displayName.setText(contact.getName());
        if (contact.getProfilePicUrl() != null) {
            Picasso.with(getContext()).load(contact.getProfilePicUrl()).into(icon);
        }
        if (contact.getPhoneNumber() != null) {
            try {
                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                Phonenumber.PhoneNumber numberProto = phoneUtil.parse(contact.getPhoneNumber(), "US");
                phoneNumberLabel.setText(phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.NATIONAL));
            } catch (NumberParseException e){
                phoneNumberLabel.setText(contact.getPhoneNumber());
            }
            phoneNumberLabel.setVisibility(View.VISIBLE);
        } else {
            phoneNumberLabel.setVisibility(View.GONE);
        }
        boolean isSelected = getSelectedContacts().contains(contact);
        checkBox.setChecked(isSelected);
        if (isSelected) {
            view.setBackgroundColor(getResources().getColor(R.color.colorLightAccent));
        } else {
            view.setBackgroundColor(getResources().getColor(R.color.white));
        }
        view.setTag(contact);
    }

    public void setContactListener(OnContactListener contactListener) {
        this.contactListener = contactListener;
    }

    public Set<Contact> getSelectedContacts() {
        return selectedContacts;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        handleClick(v);
    }

    private void handleClick(View v) {
        Contact contact = (Contact) v.getTag();
        if (selectedContacts.contains(contact)) {
            selectedContacts.remove(contact);
            ((CheckBox) v.findViewById(R.id.contact_list_item_check)).setChecked(false);
            if (contactListener != null) {
                contactListener.onContactUnselected(contact);
            }
        } else {
            selectedContacts.add(contact);
            ((CheckBox) v.findViewById(R.id.contact_list_item_check)).setChecked(true);
            if (contactListener != null) {
                contactListener.onContactSelected(contact);
            }
        }
    }

    public interface OnContactListener {
        void onContactSelected(Contact contact);

        void onContactUnselected(Contact contact);
    }

    public class Contact {
        private final String name;
        private final String profilePicUrl;
        private final String phoneNumber;
        private ParseUser user;

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

        public ParseUser getUser() {
            return user;
        }

        public void setUser(ParseUser user) {
            this.user = user;
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

}