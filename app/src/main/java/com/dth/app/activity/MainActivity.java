package com.dth.app.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
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
import com.dth.app.fragment.AccountFragment;
import com.dth.app.fragment.EventCreateFragment;
import com.dth.app.fragment.EventInviteFragment;
import com.dth.app.fragment.HomeFragment;
import com.dth.app.fragment.NearbyFragment;
import com.facebook.appevents.AppEventsLogger;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarTab;
import com.roughike.bottombar.OnTabClickListener;

import de.cketti.mailto.EmailIntentBuilder;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    private EventCreateFragment createFragment;
    private EventInviteFragment inviteFragment;
    private HomeFragment homeFragment;
    private NearbyFragment nearbyFragment;
    private AccountFragment accountFragment;
    private BottomBar bottomBar;

    private static void launchFeedback(Activity activity) {
        Intent intent = EmailIntentBuilder.from(activity).build();
        //TODO populate fields
        activity.startActivityForResult(intent, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!LoginManager.INSTANCE.isUserLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            //TODO url parsing and showing event details
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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

        createFragment = EventCreateFragment.newInstance();
        accountFragment = AccountFragment.newInstance();
        nearbyFragment = NearbyFragment.newInstance();
        homeFragment = HomeFragment.newInstance();
        inviteFragment = EventInviteFragment.newInstance();

        bottomBar = BottomBar.attach(this, savedInstanceState);
        bottomBar.noNavBarGoodness();
        bottomBar.noTabletGoodness();

        BottomBarTab meTab = new BottomBarTab(R.drawable.ic_person_24dp, "Me");
        BottomBarTab dthTab = new BottomBarTab(R.mipmap.ic_launcher, "DTH");
        BottomBarTab nearbyTab = new BottomBarTab(R.drawable.ic_group_24dp, "Nearby");
        bottomBar.setItems(meTab, dthTab, nearbyTab);

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
                if(!LoginManager.INSTANCE.isFirstLogin()) {
                    if (position == 0) {
                        showFragment(homeFragment);
                    } else if (position == 1) {
                        showFragment(createFragment);
                    } else if (position == 2) {
                        showFragment(nearbyFragment);
                    }
                    dthIcon.setColorFilter(null);
                }
            }

            @Override
            public void onTabReSelected(int position) {
                if(!LoginManager.INSTANCE.isFirstLogin()) {
                    if (position == 0) {
                        showFragment(homeFragment);
                    } else if (position == 1) {
                        showFragment(createFragment);
                    } else if (position == 2) {
                        showFragment(nearbyFragment);
                    }
                    dthIcon.setColorFilter(null);
                }
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 5);
        }

        //FIXME
//        if (LoginManager.INSTANCE.isFirstLogin()) {
//            LoginManager.INSTANCE.resetFirstLogin();
//            IntroFragment introFragment = IntroFragment.newInstance();
//            showFragment(introFragment);
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

    private void showFragment(Fragment fragment, String tag) {
        if (!fragment.isVisible()) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_up, 0, 0, R.anim.slide_out_down);
            transaction.replace(R.id.content_main, fragment);
            if (tag != null) {
                transaction.addToBackStack(tag);
            }
            transaction.commit();
        }
    }

    private void showFragment(Fragment fragment) {
        showFragment(fragment, null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_account) {
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

    public void displayUserAccount(ParseUser user){
        accountFragment.setUser(ParseUser.getCurrentUser());
        showFragment(accountFragment, "account");
    }

}
