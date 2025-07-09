package org.example.data.remote

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.example.data.remote.api.ReversedGeocodingApi
import org.example.data.remote.api.WeatherApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import io.github.cdimascio.dotenv.Dotenv

private const val WEATHER_BASE_URL = "http://api.weatherapi.com/v1/"
private const val REVERSE_GEOCODING_BASE_URL = "https://nominatim.openstreetmap.org/"
val dotenv: Dotenv = Dotenv.load()

private var API_KEY = dotenv["API_KEYy"]

enum class RetrofitType(val baseUrl: String) {
    WEATHER(WEATHER_BASE_URL),
    REVERSE_GEOCODER(REVERSE_GEOCODING_BASE_URL)
}

class RetrofitClient {

    fun getClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
        return okHttpClient.build()
    }

    fun getRetrofit(retrofitType: RetrofitType): Retrofit =
         Retrofit.Builder()
            .baseUrl(retrofitType.baseUrl)
            .addCallAdapterFactory(CoroutineCallAdapterFactory.invoke())
            .addConverterFactory(GsonConverterFactory.create())
            .build()



    fun getWeatherApi(retrofit: Retrofit) : WeatherApi = retrofit.create(WeatherApi::class.java)
    fun getReverseGeocodingApi(retrofit: Retrofit) : ReversedGeocodingApi = retrofit.create(ReversedGeocodingApi::class.java)

}