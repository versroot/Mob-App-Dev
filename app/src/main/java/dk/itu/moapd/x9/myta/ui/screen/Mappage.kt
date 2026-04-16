package dk.itu.moapd.x9.myta.ui.screen

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.itu.moapd.x9.myta.R
import dk.itu.moapd.x9.myta.service.LocationService
import dk.itu.moapd.x9.myta.viewmodel.ReportViewModel

@Composable
fun Mappage(
    modifier: Modifier = Modifier,
    viewModel: ReportViewModel
) {
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.generic_padding)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Current location",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (currentLocation.latitude != null && currentLocation.longitude != null) {
                "Location: %.6f, %.6f".format(
                    currentLocation.latitude,
                    currentLocation.longitude
                )
            } else {
                "Location: not available"
            }
        )
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