package com.pablo.trafficalert.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class CarInfo implements Parcelable {
    private @Nullable String carName = null;
    private @Nullable String numberPlate = null;
    private @Nullable String imageUrl = null;

    public CarInfo() { }
    public CarInfo(@Nullable String carName, @Nullable String numberPlate, @Nullable String imageUrl) {
        this.carName = carName;
        this.numberPlate = numberPlate;
        this.imageUrl = imageUrl;
    }
    protected CarInfo(@NonNull Parcel in) {
        carName = in.readString();
        numberPlate = in.readString();
        imageUrl = in.readString();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(carName);
        dest.writeString(numberPlate);
        dest.writeString(imageUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CarInfo> CREATOR = new Creator<>() {
        @NonNull
        @Contract("_ -> new")
        @Override
        public CarInfo createFromParcel(Parcel in) {
            return new CarInfo(in);
        }

        @NonNull
        @Contract(value = "_ -> new", pure = true)
        @Override
        public CarInfo[] newArray(int size) {
            return new CarInfo[size];
        }
    };

    @Nullable
    public String getCarName() {
        return carName;
    }

    @Nullable
    public String getNumberPlate() {
        return numberPlate;
    }

    @Nullable
    public String getImageUrl() {
        return imageUrl;
    }
}
