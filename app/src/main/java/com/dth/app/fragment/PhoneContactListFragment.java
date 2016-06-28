package com.dth.app.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.FilterQueryProvider;
import android.widget.SectionIndexer;

import com.dth.app.R;

import java.util.ArrayList;

import io.branch.invite.util.BranchInviteUtil;

public class PhoneContactListFragment extends ContactsListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final Uri URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
    private static final String SORT = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC";
    private static final String SELECT = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " CONTAINS ?";
    private static String[] PROJECTION = null;

    static {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            PROJECTION = new String[]{
                    ContactsContract.CommonDataKinds.Phone._ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI};
        } else {
            PROJECTION = new String[]{
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.LOOKUP_KEY,
                    ContactsContract.CommonDataKinds.Phone._ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.TYPE};
        }
    }

    private ContactsAdapter adapter;

    public static PhoneContactListFragment newInstance() {
        return new PhoneContactListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ContactsAdapter(getActivity());
        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                return getActivity().getContentResolver().query(URI, PROJECTION, SELECT, new String[]{constraint.toString() }, SORT);
            }
        });
    }

    @Override
    public void onQueryTextChanged(String query) {

    }

    private static final int REQUEST_CODE_READ_CONTACTS = 5;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setListAdapter(adapter);
        setListShown(false);

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE_READ_CONTACTS);
        } else {
            getLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_READ_CONTACTS && resultCode == Activity.RESULT_OK){
            getLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                URI,
                PROJECTION,
                null,
                null,
                SORT);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        setListShown(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    public Intent getInviteIntent(String referralUrl, ArrayList<String> selectedContacts, String subject, String message) {
        Intent inviteIntent;
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            inviteIntent = new Intent(Intent.ACTION_SENDTO);
            inviteIntent.addCategory(Intent.CATEGORY_DEFAULT);
            inviteIntent.setType("vnd.android-dir/mms-sms");
            inviteIntent.setData(Uri.parse("sms:" + Uri.encode(BranchInviteUtil.formatListToCSV(selectedContacts))));
            inviteIntent.putExtra("sms_body", message + "\n" + referralUrl);
        } else {
            inviteIntent = new Intent(Intent.ACTION_SENDTO);
            inviteIntent.setType("text/plain");
            inviteIntent.putExtra("sms_body", message + "\n" + referralUrl);
            inviteIntent.setData(Uri.parse("smsto:" + Uri.encode(BranchInviteUtil.formatListToCSV(selectedContacts))));

            // In any old version of SMS app checking for subject and text params
            inviteIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
            inviteIntent.putExtra(android.content.Intent.EXTRA_TEXT, message + "\n" + referralUrl);
            inviteIntent.putExtra("address", BranchInviteUtil.formatListToCSV(selectedContacts));

            String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(getContext());
            inviteIntent.setPackage(defaultSmsPackageName);
        }
        return inviteIntent;
    }

    private class ContactsAdapter extends CursorAdapter implements SectionIndexer {
        private LayoutInflater mInflater;
        private AlphabetIndexer mAlphabetIndexer;

        public ContactsAdapter(Context context) {
            super(context, null, 0);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            return mInflater.inflate(R.layout.contact_list_item, viewGroup, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            int nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            String name = cursor.getString(nameIdx);
            int phoneNumberIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            String phoneNumber = cursor.getString(phoneNumberIdx);
            Contact contact = new Contact(name, null, phoneNumber);
            PhoneContactListFragment.this.bindView(contact, view);
        }

        @Override
        public Cursor swapCursor(Cursor newCursor) {
            if (newCursor != null) {
                int nameIdx = newCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                mAlphabetIndexer = new AlphabetIndexer(newCursor, nameIdx, getString(R.string.alphabet));
            }
            return super.swapCursor(newCursor);
        }

        @Override
        public Object[] getSections() {
            return mAlphabetIndexer.getSections();
        }

        @Override
        public int getPositionForSection(int i) {
            if (getCursor() == null) {
                return 0;
            }
            return mAlphabetIndexer.getPositionForSection(i);
        }

        @Override
        public int getSectionForPosition(int i) {
            if (getCursor() == null) {
                return 0;
            }
            return mAlphabetIndexer.getSectionForPosition(i);
        }
    }
}
