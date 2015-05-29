package ranking.f5.com.challengeapp.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class LocationEntity implements Serializable {

    @SerializedName("latitude")
    private double mLat;

    @SerializedName("longitude")
    private double mLng;

    @SerializedName("full_name")
    private String mName;

    @SerializedName("id")
    private long mId;

    @SerializedName("profile_picture")
    private String mProfileImage;

    @SerializedName("username")
    private String mUserName;

    public String getProfileImage() {
        return mProfileImage;
    }

    public void setProfileImage(String mProfileImage) {
        this.mProfileImage = mProfileImage;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String mUserName) {
        this.mUserName = mUserName;
    }

    public double getLat() {
        return mLat;
    }

    public void setLat(double mLat) {
        this.mLat = mLat;
    }

    public double getLng() {
        return mLng;
    }

    public void setLng(double mLng) {
        this.mLng = mLng;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public long getId() {
        return mId;
    }

    public void setId(long mId) {
        this.mId = mId;
    }
}
