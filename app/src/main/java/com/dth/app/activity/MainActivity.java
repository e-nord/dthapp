package com.dth.app.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.dth.app.LoginManager;
import com.dth.app.R;
import com.dth.app.fragment.UserFragment;
import com.dth.app.fragment.ContactsInviteFragment;
import com.dth.app.fragment.EventCreateFragment;
import com.dth.app.fragment.EventDetailFragment;
import com.dth.app.fragment.EventListFragment;
import com.dth.app.fragment.HomeFragment;
import com.dth.app.fragment.NearbyFragment;
import com.facebook.appevents.AppEventsLogger;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarTab;
import com.roughike.bottombar.OnTabClickListener;

import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.cketti.mailto.EmailIntentBuilder;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    private BottomBar bottomBar;

    @Bind(R.id.activity_main_section_pager)
    ViewPager sectionPager;
    private EventListFragment.OnUserSelectedListener userListener;

    private static void launchFeedback(Activity activity) {
        Intent intent = EmailIntentBuilder.from(activity).build();
        //TODO populate fields
        activity.startActivityForResult(intent, 0);
    }

    private class SectionFragmentAdapter extends FragmentStatePagerAdapter {

        private Fragment[] fragments;

        public SectionFragmentAdapter(FragmentManager fm, Fragment... fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!LoginManager.INSTANCE.isUserLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Branch branch = Branch.getInstance();

        branch.initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                if (error == null) {
                    // params are the deep linked params associated with the link that the user clicked -> was re-directed to this app
                    // params will be empty if no data found
                    // ... insert custom logic here ...
                } else {
                    Timber.e("Branch initialization error: %s", error.getMessage());
                }
            }
        }, getIntent().getData(), this);

        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            //TODO url parsing and showing event details
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount() > 0);
        getSupportFragmentManager().addOnBackStackChangedListener(new android.support.v4.app.FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount() > 0);
            }
        });

        ButterKnife.bind(this);

        EventListFragment.OnEventSelectedListener eventListener = new EventListFragment.OnEventSelectedListener() {
            @Override
            public void onEventSelected(ParseObject activity) {
                EventDetailFragment fragment = EventDetailFragment.newInstance(activity.getObjectId());
                fragment.setOnUserSelectedListener(userListener);
                showFragment(fragment, "detail", true);
            }
        };

        userListener = new EventListFragment.OnUserSelectedListener() {
            @Override
            public void onUserSelected(ParseUser user) {
                displayUserAccount(user);
            }
        };

        HomeFragment homeFragment = HomeFragment.newInstance();
        homeFragment.setOnEventSelectedListener(eventListener);
        homeFragment.setOnUserSelectedListener(userListener);

        EventCreateFragment createFragment = EventCreateFragment.newInstance();
        createFragment.setOnEventCreatedListener(new EventCreateFragment.OnEventCreatedListener() {
            @Override
            public void onEventCreated(ParseObject event) {
                sectionPager.setCurrentItem(0);
            }

            @Override
            public void onEventCreationFailure(Exception e) {

            }
        });

        NearbyFragment nearbyFragment = NearbyFragment.newInstance();
        nearbyFragment.setOnEventSelectedListener(eventListener);
        nearbyFragment.setOnUserSelectedListener(userListener);

        SectionFragmentAdapter sectionAdapter = new SectionFragmentAdapter(getSupportFragmentManager(), homeFragment, createFragment, nearbyFragment);
        sectionPager.setOffscreenPageLimit(2);
        sectionPager.setAdapter(sectionAdapter);
        sectionPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                bottomBar.selectTabAtPosition(position, true);
            }
        });

        bottomBar = BottomBar.attach(sectionPager, savedInstanceState);
        bottomBar.noNavBarGoodness();
        bottomBar.noTabletGoodness();

        BottomBarTab meTab = new BottomBarTab(R.drawable.ic_person_24dp, "Me");
        BottomBarTab dthTab = new BottomBarTab(R.mipmap.ic_launcher, "DTH");
        BottomBarTab nearbyTab = new BottomBarTab(R.drawable.ic_group_24dp, "Nearby");
        bottomBar.setItems(meTab, dthTab, nearbyTab);
        bottomBar.setDefaultTabPosition(0);

        ViewGroup container = (ViewGroup) bottomBar.findViewById(R.id.bb_bottom_bar_item_container);
        container.getChildAt(0).getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        View dthButton = container.getChildAt(1);
        dthButton.findViewById(R.id.bb_bottom_bar_title).setVisibility(View.GONE);
        final ImageView dthIcon = (ImageView) dthButton.findViewById(R.id.bb_bottom_bar_icon);
        dthIcon.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        dthIcon.setColorFilter(null);
        container.getChildAt(2).getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;

        bottomBar.setOnTabClickListener(new OnTabClickListener() {
            @Override
            public void onTabSelected(int position) {
                sectionPager.setCurrentItem(position);
                dthIcon.setColorFilter(null);
            }

            @Override
            public void onTabReSelected(int position) {
                sectionPager.setCurrentItem(position);
                dthIcon.setColorFilter(null);
            }
        });

//        if (LoginManager.INSTANCE.isFirstLogin()) {
//            new HintCase(getWindow().getDecorView()).setTarget(dthIcon, true).show();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        getSupportFragmentManager().popBackStack();
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        bottomBar.onSaveInstanceState(outState);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void showFragment(Fragment fragment, String tag, boolean slideInFromRight) {
        if (!fragment.isVisible()) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (slideInFromRight) {
                transaction.setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right);
            } else {
                transaction.setCustomAnimations(R.anim.slide_in_up, 0, 0, R.anim.slide_out_down);
            }
            if (!fragment.isAdded()) {
                transaction.add(R.id.activity_main_content_main, fragment);
            } else {
                transaction.show(fragment);
            }
            if (tag != null) {
                transaction.addToBackStack(tag);
            }
            transaction.commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_invite) {
            ContactsInviteFragment fragment = ContactsInviteFragment.newInstance();
            showFragment(fragment, "friendinvite", false);
            return true;
        } else if (item.getItemId() == R.id.action_account) {
            displayUserAccount(ParseUser.getCurrentUser());
            return true;
        } else if (item.getItemId() == R.id.action_feedback) {
            launchFeedback(this);
            return true;
        } else if (item.getItemId() == R.id.action_about) {
            new LibsBuilder()
                    .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                    .withVersionShown(true)
                    .withLicenseShown(true)
                    .withFields(R.string.class.getFields())
                    .start(this);
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            LoginManager.INSTANCE.logout(new LoginManager.LogoutCallback() {
                @Override
                public void onUserLoggedOut(ParseUser user) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }

                @Override
                public void onError(Exception e) {
                    Timber.e(e, "Login error");
                    Toast.makeText(MainActivity.this, "Logout failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    //TODO handle error
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onNewIntent(Intent intent) {
        this.setIntent(intent);
    }

    public void displayUserAccount(ParseUser user) {
        UserFragment fragment = UserFragment.newInstance(user.getObjectId());
        showFragment(fragment, "account", true);
    }

}
