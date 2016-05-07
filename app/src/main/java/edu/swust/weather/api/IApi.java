package edu.swust.weather.api;

import edu.swust.weather.model.WeatherData;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface IApi {
    @GET("weather")
    Observable<WeatherData> getWeather(@Query("city") String city, @Query("key") String key);
}
