package com.pablo.trafficalert.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UserInfo implements Parcelable {
    private @NotNull String uid = "";
    private @NotNull String email = "";
    private @Nullable String phoneNumber = null;
    private @Nullable String names = null;
    private @Nullable String lastName = null;
    private @Nullable String imageUrl = null;
    private boolean isAdmin = false;
    private @Nullable CarInfo carInfo = null;

    @NonNull
    @Override
    public String toString() {
        return "\nUserInfo{" +
                "uid='" + uid + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", names='" + names + '\'' +
                ", lastName='" + lastName + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", isAdmin=" + isAdmin +
                ", carInfo=" + carInfo +
                '}';
    }

    public UserInfo() { }
    public UserInfo(@NotNull String uid, @NotNull String email) {
        this.uid = uid;
        this.email = email;
    }

    public UserInfo(@NotNull String uid, @NotNull String email, @Nullable String phoneNumber, @Nullable String names, @Nullable String lastName, @Nullable String imageUrl, boolean isAdmin, @Nullable CarInfo carInfo) {
        this.uid = uid;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.names = names;
        this.lastName = lastName;
        this.imageUrl = imageUrl;
        this.isAdmin = isAdmin;
        this.carInfo = carInfo;
    }

    protected UserInfo(@NonNull Parcel in) {
        uid = in.readString();
        email = in.readString();
        phoneNumber = in.readString();
        names = in.readString();
        lastName = in.readString();
        imageUrl = in.readString();
        isAdmin = in.readByte() != 0;
        carInfo = in.readParcelable(CarInfo.class.getClassLoader());
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(email);
        dest.writeString(phoneNumber);
        dest.writeString(names);
        dest.writeString(lastName);
        dest.writeString(imageUrl);
        dest.writeByte((byte) (isAdmin ? 1 : 0));
        dest.writeParcelable(carInfo, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @NonNull
        @Contract("_ -> new")
        @Override
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        @NonNull
        @Contract(value = "_ -> new", pure = true)
        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    @NotNull
    public String getUid() {
        return uid;
    }
    @NotNull
    public String getEmail() {
        return email;
    }
    @Nullable
    public String getPhoneNumber() {
        return phoneNumber;
    }
    @Nullable
    public String getNames() {
        return names;
    }
    @Nullable
    public String getLastName() {
        return lastName;
    }

    @Nullable
    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}
