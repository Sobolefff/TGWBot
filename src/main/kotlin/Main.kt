package org.example

import org.example.bot.WeatherBot
import org.example.data.remote.RetrofitClient
import org.example.data.remote.RetrofitType
import org.example.data.remote.repository.WeatherRepository

fun main() {
    val weatherRetrofit = RetrofitClient().getRetrofit(RetrofitType.WEATHER)
    val reverseRetrofit = RetrofitClient().getRetrofit(RetrofitType.REVERSE_GEOCODER)
    val weatherApi = RetrofitClient().getWeatherApi(weatherRetrofit)
    val reverseApi = RetrofitClient().getReverseGeocodingApi(reverseRetrofit)
    val weatherRepository = WeatherRepository(weatherApi, reverseApi)
    val weatherBot = WeatherBot(weatherRepository).createBot()
    weatherBot.startPolling()
}