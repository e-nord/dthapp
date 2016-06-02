package com.dth.app.fragment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AlphabetIndexer;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.dth.app.R;

import java.util.ArrayList;

import io.branch.invite.util.BranchInviteUtil;
import io.branch.invite.util.CircularImageView;

public class ContactsListFragment extends ListFragment implements
        AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private ContactsAdapter mAdapter;
    private OnContactsInteractionListener mOnContactSelectedListener;

    public ContactsListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new ContactsAdapter(getActivity());
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setOnItemClickListener(this);
        getListView().setFastScrollEnabled(true);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        setListAdapter(mAdapter);
    }

    public void setOnContactSelectedListener(OnContactsInteractionListener onContactSelectedListener) {
        mOnContactSelectedListener = onContactSelectedListener;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);
        int phoneNumberIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        String phoneNumber = cursor.getString(phoneNumberIdx);
        mOnContactSelectedListener.onContactSelected(phoneNumber);
    }

    private static final Uri URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
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

    private static final String SORT =  ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC";

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
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public interface OnContactsInteractionListener {
        void onContactSelected(String phoneNumber);
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
            View itemLayout = mInflater.inflate(R.layout.contact_list_item, viewGroup, false);
            ViewHolder holder = new ViewHolder();
            holder.name = (TextView) itemLayout.findViewById(R.id.contact_list_item_name);
            holder.icon = (CircularImageView) itemLayout.findViewById(R.id.contact_list_item_pic);
            itemLayout.setTag(holder);
            return itemLayout;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();
            int nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            String displayName = cursor.getString(nameIdx);
            holder.name.setText(displayName);
        }

        @Override
        public Cursor swapCursor(Cursor newCursor) {
            if(newCursor != null) {
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

        private class ViewHolder {
            TextView name;
            CircularImageView icon;
        }
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
}