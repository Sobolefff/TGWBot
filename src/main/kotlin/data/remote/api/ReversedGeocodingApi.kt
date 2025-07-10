package org.example.data.remote.api

import data.remote.models.ReversedCountry
import retrofit2.http.GET
import retrofit2.http.Query

interface ReversedGeocodingApi {

    @GET("reverse")
    suspend fun getCountryNameByCoordinates(
        @Query("lat") latitude: String,
        @Query("lon") longitude: String,
        @Query("format") formatData: String
    ): ReversedCountry
}