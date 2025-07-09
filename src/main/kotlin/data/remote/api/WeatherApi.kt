package org.example.data.remote.api

import data.remote.models.CurrentWeather
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryName

interface WeatherApi {
    @GET("current.json")
    fun getCurrentWeather(
        @Query("key") apiKey: String,
        @Query("q") countryName: String,
        @Query("aqi") airQualityData: String,
    ): Deferred<CurrentWeather>
}