package com.hito.nikolay.devtesthh

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import com.hito.nikolay.devtesthh.model.Weather

//Retrofit request to get weather information using phone coordinates
interface WeatherService {
    @GET("data/2.5/weather?")
    fun getCurrentWeatherDataWithLocation(@Query("lat") lat: Double, @Query("lon") lon: Double, @Query("appid") app_id: String, @Query("units") units:String): Call<Weather>
}