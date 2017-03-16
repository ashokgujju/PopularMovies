package com.ashok.popularmovies.network;

import android.net.Uri;

/**
 * Created by ashok on 24/2/17.
 */

public class NetworkUtils {
    private static final String IMAGES_BASE_URL = "http://image.tmdb.org/t/p";
    private static final String IMAGES_SIZE_PATH = "w185";

    public static Uri buildImgUri(String imagePath) {
        Uri uri = Uri.parse(IMAGES_BASE_URL).buildUpon()
                .appendPath(IMAGES_SIZE_PATH)
                .appendEncodedPath(imagePath)
                .build();

        return uri;
    }
}