package edu.swust.weather.api;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class Api {
    private static final String BASE_URL = "https://api.heweather.com/x3/";

    private Api() {
    }

    private static Retrofit sRetrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .build();

    private static IApi sIApi = sRetrofit.create(IApi.class);

    public static IApi getIApi() {
        return sIApi;
    }
}
