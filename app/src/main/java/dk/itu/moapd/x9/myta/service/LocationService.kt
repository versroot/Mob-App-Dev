package dk.itu.moapd.x9.myta.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationService : Service() {

    private val binder = LocalBinder()
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val _locationUpdates = MutableStateFlow<Location?>(null)
    val locationUpdates: StateFlow<Location?> = _locationUpdates.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    inner class LocalBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }

    override fun onCreate() {
        super.onCreate()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                _locationUpdates.value = locationResult.lastLocation
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun startTracking() {
        if (_isTracking.value) return

        if (!hasLocationPermission()) {
            Log.e("LocationService", "Missing location permission")
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000L
        )
            .setMinUpdateIntervalMillis(1000L)
            .build()

        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            _isTracking.value = true
        } catch (e: SecurityException) {
            Log.e("LocationService", "Missing location permissions", e)
            _isTracking.value = false
        }
    }

    fun stopTracking() {
        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        } catch (e: SecurityException) {
            Log.e("LocationService", "Error stopping location updates", e)
        } finally {
            _isTracking.value = false
            _locationUpdates.value = null
        }
    }

    override fun onDestroy() {
        stopTracking()
        super.onDestroy()
    }
}