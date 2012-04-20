package cz.cesnet.shongo.controller.api;

import cz.cesnet.shongo.common.api.UserIdentity;

/**
 * Represents an active user in a virtual Room
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class RoomUser
{
    /**
     * User id in room (generated by the concrete technology - NOT the user identity
     */
    private String id;

    /**
     * Room resource id
     */
    private String roomId;

    /**
     * User identity
     */
    private UserIdentity user;

    /**
     * Is the user muted?
     */
    private boolean muted;

    /**
     * Microphone level
     */
    private int microphoneLevel;

    /**
     * Playback level (speakers volume)
     */
    private int playbackLevel;


    public String getId()
    {
        return id;
    }

    public String getRoomId()
    {
        return roomId;
    }

    public UserIdentity getUser()
    {
        return user;
    }

    public boolean isMuted()
    {
        return muted;
    }

    public void setMuted(boolean muted)
    {
        this.muted = muted;
    }

    public int getMicrophoneLevel()
    {
        return microphoneLevel;
    }

    public void setMicrophoneLevel(int microphoneLevel)
    {
        this.microphoneLevel = microphoneLevel;
    }

    public int getPlaybackLevel()
    {
        return playbackLevel;
    }

    public void setPlaybackLevel(int playbackLevel)
    {
        this.playbackLevel = playbackLevel;
    }
}
