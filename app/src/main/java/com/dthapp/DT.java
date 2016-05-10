package com.dthapp;


public class DT {
    private final String hostProfilePicUrl;
    private final String hostname;
    private final String name;
    private final long duration;
    private final boolean isUserDown;

    public DT(String name, String hostname, String hostProfilePicUrl, long duration, boolean isUserDown) {
        this.hostProfilePicUrl = hostProfilePicUrl;
        this.hostname = hostname;
        this.name = name;
        this.duration = duration;
        this.isUserDown = isUserDown;
    }

    public String getName() {
        return name;
    }

    public String getHostname() {
        return hostname;
    }

    public String getHostProfilePic() {
        return hostProfilePicUrl;
    }

    public long getFinishTimeUTC() {
        return duration;
    }

    public boolean isUserDown() {
        return isUserDown;
    }
}