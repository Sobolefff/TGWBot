package org.example.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.example.data.remote.api.ReversedGeocodingApi
import org.example.data.remote.api.WeatherApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/"
private const val REVERSE_GEOCODING_BASE_URL = "https://nominatim.openstreetmap.org/"

enum class RetrofitType(val baseUrl: String) {
    WEATHER(WEATHER_BASE_URL),
    REVERSE_GEOCODER(REVERSE_GEOCODING_BASE_URL)
}

class RetrofitClient {

    fun getClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        logging.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val newRequest = chain.request().newBuilder()
                    .header("User-Agent", "MyWeatherApp/1.0 (petr.soboleff@gmail.com)")
                    .build()
                chain.proceed(newRequest)
            }
            .addInterceptor(logging)
            .build()
    }

    fun getRetrofit(retrofitType: RetrofitType): Retrofit =
         Retrofit.Builder()
            .baseUrl(retrofitType.baseUrl)
            .client(getClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()





    fun getWeatherApi(retrofit: Retrofit) : WeatherApi = retrofit.create(WeatherApi::class.java)
    fun getReverseGeocodingApi(retrofit: Retrofit) : ReversedGeocodingApi = retrofit.create(ReversedGeocodingApi::class.java)

}