package com.hito.nikolay.devtesthh

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.hito.nikolay.devtesthh.extensions.isEmailValid
import com.hito.nikolay.devtesthh.extensions.isPasswordValid
import com.hito.nikolay.devtesthh.model.Weather
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val PERMISSION_ID = 1000
const val API_KEY = "e6c599b712ee97cb98c7ea6583fa8926"
const val UNITS = "metric"
const val requestWeather = "https://api.openweathermap.org/"

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_black_24_px)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.rgb(244, 244, 244)
        }

        val buttonPasswordRules = findViewById<ImageButton>(R.id.buttonPasswordRules)
        buttonPasswordRules.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.apply {
                setTitle("?????????????? ?????????? ????????????")
                setMessage("???????????? ???????????? ?????????????????? ?????????????? 1 ??????????, 1 ?????????????????? ?? 1 ???????????????? ?????????? ?? ???????????????? ?????????????? ???? 6 ????????????????")
            }.create().show()
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val enterButton = findViewById<Button>(R.id.buttonAuth)
        enterButton.setOnClickListener {
            val email = findViewById<EditText>(R.id.editTextTextEmailAddress).text.toString()
            val password = findViewById<EditText>(R.id.editTextPassword).text.toString()

            if (email.isEmailValid() && password.isPasswordValid()) {
                requestPermission()
                getLastLocation(it)

                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        locationResult ?: return
                        for (locFromCallback in locationResult.locations) {
                            getWeather(it, locFromCallback)
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        menu?.setGroupVisible(R.menu.menu, false)
        return super.onCreateOptionsMenu(menu)
    }

    //Get the last location of the device
    @SuppressLint("MissingPermission")
    private fun getLastLocation(view: View) {
        if (checkPermissions() && isLocationEnable()) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location == null) {
                    getNewLocation()
                } else {
                    getWeather(view, location)
                }
            }
        } else {
            requestPermission()
            Toast.makeText(this, "Please enable your location service", Toast.LENGTH_SHORT).show()
        }
    }

    // Get the new location of the device
    @SuppressLint("MissingPermission")
    private fun getNewLocation() {
        val locationRequest = LocationRequest()
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest.apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 0
                fastestInterval = 0
                numUpdates = 1
            },
            locationCallback,
            Looper.getMainLooper()
        )
    }


    // Fun for check permissions
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    // Request for geolocation permissions
    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    // Is the location enable
    private fun isLocationEnable(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    // Fun to ger weather data with Retrofit and show they
    private fun getWeather(view: View, location: Location) {
        val retrofit = Retrofit.Builder()
            .baseUrl(requestWeather)
            .addConverterFactory(GsonConverterFactory.create())
            .build()


        val service = retrofit.create(WeatherService::class.java)
        val call = service.getCurrentWeatherDataWithLocation(
            location.latitude,
            location.longitude,
            API_KEY,
            UNITS
        )

        call.enqueue(object : Callback<Weather> {
            override fun onResponse(call: Call<Weather>, response: Response<Weather>) {
                if (response.isSuccessful) {
                    val weather = response.body()
                    if (weather != null) {
                        val message =
                            StringBuilder("${weather.name} - ${weather.main.temp} ??C, ?????????????????? ??????: ${weather.main.feels_like} ??C \n????????????????????: lat: ${weather.coord.lat}, lon: ${weather.coord.lon}").toString()
                        Snackbar.make(view, message, 5000).show()
                    } else {
                        Snackbar.make(view, "????????????! ???? ?????????????? ???????????????? ???????????? ?? ???????????? :(", 10000)
                            .show()
                    }
                }
            }

            override fun onFailure(call: Call<Weather>, t: Throwable) {
                println(t.message.toString())
                Snackbar.make(view, t.message.toString(), 10000).show()
            }
        })
    }
}

