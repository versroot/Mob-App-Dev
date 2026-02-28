package dk.itu.moapd.x9.myta
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.itu.moapd.x9.myta.ui.theme.X9mytaTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Slider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

private const val TAG = "X9"
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            X9mytaTheme {
                NavigationBar()
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun TrafficReportForm(modifier: Modifier = Modifier) // 1st - parameter, 2nd - type, 3rd - value
{
    val reportTypes = stringArrayResource(R.array.report_types).toList()
    var selectedType by remember { mutableStateOf(reportTypes[0]) } // store what is selected; default - first
    var dropdownOpened by remember { mutableStateOf(false) } // store if opened; default - not
    var description by remember { mutableStateOf("") } // store what is in description; default - empty
    var severity by remember { mutableFloatStateOf(3f) } // store severity; default middle (Slider uses Float) [web:111]

    // column  layout (left to right)
    Column(

        modifier = modifier // update with styling
            .fillMaxSize()    // height/width = 100%
            .verticalScroll(rememberScrollState())      //  make form scrollable (when rotated)
            .padding(24.dp),  // with padding == 16 dp(scalable units)



        verticalArrangement = Arrangement.spacedBy(24.dp) // like CSS: gap: 16px
    ) {

        Text(
            text = stringResource(R.string.new_traffic_report), //
            style = MaterialTheme.typography.headlineMedium
        )


        ExposedDropdownMenuBox(
            expanded = dropdownOpened, // initially closed
            onExpandedChange = { dropdownOpened = !dropdownOpened }
            // change to the opposite
        ) {

            OutlinedTextField(
                value = selectedType, // show what is in description
                onValueChange = {}, // no typing
                readOnly = true, // no typing
                label = {Text(stringResource(R.string.report_type)) }, //
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownOpened)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)

            )

            DropdownMenu(
                expanded = dropdownOpened, // if true, show menu
                onDismissRequest = { dropdownOpened = false } // click outside -> close
            ) {
                reportTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) }, // show option text
                        onClick = {
                            selectedType = type // save selected option
                            dropdownOpened = false // close menu
                        }
                    )
                }
            }
        }
        OutlinedTextField(
            value = description, // show what is in description
            onValueChange = { newText -> description = newText }, // save what user typed into description
            label = { Text(stringResource(R.string.description)) },
            modifier = Modifier.fillMaxWidth()
        )

        Text(text = stringResource(R.string.severity_format, severity.toInt())) // show current severity number ${severity.toInt()}") // show current severity number

        Slider(
            value = severity, // show where the thumb is now
            onValueChange = { newValue -> severity = newValue }, // save new slider value while dragging
            valueRange = 1f..5f, // allowed range 1..5
            steps = 3, // makes discrete stops: 1,2,3,4,5 (5 values => 3 steps between ends) [web:111]
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = {
            if (description.isBlank()) { // handle blank
                Log.w(TAG, "Blocked submit: empty description")
                return@Button // exit button click
            }
            Log.d( // log logic
                TAG,
                "Report submitted: type=$selectedType, description=$description, severity=${severity.toInt()}"
            )
            },modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
            Text(stringResource(R.string.submit_report))
        }

    }
}
sealed class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector
    ) {
        object Home : Destination(
            route = "home",
            label = "Home",
            icon = Icons.Default.Home
        )
        object Latest : Destination(
            route = "latest",
            label = "Latest",
            icon = Icons.Default.Email
        )
        object Log : Destination(
            route = "log",
            label = "Report",
            icon = Icons.Default.Create
        )
}

@Composable
fun NavigationBarHost(navController: NavHostController, modifier: Modifier) {
    NavHost(
        navController = navController,
        startDestination = Destination.Home.route
    ) {
        composable(Destination.Home.route) {
            Homepage()
        }
        composable(Destination.Latest.route) {
            Latestpage()
        }
        composable(Destination.Log.route) {
            Logpage()
        }
    }
}

@Composable
fun NavigationBar() {
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
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun Homepage() {
//
}

@Composable
fun Latestpage() {
    //
}

@Composable
fun Logpage() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        TrafficReportForm(
            modifier = Modifier.padding(innerPadding) // innerPadding avoid status bar/notch
        )
    }
}

// purely for preview:
@Preview(showBackground = true)
@Composable
fun TrafficReportFormPreview() {
    X9mytaTheme {
        TrafficReportForm()
    }
}
