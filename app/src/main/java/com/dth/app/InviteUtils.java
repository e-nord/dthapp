package com.dth.app;

import com.parse.FunctionCallback;
import com.parse.ParseACL;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class InviteUtils {

    public static void referContact(final String phoneNumber, final SaveCallback callback){
        final ParseUser currentUser = ParseUser.getCurrentUser();
        sendSMS(phoneNumber, currentUser, new FunctionCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if(e == null) {
                    createReferralForPhoneNumber(phoneNumber, currentUser, callback);
                } else {
                    Timber.e(e, "Failed to refer contact");
                }
            }
        });
    }

    public static void inviteContact(String phoneNumber, FunctionCallback<ParseObject> callback){
        sendSMS(phoneNumber, null, callback);
    }

    private static void sendSMS(final String phoneNumber, ParseUser fromUser, FunctionCallback<ParseObject> callback){
        Map<String,String> params = new HashMap<>();
        params.put("number", phoneNumber);
        if(fromUser != null) {
            params.put("invitingUserName", fromUser.getString(Constants.UserDisplayNameKey));
        }
        ParseCloud.callFunctionInBackground("inviteWithTwilio", params, callback);
    }

    private static void createReferralForPhoneNumber(String phoneNumber, ParseUser currentUser, SaveCallback callback){
        ParseObject referral = new ParseObject(Constants.ActivityClassKey);
        referral.put(Constants.ActivityTypeKey, Constants.ActivityTypeReferral);
        referral.put(Constants.ActivityFromUserKey, currentUser);
        referral.put(Constants.ActivityToUserKey, currentUser);

        // Set the phone number as the content
        referral.put(Constants.ActivityContentKey, phoneNumber);

        ParseACL acl = new ParseACL(currentUser);
        acl.setPublicReadAccess(true);
        referral.setACL(acl);

        referral.saveInBackground(callback);
    }
}
