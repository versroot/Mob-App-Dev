package dk.itu.moapd.x9.myta.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import dk.itu.moapd.x9.myta.viewmodel.ReportViewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import dk.itu.moapd.x9.myta.R
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
fun Mappage(
    viewModel: ReportViewModel
) {
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    val reports by viewModel.reports.collectAsStateWithLifecycle()

    val userLatLng = currentLocation.latitude?.let { lat ->
        currentLocation.longitude?.let { lng ->
            LatLng(lat, lng)
        }
    }

    if (userLatLng == null) {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(all = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Location unavailable :(",
                style = MaterialTheme.typography.headlineMedium
            )
        }
        return
    }

    val cameraPositionState = rememberCameraPositionState()
    val markerState = rememberMarkerState(position = userLatLng)

    var hasCenteredOnUser by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(userLatLng) {
        markerState.position = userLatLng

        if (!hasCenteredOnUser) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(userLatLng, 16f)
            )
            hasCenteredOnUser = true
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            mapType = MapType.NORMAL
        )
    ) {
        Marker(
            state = markerState,
            title = "You are here"
        )
        reports.forEach { report ->
            val lat = report.latitude
            val lng = report.longitude

            if (lat != null && lng != null) {
                key(report.key) {
                    MarkerInfoWindowContent(
                        state = rememberUpdatedMarkerState(
                            position = LatLng(lat, lng)
                        )
                    ) { marker ->
                        Column(
                            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_medium))
                        ) {
                            Text(
                                text = report.type.ifBlank { "Traffic report" },
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text("Severity: ${report.severity}")
                            Text("Time: ${formatTime24(report.timestamp)}")
                            if (report.description.isNotBlank()) {
                                Text(report.description)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun requestOrStartTracking(
    context: Context,
    onHasPermission: () -> Unit,
    onRequestPermission: () -> Unit,
) {
    val hasPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED

    if (hasPermission) onHasPermission() else onRequestPermission()
}