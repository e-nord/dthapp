package com.dth.app.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dth.app.LoginManager;
import com.dth.app.R;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LoginActivity extends AppCompatActivity {

    private boolean isResumed;

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isResumed = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Button loginButton = (Button) findViewById(R.id.login_facebook_login_button);
        assert loginButton != null;
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.INSTANCE.login(LoginActivity.this, new LoginManager.LoginCallback() {
                    @Override
                    public void onUserLoggedIn(ParseUser user) {
                        startActivity(new Intent(LoginActivity.this, com.dth.app.activity.MainActivity.class));
                        finish();
                    }

                    @Override
                    public void onError(Exception e) {
                        if(isResumed) {
                            Timber.e(e, "Login error");
                            Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            new MaterialDialog.Builder(getApplication())
                                    .title(R.string.login_error)
                                    .content(R.string.connection_error)
                                    .neutralText(R.string.ok)
                                    .show();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
