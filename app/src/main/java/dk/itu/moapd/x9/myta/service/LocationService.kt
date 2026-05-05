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
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dk.itu.moapd.x9.myta.viewmodel.Report
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

    private var reports: List<Report> = emptyList()
    private val alertedReports = mutableSetOf<String>()

    inner class LocalBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }

    override fun onCreate() {
        super.onCreate()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                _locationUpdates.value = location
                checkDistances(location)
            }
        }
    }

    private fun checkDistances(currentLocation: Location) {
        reports.forEach { report ->
            val lat = report.latitude ?: return@forEach
            val lon = report.longitude ?: return@forEach

            val reportLocation = Location("").apply {
                latitude = lat
                longitude = lon
            }

            val distance = currentLocation.distanceTo(reportLocation)
            
            // Trigger alert when within 200 meters
            if (distance < 200) {
                if (!alertedReports.contains(report.key)) {
                    Toast.makeText(
                        applicationContext,
                        "Approaching ${report.type}!",
                        Toast.LENGTH_SHORT
                    ).show()
                    alertedReports.add(report.key)
                    Log.d("LocationService", "Alert triggered for ${report.key} at distance $distance")
                }
            } else if (distance > 300) {
                // Reset alert if user moves more than 300 meters away
                if (alertedReports.contains(report.key)) {
                    alertedReports.remove(report.key)
                    Log.d("LocationService", "Alert reset for ${report.key}")
                }
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
            alertedReports.clear()
        }
    }

    fun updateGeofences(newReports: List<Report>) {
        this.reports = newReports
        Log.d("LocationService", "Manual geofences updated with ${reports.size} reports")
    }

    override fun onDestroy() {
        stopTracking()
        super.onDestroy()
    }
}
