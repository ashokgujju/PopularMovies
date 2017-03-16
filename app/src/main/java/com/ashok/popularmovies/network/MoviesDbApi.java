package com.ashok.popularmovies.network;

import com.ashok.popularmovies.model.MoviesResponse;
import com.ashok.popularmovies.model.ReviewsResponse;
import com.ashok.popularmovies.model.TrailersResponse;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by ashok on 26/2/17.
 */

public class MoviesDbApi {
    private static final String BASE_URL = "http://api.themoviedb.org/3/";

    //TODO Add Api key value
    public static final String API_KEY_VALUE = null;
    public static final String API_KEY_PARAM = "api_key";

    private static MoviesDBService service;

    public static MoviesDBService getClient() {
        if (service == null) {
            // Add api key query parameter to all urls
            // Source:  https://futurestud.io/tutorials/retrofit-2-how-to-add-query-parameters-to-every-request
            OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request original = chain.request();
                            HttpUrl originalHttpUrl = original.url();
                            HttpUrl url = originalHttpUrl.newBuilder()
                                    .addQueryParameter(API_KEY_PARAM, API_KEY_VALUE)
                                    .build();
                            Request.Builder requestBuilder = original.newBuilder()
                                    .url(url);
                            Request request = requestBuilder.build();
                            return chain.proceed(request);
                        }
                    });
            okHttpClientBuilder.addNetworkInterceptor(new StethoInterceptor());
            Retrofit client = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create(new Gson()))
                    .client(okHttpClientBuilder.build())
                    .baseUrl(BASE_URL)
                    .build();

            service = client.create(MoviesDBService.class);
        }
        return service;
    }

    public interface MoviesDBService {
        @GET("movie/{listType}")
        Call<MoviesResponse> getMovies(@Path("listType") String listType, @Query("page") String page);

        @GET("movie/{id}/videos")
        Call<TrailersResponse> getMovieVideos(@Path("id") String movieId);

        @GET("movie/{id}/reviews")
        Call<ReviewsResponse> getMovieReviews(@Path("id") String movieId);
    }
}
