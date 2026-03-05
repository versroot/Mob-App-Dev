package dk.itu.moapd.x9.myta
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dk.itu.moapd.x9.myta.ui.theme.X9mytaTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dk.itu.moapd.x9.myta.pages.Homepage
import dk.itu.moapd.x9.myta.pages.Logpage
import dk.itu.moapd.x9.myta.pages.Latestpage
import dk.itu.moapd.x9.myta.ReportViewModel

const val TAG = "X9"
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: ReportViewModel = viewModel() // create viewmodel
            X9mytaTheme {
                BottomNavigationBar(viewModel = viewModel) //pass viewmodel to navigation bar
            }
        }
    }
}


sealed class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Home : Destination("home", "Home", Icons.Default.Home)
    object Latest : Destination("latest", "Latest", Icons.Default.Email)
    object Log : Destination("log", "Report", Icons.Default.Create)
}

@Composable
fun NavigationBarHost(navController: NavHostController, modifier: Modifier, viewModel: ReportViewModel) {
    NavHost(
        navController = navController,
        startDestination = Destination.Home.route
    ) {
        composable(Destination.Home.route) {
            Homepage(viewModel = viewModel)
        }
        composable(Destination.Latest.route) {
            Latestpage(viewModel = viewModel)
        }
        composable(Destination.Log.route) {
            Logpage(viewModel = viewModel)
        }
    }
}

@Composable
fun BottomNavigationBar(viewModel: ReportViewModel) {
    val navController = rememberNavController()     // navigation state
    val destinations = listOf(Destination.Home, Destination.Latest, Destination.Log)

    Scaffold(
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
                                contentDescription = destination.label
                            )
                        },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavigationBarHost(
            navController = navController,
            modifier = Modifier.padding(paddingValues),
            viewModel = viewModel
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
