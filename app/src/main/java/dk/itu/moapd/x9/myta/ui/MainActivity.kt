package dk.itu.moapd.x9.myta.ui

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.google.firebase.auth.FirebaseAuth
import dk.itu.moapd.x9.myta.R
import dk.itu.moapd.x9.myta.auth.LoginActivity
import dk.itu.moapd.x9.myta.service.LocationService
import dk.itu.moapd.x9.myta.theme.X9mytaTheme
import dk.itu.moapd.x9.myta.ui.screen.Homepage
import dk.itu.moapd.x9.myta.ui.screen.Logpage
import dk.itu.moapd.x9.myta.ui.screen.Mappage
import dk.itu.moapd.x9.myta.ui.screen.requestOrStartTracking
import dk.itu.moapd.x9.myta.viewmodel.ReportViewModel
import dk.itu.moapd.x9.myta.util.ShakeDetector

const val TAG = "X9"
class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var sensorManager: SensorManager
    private lateinit var shakeDetector: ShakeDetector
    private var accelerometer: Sensor? = null

    private var navigateToLogRequested by mutableStateOf(false)

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            startLoginActivity()
        }
    }

    private fun startLoginActivity() {
        Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }.let(::startActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        shakeDetector = ShakeDetector {
            navigateToLogRequested = true
        }

        setContent {
            val viewModel: ReportViewModel = viewModel()

            X9mytaTheme {
                BottomNavigationBar(
                    viewModel = viewModel,
                    auth = auth,
                    onLogout = {
                        auth.signOut()
                        startLoginActivity()
                    },
                    navigateToLogRequested = navigateToLogRequested,
                    onLogNavigationHandled = {
                        navigateToLogRequested = false
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(
                shakeDetector,
                it,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(shakeDetector)
    }
}

sealed class Destination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    object Home : Destination("home", R.string.nav_home, Icons.Default.Home)
    object Map : Destination("map", R.string.nav_map, Icons.Default.LocationSearching)
    object Log : Destination("log", R.string.nav_report, Icons.Default.AddTask)
}

@Composable
fun NavigationBarHost(
    navController: NavHostController,
    modifier: Modifier,
    viewModel: ReportViewModel,
    innerPadding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Home.route,
        modifier = modifier
    ) {
        composable(Destination.Home.route) {
            Homepage(viewModel = viewModel, innerPadding = innerPadding)
        }
        composable(Destination.Log.route) {
            Logpage(viewModel = viewModel, innerPadding = innerPadding)
        }
        composable(Destination.Map.route) {
            Mappage(viewModel = viewModel, innerPadding = innerPadding)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(viewModel: ReportViewModel, auth: FirebaseAuth, onLogout: () -> Unit,
                        navigateToLogRequested: Boolean,
                        onLogNavigationHandled: () -> Unit) {
    val navController = rememberNavController()

    LaunchedEffect(navigateToLogRequested) {
        if (navigateToLogRequested) {
            navController.navigate(Destination.Log.route) {
                popUpTo(navController.graph.startDestinationId)
                launchSingleTop = true
            }
            onLogNavigationHandled()
        }
    }

    val destinations = listOf(Destination.Home, Destination.Map, Destination.Log)
    var menuExpanded by remember { mutableStateOf(false) }
    var showProfileDialog by rememberSaveable { mutableStateOf(false) }
    var showLocationDialog by rememberSaveable { mutableStateOf(false) }
    val reports by viewModel.reports.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var locationService by remember { mutableStateOf<LocationService?>(null) }
    var isBound by remember { mutableStateOf(false) }

    val connection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as? LocationService.LocalBinder
                locationService = binder?.getService()
                isBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                locationService = null
                isBound = false
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            locationService?.startTracking()
        }
    }

    DisposableEffect(context) {
        val intent = Intent(context, LocationService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        onDispose {
            if (isBound) {
                context.unbindService(connection)
            }
        }
    }

    // Fix: Avoid conditional Composable calls to collectAsStateWithLifecycle
    val locationUpdatesFlow = remember(locationService) {
        locationService?.locationUpdates ?: kotlinx.coroutines.flow.MutableStateFlow(null)
    }
    val locationUpdates by locationUpdatesFlow.collectAsStateWithLifecycle(null)

    val isTrackingFlow = remember(locationService) {
        locationService?.isTracking ?: kotlinx.coroutines.flow.MutableStateFlow(false)
    }
    val isTracking by isTrackingFlow.collectAsStateWithLifecycle(false)

    LaunchedEffect(reports, locationService, isBound) {
        if (isBound) {
            locationService?.updateGeofences(reports)
        }
    }

    LaunchedEffect(locationUpdates) {
        locationUpdates?.let {
            viewModel.updateCurrentLocation(it.latitude, it.longitude)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {},
                navigationIcon = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Filled.Menu, stringResource(R.string.menu))
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_item_1)) },
                                onClick = {
                                    menuExpanded = false
                                    showProfileDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_item_2)) },
                                onClick = {
                                    menuExpanded = false
                                    onLogout()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Location") },
                                onClick = {
                                    menuExpanded = false
                                    showLocationDialog = true
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = stringResource(destination.labelRes)) },
                        label = { Text(stringResource(destination.labelRes)) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavigationBarHost(
            navController = navController,
            modifier = Modifier,
            viewModel = viewModel,
            innerPadding = paddingValues
        )
    }

    if (showProfileDialog) {
        val userName = auth.currentUser?.displayName ?: auth.currentUser?.email ?: "User"
        val currentUserId = auth.currentUser?.uid
        val reportCount = reports.count { it.uid == currentUserId }
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            confirmButton = { TextButton(onClick = { showProfileDialog = false }) { Text("OK") } },
            title = { Text("Profile") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(80.dp)) {
                        Icon(Icons.Filled.AccountCircle, contentDescription = null, modifier = Modifier.fillMaxSize())
                    }
                    Text(text = userName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = "My Reports: $reportCount", fontSize = 16.sp)
                }
            }
        )
    }

    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text(stringResource(R.string.dialogue_loc)) },
            text = {
                Column {
                    Text("Service bound: $isBound")
                    Text("Latitude: ${locationUpdates?.latitude ?: "Not available"}")
                    Text("Longitude: ${locationUpdates?.longitude ?: "Not available"}")
                    Text("Tracking: ${if (isTracking) "ON" else "OFF"}")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val service = locationService ?: return@TextButton
                        if (isTracking) {
                            service.stopTracking()
                            viewModel.clearCurrentLocation()
                        } else {
                            requestOrStartTracking(context, { service.startTracking() }, { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) })
                        }
                    },
                    enabled = isBound
                ) {
                    Text(if (isTracking) "Stop tracking" else "Start tracking")
                }
            },
            dismissButton = { TextButton(onClick = { showLocationDialog = false }) { Text(stringResource(R.string.dialog_ok)) } }
        )
    }
}