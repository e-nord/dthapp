package com.dth.app;

public class Constants {

    public static final String UserDefaultsActivityFeedViewControllerLastRefreshKey = "com.timstrother.dth.userDefaults.activityFeedViewController.lastRefresh";
    public static final String UserDefaultsNearbyViewControllerLastRefreshKey = "com.timstrother.dth.userDefaults.nearbyViewController.lastRefresh";
    public static final String UserDefaultsCacheFacebookFriendsKey = "com.timstrother.dth.userDefaults.cache.facebookFriends";

    public static final String LaunchURLHostTakePicture = "camera";

    public static final String AppDelegateApplicationDidReceiveRemoteNotification = "com.timstrother.dth.appDelegate.applicationDidReceiveRemoteNotification";
    public static final String UtilityUserFollowingChangedNotification = "com.timstrother.dth.utility.userFollowingChanged";
    public static final String UtilityUserLikedUnlikedPhotoCallbackFinishedNotification = "com.timstrother.dth.utility.userLikedUnlikedPhotoCallbackFinished";
    public static final String UtilityDidFinishProcessingProfilePictureNotification = "com.timstrother.dth.utility.didFinishProcessingProfilePictureNotification";
    public static final String TabBarControllerDidFinishEditingPhotoNotification = "com.timstrother.dth.tabBarController.didFinishEditingPhoto";
    public static final String TabBarControllerDidFinishImageFileUploadNotification = "com.timstrother.dth.tabBarController.didFinishImageFileUploadNotification";
    public static final String PhotoDetailsViewControllerUserDeletedPhotoNotification = "com.timstrother.dth.photoDetailsViewController.userDeletedPhoto";
    public static final String PhotoDetailsViewControllerUserLikedUnlikedPhotoNotification = "com.timstrother.dth.photoDetailsViewController.userLikedUnlikedPhotoInDetailsViewNotification";
    public static final String PhotoDetailsViewControllerUserCommentedOnPhotoNotification = "com.timstrother.dth.photoDetailsViewController.userCommentedOnPhotoInDetailsViewNotification";
    public static final String UtilityDidSendDthNotification = "com.timstrother.dth.utility.didSendDthNotification";
    public static final String UtilityDidCreateEventNotification = "com.timstrother.dth.utility.didCreateEventNotification";
    public static final String UtilityDidSendInviteNotification = "com.timstrother.dth.utility.didSendInviteNotification";
    public static final String UtilityDidAcceptInviteNotification = "com.timstrother.dth.utility.didAcceptInviteNotification";
    public static final String UtilityDidAddNewInvitesNotification = "com.timstrother.dth.utility.didAddNewInvitesNotification";

    public static final String PhotoDetailsViewControllerUserLikedUnlikedPhotoNotificationUserInfoLikedKey = "liked";
    public static final String EditPhotoViewControllerUserInfoCommentKey = "comment";

    public static final String DTHNearbyPublicLabel = "Nearby Public";

    public static final String InstallationUserKey = "user";

    public static final String DTHEventClassKey = "Event";

    public static final String DTHEventTypeKey = "type";
    public static final String DTHEventCreatedByUserKey = "createdByUser";
    public static final String DTHEventDescriptionKey = "description";
    public static final String DTHEventUUIDKey = "uniqueId";
    public static final String DTHEventLifetimeMinutesKey = "lifetime";
    public static final String DTHEventLocationKey = "location";
    public static final String DTHEventDTKey = "dt";

    public static final String DTHEventTypePublic = "public"; // Change to public_t for testing
    public static final String DTHEventTypePrivate = "private";

    public static final String ActivityClassKey = "Activity";

    public static final String ActivityTypeKey = "type";
    public static final String ActivityFromUserKey = "fromUser";
    public static final String ActivityToUserKey = "toUser";
    public static final String ActivityContentKey = "content";
    public static final String ActivityPhotoKey = "photo";
    public static final String ActivityEventKey = "event";
    public static final String ActivityActivityKey = "activity";
    public static final String ActivityAcceptedKey = "accepted";
    public static final String ActivityExpirationKey = "expiresAt";
    public static final String ActivityExpiredKey = "expired";
    public static final String ActivityDeletedKey = "deleted";
    public static final String ActivityDTKey = "dt";
    public static final String ActivityPublicTagKey = "publicTag";
    public static final String ActivityReferringUserKey = "referringUser";

    public static final String ActivityTypeLike = "like";
    public static final String ActivityTypeFollow = "follow";
    public static final String ActivityTypeComment = "comment";
    public static final String ActivityTypeJoined = "joined";
    public static final String ActivityTypeDth = "dth";
    public static final String ActivityTypeInvite = "invite";
    public static final String ActivityTypeAccept = "accept";
    public static final String ActivityTypeExpire = "expire";
    public static final String ActivityTypeReferral = "referral";

    public static final String ActivityPublicTagTypePublic = "public"; // Change to public_t for testing
    public static final String ActivityPublicTagTypePrivate = "private";
    public static final String ActivityPublicTagTypePublicInvite = "public_invite";

    public static final String UserDisplayNameKey = "displayName";
    public static final String UserFacebookIDKey = "facebookId";
    public static final String UserPhotoIDKey = "photoId";
    public static final String UserProfilePicSmallKey = "profilePictureSmall";
    public static final String UserProfilePicMediumKey = "profilePictureMedium";
    public static final String UserFacebookFriendsKey = "facebookFriends";
    public static final String UserAlreadyAutoFollowedFacebookFriendsKey = "userAlreadyAutoFollowedFacebookFriends";
    public static final String UserEmailKey = "email";
    public static final String UserAutoFollowKey = "autoFollow";

    public static final String PhotoClassKey = "Photo";

    public static final String PhotoPictureKey = "image";
    public static final String PhotoThumbnailKey = "thumbnail";
    public static final String PhotoUserKey = "user";
    public static final String PhotoOpenGraphIDKey = "fbOpenGraphID";

    public static final String PhotoAttributesIsLikedByCurrentUserKey = "isLikedByCurrentUser";
    public static final String PhotoAttributesLikeCountKey = "likeCount";
    public static final String PhotoAttributesLikersKey = "likers";
    public static final String PhotoAttributesCommentCountKey = "commentCount";
    public static final String PhotoAttributesCommentersKey = "commenters";

    public static final String UserAttributesPhotoCountKey = "photoCount";
    public static final String UserAttributesIsFollowedByCurrentUserKey = "isFollowedByCurrentUser";

    public static final String APNSAlertKey = "alert";
    public static final String APNSBadgeKey = "badge";
    public static final String APNSSoundKey = "sound";

    // the following keys are intentionally kept short, APNS has a maximum payload limit
    public static final String PushPayloadPayloadTypeKey = "p";
    public static final String PushPayloadPayloadTypeActivityKey = "a";

    public static final String PushPayloadActivityTypeKey = "t";
    public static final String PushPayloadActivityLikeKey = "l";
    public static final String PushPayloadActivityCommentKey = "c";
    public static final String PushPayloadActivityFollowKey = "f";

    public static final String PushPayloadFromUserObjectIdKey = "fu";
    public static final String PushPayloadToUserObjectIdKey = "tu";
    public static final String PushPayloadPhotoObjectIdKey = "pid";
    public static final String PushPayloadActivityObjectIdKey = "aid";

    public static final String CREATED_AT = "createdAt";
    public static final String CURRENT_LOCATION = "currentLocation";
}
