package org.example.data.remote.repository

import data.remote.models.CurrentWeather
import data.remote.models.ReversedCountry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.data.remote.api.ReversedGeocodingApi
import org.example.data.remote.api.WeatherApi

class WeatherRepository(
    private val weatherApi: WeatherApi,
    private val reversedGeocodingApi: ReversedGeocodingApi,
) {

    suspend fun getCurrentWeather(countryName: String, apiKey: String, units: String): CurrentWeather {
        return withContext(Dispatchers.IO) {
            weatherApi.getCurrentWeather(countryName, apiKey, units)

        }
    }

    suspend fun getReverseGeocodingCountryName(latitude: String, longitude: String, format: String): ReversedCountry {
        return withContext(Dispatchers.IO) {
            reversedGeocodingApi.getCountryNameByCoordinates(latitude, longitude, "json")
        }
    }

}