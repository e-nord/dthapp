package com.dthapp.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.view.WindowManager;
import android.widget.ImageView;

import com.dthapp.LoginManager;
import com.dthapp.R;
import com.dthapp.User;
import com.dthapp.fragment.AccountFragment;
import com.dthapp.fragment.DTCreateFragment;
import com.dthapp.fragment.DTInviteFragment;
import com.dthapp.fragment.DTListFragment;
import com.dthapp.fragment.IntroFragment;
import com.facebook.appevents.AppEventsLogger;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.parse.ParseFacebookUtils;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarTab;
import com.roughike.bottombar.OnTabClickListener;
import com.squareup.picasso.Picasso;

import de.cketti.mailto.EmailIntentBuilder;

public class MainActivity extends AppCompatActivity {

    private DTCreateFragment createFragment;
    private DTInviteFragment inviteFragment;
    private DTListFragment meFragment;
    private DTListFragment nearbyFragment;
    private AccountFragment accountFragment;
    private BottomBar bottomBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (LoginManager.INSTANCE.isUserLoggedIn()) {
            IntroFragment introFragment = IntroFragment.newInstance();
            showFragment(introFragment);
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Picasso.with(this).load(LoginManager.INSTANCE.getUser().getPictureUrl()).fetch();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount() > 0);
        getSupportFragmentManager().addOnBackStackChangedListener(new android.support.v4.app.FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount() > 0);
            }
        });

        createFragment = DTCreateFragment.newInstance();
        accountFragment = AccountFragment.newInstance();
        nearbyFragment = DTListFragment.newInstance();
        meFragment = DTListFragment.newInstance();
        inviteFragment = DTInviteFragment.newInstance();

        bottomBar = BottomBar.attach(this, savedInstanceState);
        bottomBar.noNavBarGoodness();
        bottomBar.noTabletGoodness();

        BottomBarTab meTab = new BottomBarTab(R.drawable.ic_person_24dp, "Me");
        BottomBarTab dthTab = new BottomBarTab(R.drawable.ic_dth, "DTH");
        BottomBarTab nearbyTab = new BottomBarTab(R.drawable.ic_group_24dp, "Nearby");
        bottomBar.setItems(meTab, dthTab, nearbyTab);

        ViewGroup container = (ViewGroup) bottomBar.findViewById(R.id.bb_bottom_bar_item_container);
        View dthButton = container.getChildAt(1);
        dthButton.findViewById(R.id.bb_bottom_bar_title).setVisibility(View.GONE);
        final ImageView dthIcon = (ImageView) dthButton.findViewById(R.id.bb_bottom_bar_icon);
        dthIcon.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        dthIcon.setColorFilter(null);

        bottomBar.setOnTabClickListener(new OnTabClickListener() {
            @Override
            public void onTabSelected(int position) {
                if (position == 0) {
                    showFragment(meFragment);
                } else if (position == 1) {
                    showFragment(createFragment);
                } else if (position == 2) {
                    showFragment(nearbyFragment);
                }
                dthIcon.setColorFilter(null);
            }

            @Override
            public void onTabReSelected(int position) {
                if (position == 0) {
                    showFragment(meFragment);
                } else if (position == 1) {
                    showFragment(createFragment);
                } else if (position == 2) {
                    showFragment(nearbyFragment);
                }
                dthIcon.setColorFilter(null);
            }

        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 5);
        }
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

    private void showFragment(Fragment fragment, String tag){
        if(!fragment.isVisible()) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_up, 0, 0, R.anim.slide_out_down);
            transaction.replace(R.id.content_main, fragment);
            if (tag != null) {
                transaction.addToBackStack(tag);
            }
            transaction.commit();
        }
    }

    private void showFragment(Fragment fragment){
        showFragment(fragment, null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_account) {
            showFragment(accountFragment, "account");
            return true;
        } else if (item.getItemId() == R.id.action_feedback) {
            launchFeedback(this);
            return true;
        } else if(item.getItemId() == R.id.action_about){
            new LibsBuilder()
                    .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                    .withVersionShown(true)
                    .withLicenseShown(true)
                    .withFields(R.string.class.getFields())
                    .start(this);
        } else if (item.getItemId() == R.id.action_logout) {
            LoginManager.INSTANCE.logout(new LoginManager.LogoutCallback() {
                @Override
                public void onUserLoggedOut(User user) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }

                @Override
                public void onError(Exception e) {
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

    private static void launchFeedback(Activity activity){
        Intent intent = EmailIntentBuilder.from(activity).build();
        //TODO populate fields
        activity.startActivityForResult(intent, 0);
    }

}
