package com.dthapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.parse.LogInCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public enum LoginManager {
    INSTANCE;

    private static final boolean MOCK_LOGIN_ENABLED = true;
    private static final User MOCK_USER = new User("Mock User", "mockuser@gmail.com",
            "https://scontent.fsjc1-3.fna.fbcdn.net/hprofile-xtp1/v/t1.0-1/p160x160/12195829_10153400423048323_4834901019021292429_n.jpg?oh=3d8194c422d12e073bc9f1b67c5ccbc2&oe=57984008");

    private static final String PROFILE_PIC_URL_KEY = "profile_pic_url";

    private User user;

    public interface LoginCallback {
        void onUserLoggedIn(User user);
        void onError(Exception e);
    }

    public interface LogoutCallback {
        void onUserLoggedOut(User user);
        void onError(Exception e);
    }

    private static final List<String> PERMISSIONS = new ArrayList<String>() {{
        add("public_profile");
        add("email");
        add("user_friends");
    }};

    private void getFacebookUser(final LoginCallback callback) {
        final Bundle parameters = new Bundle();
        parameters.putString("fields", "email,name,picture");

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me",
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        if(response.getError() != null){
                            callback.onError(response.getError().getException());
                        }

                        String name = null;
                        String email = null;
                        String pictureUrl = null;
                        try {
                            email = response.getJSONObject().getString("email");
                            name = response.getJSONObject().getString("name");
                            JSONObject picture = response.getJSONObject().getJSONObject("picture");
                            JSONObject data = picture.getJSONObject("data");
                            pictureUrl = data.getString("url");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callback.onError(e);
                        }

                        ParseUser parseUser = ParseUser.getCurrentUser();
                        parseUser.setUsername(name);
                        parseUser.setEmail(email);
                        parseUser.put(PROFILE_PIC_URL_KEY, pictureUrl);

                        User user = new User(name, email, pictureUrl);
                        setUser(user);
                        callback.onUserLoggedIn(user);
                    }
                }
        ).executeAsync();
    }

    private User getParseUser() {
        ParseUser parseUser = ParseUser.getCurrentUser();
        return new User(parseUser.getUsername(), parseUser.getEmail(), parseUser.getString(PROFILE_PIC_URL_KEY));
    }

    private boolean mockUserIsLoggedIn = false;

    public boolean isUserLoggedIn(){
        if(MOCK_LOGIN_ENABLED) {
            return mockUserIsLoggedIn;
        } else {
            return ParseUser.getCurrentUser().isAuthenticated();
        }
    }

    public User getUser(){
        return user;
    }

    private User setUser(User user){
        this.user = user;
        return this.user;
    }

    public void login(final Activity activity, final LoginCallback callback) {
        if(MOCK_LOGIN_ENABLED) {
            mockUserIsLoggedIn = true;
            user = MOCK_USER;
            callback.onUserLoggedIn(MOCK_USER);
        } else {
            ParseFacebookUtils.logInWithReadPermissionsInBackground(activity, PERMISSIONS, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException err) {
                    if (err != null) {
                        callback.onError(err);
                    } else if (user == null) {
                        Toast.makeText(activity, "Login cancelled!", Toast.LENGTH_LONG).show();
                    } else if (user.isNew()) {
                        getFacebookUser(callback);
                    } else {
                        callback.onUserLoggedIn(setUser(getParseUser()));
                    }
                }
            });
        }
    }

    public void logout(final LogoutCallback callback){
        if(MOCK_LOGIN_ENABLED) {
            mockUserIsLoggedIn = false;
            user = null;
            callback.onUserLoggedOut(MOCK_USER);
        } else {
            final User user = getParseUser();
            ParseUser.logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        callback.onError(e);
                    } else {
                        callback.onUserLoggedOut(user);
                    }
                }
            });
        }
    }
}
