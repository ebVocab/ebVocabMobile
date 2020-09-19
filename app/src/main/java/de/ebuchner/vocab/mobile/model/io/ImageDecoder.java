package de.ebuchner.vocab.mobile.model.io;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageDecoder {

    private ImageDecoder() {

    }

    public static Bitmap decode(byte[] imageBuffer) {
        return BitmapFactory.decodeByteArray(imageBuffer, 0, imageBuffer.length);
    }

}
