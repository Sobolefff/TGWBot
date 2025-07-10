package org.example.data.remote.api

import data.remote.models.CurrentWeather
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("q") countryName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String,
    ): CurrentWeather
}