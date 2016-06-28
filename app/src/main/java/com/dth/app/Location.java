package com.dth.app;

import android.location.Criteria;

import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import timber.log.Timber;

public class Location {

    private static final int LOCATION_FETCH_TIMEOUT_MS = 30000;

    public interface UserLocationUpdateListener {
        void onUserLocationUpdated(ParseGeoPoint geoPoint);
        void onUserLocationError(Exception e);
    }

    public static ParseGeoPoint getLastGeoPoint(){
        return ParseUser.getCurrentUser().getParseGeoPoint(Constants.CURRENT_LOCATION);
    }

    public static void updateUserLocation(final UserLocationUpdateListener listener){
        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAccuracy(Criteria.ACCURACY_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setBearingRequired(false);

        setMockLocation(listener);

//        ParseGeoPoint.getCurrentLocationInBackground(LOCATION_FETCH_TIMEOUT_MS, criteria, new LocationCallback() {
//            @Override
//            public void done(final ParseGeoPoint geoPoint, ParseException e) {
//                if(e == null){
//                    ParseUser user = ParseUser.getCurrentUser();
//                    user.put(Constants.CURRENT_LOCATION, geoPoint);
//                    user.saveInBackground(new SaveCallback() {
//                        @Override
//                        public void done(ParseException e) {
//                            if(e == null){
//                                listener.onUserLocationUpdated(geoPoint);
//                            } else {
//                                Timber.e(e, "Failed to update user location");
//                                listener.onUserLocationError(e);
//                            }
//                        }
//                    });
//                } else {
//                    Timber.e(e, "Location update error");
//                }
//            }
//        });
    }

    private static void setMockLocation(final UserLocationUpdateListener listener){
        final ParseGeoPoint fakePoint = new ParseGeoPoint(47.6516580, -122.3422210);
        ParseUser user = ParseUser.getCurrentUser();
        user.put(Constants.CURRENT_LOCATION, fakePoint);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    listener.onUserLocationUpdated(fakePoint);
                } else {
                    Timber.e(e, "Failed to update user location");
                    listener.onUserLocationError(e);
                }
            }
        });
    }


}
