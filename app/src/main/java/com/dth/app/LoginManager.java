package com.dth.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestBatch;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.orhanobut.hawk.Hawk;
import com.parse.LogInCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import timber.log.Timber;

public enum LoginManager {
    INSTANCE;

    private static final List<String> PERMISSIONS = new ArrayList<String>() {{
        add("public_profile");
        add("email");
        add("user_friends");
    }};

    private void getFacebookUser(final Activity activity, final LoginCallback callback) {
        final Bundle profileParameters = new Bundle();
        profileParameters.putString("fields", "email,name,picture.type(large)");
        final Bundle friendsParameters = new Bundle();
        friendsParameters.putString("fields", "id,name,first_name,last_name");
        GraphRequestBatch batch = new GraphRequestBatch(
                new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/friends?limit=5000", friendsParameters, HttpMethod.GET, new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        if (response.getError() != null) {
                            callback.onError(response.getError().getException());
                            return;
                        }
                        try {
                            JSONArray array = response.getJSONObject().getJSONArray("data");
                            List<String> friendIds = new LinkedList<>();
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject o = array.getJSONObject(i);
                                String friendId = o.getString("id");
                                friendIds.add(friendId);
                            }
                            Hawk.put(Constants.UserFacebookFriendsKey, friendIds);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callback.onError(e);
                        }
                    }
                }),
                new GraphRequest(AccessToken.getCurrentAccessToken(), "/me", profileParameters, HttpMethod.GET, new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        if (response.getError() != null) {
                            callback.onError(response.getError().getException());
                            return;
                        }
                        try {
                            String email = response.getJSONObject().getString("email");
                            String name = response.getJSONObject().getString("name");
                            String id = response.getJSONObject().getString("id");
                            JSONObject picture = response.getJSONObject().getJSONObject("picture");
                            JSONObject data = picture.getJSONObject("data");
                            final String pictureUrl = data.getString("url");
                            System.out.println(pictureUrl);

                            final ParseUser parseUser = ParseUser.getCurrentUser();
                            parseUser.setUsername(name);
                            parseUser.setEmail(email);
                            parseUser.put(Constants.UserFacebookIDKey, id);

                            Picasso.with(activity).load(pictureUrl).into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();

                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                    byte[] mediumPicBytes = stream.toByteArray();
                                    ParseFile mediumPicFile = new ParseFile(mediumPicBytes);
                                    parseUser.put(Constants.UserProfilePicMediumKey, mediumPicFile);

                                    stream.reset();

                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                    byte[] smallPicBytes = stream.toByteArray();
                                    ParseFile smallPicFile = new ParseFile(smallPicBytes);
                                    parseUser.put(Constants.UserProfilePicSmallKey, smallPicFile);

                                    parseUser.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if(e == null){
                                                callback.onUserLoggedIn(parseUser);
                                            } else {
                                                callback.onError(e);
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onBitmapFailed(Drawable errorDrawable) {
                                    callback.onError(new IOException("Failed to fetch profile image"));
                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {
                                    Timber.d("Fetching profile images...");
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                            callback.onError(e);
                        }
                    }
                }));
        Timber.d("Requesting Facebook info...");
        batch.executeAsync();
    }

    public boolean isUserLoggedIn() {
        return ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().isAuthenticated();
    }

    public void login(final Activity activity, final LoginCallback callback) {
        Timber.d("Starting login...");
        ParseFacebookUtils.logInWithReadPermissionsInBackground(activity, PERMISSIONS, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                if (err != null) {
                    callback.onError(err);
                } else if (user == null) {
                    Toast.makeText(activity, "Login cancelled!", Toast.LENGTH_LONG).show();
                } else {
                    getFacebookUser(activity, callback);
                    callback.onUserLoggedIn(ParseUser.getCurrentUser());
                }
            }
        });
    }

    public void logout(final LogoutCallback callback) {
        final ParseUser user = ParseUser.getCurrentUser();
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
        Hawk.put("first_login", false);
    }

    public boolean isFirstLogin() {
        return Hawk.get("first_login",  true);
    }

    public void setFirstLogin() {
        Hawk.put("first_login",  false);
    }

    public interface LoginCallback {
        void onUserLoggedIn(ParseUser user);

        void onError(Exception e);
    }

    public interface LogoutCallback {
        void onUserLoggedOut(ParseUser user);

        void onError(Exception e);
    }
}
