package de.ebuchner.vocab.mobile.common;

import android.content.Context;
import android.util.DisplayMetrics;

public class Density {

    private Density() {

    }

    public static DensityType densityType(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        switch (displayMetrics.densityDpi) {
            case DisplayMetrics.DENSITY_HIGH:
                return DensityType.HIGH;
            case DisplayMetrics.DENSITY_LOW:
                return DensityType.LOW;
            case DisplayMetrics.DENSITY_MEDIUM:
                return DensityType.MEDIUM;
            case DisplayMetrics.DENSITY_TV:
                return DensityType.TV;
            case DisplayMetrics.DENSITY_XHIGH:
                return DensityType.XHIGH;
            case DisplayMetrics.DENSITY_XXHIGH:
                return DensityType.XXHIGH;
            case DisplayMetrics.DENSITY_XXXHIGH:
                return DensityType.XXXHIGH;
        }
        return DensityType.UNKNOWN;
    }

    public enum DensityType {
        UNKNOWN, HIGH, LOW, MEDIUM, TV, XHIGH, XXHIGH, XXXHIGH
    }
}
