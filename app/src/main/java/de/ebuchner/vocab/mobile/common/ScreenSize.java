package de.ebuchner.vocab.mobile.common;

import android.content.Context;
import android.content.res.Configuration;

public class ScreenSize {

    private ScreenSize() {

    }

    public static Size screenSizeName(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        Size size = Size.UNKNOWN;
        if ((configuration.screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_LARGE)
            size = Size.LARGE;
        if ((configuration.screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_NORMAL)
            size = Size.NORMAL;
        if ((configuration.screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_SMALL)
            size = Size.SMALL;
        if ((configuration.screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_XLARGE)
            size = Size.X_LARGE;

        return size;
    }

    public enum Size {
        X_LARGE, LARGE, NORMAL, SMALL, UNKNOWN
    }
}
