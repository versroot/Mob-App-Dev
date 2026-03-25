package dk.itu.moapd.x9.myta
import android.content.Intent
import android.os.Bundle
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
import dk.itu.moapd.x9.myta.ui.theme.X9mytaTheme
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dk.itu.moapd.x9.myta.pages.Homepage
import dk.itu.moapd.x9.myta.pages.Logpage
import dk.itu.moapd.x9.myta.pages.Latestpage


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
    object Latest : Destination("latest", R.string.nav_latest, Icons.Default.Email)
    object Log : Destination("log", R.string.nav_report, Icons.Default.Create)
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
        composable(Destination.Latest.route) {
            Latestpage(viewModel = viewModel)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(viewModel: ReportViewModel, auth: FirebaseAuth, onLogout: () -> Unit ) {
    val navController = rememberNavController()     // navigation state
    val destinations = listOf(Destination.Home, Destination.Latest, Destination.Log)
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent),
                title = {},
                navigationIcon = {
                    Box {
                        IconButton(onClick = {menuExpanded = true}) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                "Open Menu")
                        }
                        DropdownMenu( expanded = menuExpanded, onDismissRequest = {menuExpanded = false}) {
                            DropdownMenuItem(
                                text = {Text("Logout")},
                                onClick = {
                                    menuExpanded = false
                                    onLogout()
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
                            Icon(imageVector = destination.icon,
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
}

// purely for preview:
@Preview(showBackground = true)
@Composable
fun TrafficReportFormPreview() {
    X9mytaTheme {
        //TrafficReportForm()
    }
}
