package dk.itu.moapd.x9.myta.ui
import android.content.Intent
import android.os.Bundle
import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import dk.itu.moapd.x9.myta.service.LocationService
import androidx.compose.ui.graphics.Color
import com.google.firebase.auth.FirebaseAuth
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import dk.itu.moapd.x9.myta.theme.X9mytaTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.itu.moapd.x9.myta.R
import dk.itu.moapd.x9.myta.auth.LoginActivity
import dk.itu.moapd.x9.myta.ui.screen.Homepage
import dk.itu.moapd.x9.myta.ui.screen.Mappage
import dk.itu.moapd.x9.myta.ui.screen.Logpage
import dk.itu.moapd.x9.myta.viewmodel.ReportViewModel
import dk.itu.moapd.x9.myta.ui.screen.requestOrStartTracking

const val TAG = "X9"

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onStart() {
        super.onStart()
        // Redirect the user to the LoginActivity if they are not logged in.
        auth.currentUser ?: startLoginActivity()
    }

    private fun startLoginActivity() {
        Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }.let(::startActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()   // Initialize Firebase Auth.

        setContent {
            val viewModel: ReportViewModel = viewModel() // create viewmodel
            X9mytaTheme {
                BottomNavigationBar(viewModel = viewModel, auth = auth, //pass viewmodel to navigation bar
                    onLogout = {
                        auth.signOut()
                        startLoginActivity() }
                )
            }
        }
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
            Mappage(viewModel = viewModel)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(viewModel: ReportViewModel, auth: FirebaseAuth, onLogout: () -> Unit) {
    val navController = rememberNavController()     // navigation state
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
                isBound = locationService != null
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

    DisposableEffect(context, connection) {
        val intent = Intent(context, LocationService::class.java)
        val bound = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)

        onDispose {
            if (bound) {
                context.unbindService(connection)
            }
        }
    }

    val location by locationService
        ?.locationUpdates
        ?.collectAsStateWithLifecycle(initialValue = null)
        ?: remember { mutableStateOf(null) }

    val isTracking by locationService
        ?.isTracking
        ?.collectAsStateWithLifecycle(initialValue = false)
        ?: remember { mutableStateOf(false) }

    LaunchedEffect(location) {
        location?.let {
            viewModel.updateCurrentLocation(
                latitude = it.latitude,
                longitude = it.longitude
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                title = {},
                navigationIcon = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                stringResource(R.string.menu)
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }) {
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
                val selectedDestination =
                    navController.currentBackStackEntryAsState().value?.destination?.route
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = selectedDestination == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = stringResource(destination.labelRes)
                            )
                        },
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
        val userName = auth.currentUser?.displayName ?: "User"
        val reportCount = reports.size

        AlertDialog(
            onDismissRequest = {/**/ },
            confirmButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("OK")
                }
            },
            title = { Text("Profile") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(text = userName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Total Reports: $reportCount", fontSize = 16.sp)
                }
            }
        )
    }
    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = {
                Text(stringResource(R.string.dialogue_loc))
            },
            text = {
                Column {
                    Text("Service bound: $isBound")
                    Text(if (location?.latitude != null) {
                            "Latitude: %.6f".format(location?.latitude)
                        } else
                        {"Latitude: Not available" }
                    )
                    Text(if (location?.longitude != null) {
                            "Longitude: %.6f".format(location?.longitude)
                        } else
                        {"Longitude: Not available"}
                    )
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
                            requestOrStartTracking(
                                context = context,
                                onHasPermission = {
                                    service.startTracking()
                                },
                                onRequestPermission = {
                                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            )
                        }
                    },
                    enabled = isBound
                ) {
                    Text(if (isTracking) "Stop tracking" else "Start tracking")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDialog = false }) {
                    Text(text = stringResource(R.string.dialog_ok))
                }
            }
        )
    }
}

// purely for preview:
@Preview(showBackground = true)
@Composable
fun TrafficReportFormPreview() {
    X9mytaTheme {
        //TrafficReportForm()
    }
}
