package org.example

import org.example.bot.WeatherBot
import org.example.bot.session.SessionManager
import org.example.data.remote.RetrofitClient
import org.example.data.remote.RetrofitType
import org.example.data.remote.repository.WeatherRepository

fun main() {
    startHealthServer()
    println("BOT_TOKEN: ${System.getenv("BOT_TOKEN")}")
    println("API_KEY: ${System.getenv("API_KEY")}")
    val retrofitClient = RetrofitClient()

    val weatherRetrofit = retrofitClient.getRetrofit(RetrofitType.WEATHER)
    val reverseRetrofit = retrofitClient.getRetrofit(RetrofitType.REVERSE_GEOCODER)

    val weatherApi = retrofitClient.getWeatherApi(weatherRetrofit)
    val reverseApi = retrofitClient.getReverseGeocodingApi(reverseRetrofit)

    val weatherRepository = WeatherRepository(weatherApi, reverseApi)
    val sessionManager = SessionManager()

    val weatherBot = WeatherBot(weatherRepository, sessionManager).createBot()
    weatherBot.startPolling()
    
    while (true) {
        Thread.sleep(1000)
    }
}
