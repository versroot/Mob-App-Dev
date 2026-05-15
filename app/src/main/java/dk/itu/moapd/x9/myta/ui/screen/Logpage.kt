package dk.itu.moapd.x9.myta.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.itu.moapd.x9.myta.R
import dk.itu.moapd.x9.myta.ui.TAG
import dk.itu.moapd.x9.myta.viewmodel.ReportViewModel
import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File

fun createImageUri(context: Context): Uri {
    val directory = File(context.cacheDir, "my_images") // temp dir
    directory.mkdirs()

    val file = File.createTempFile(
        "captured_image_",
        ".jpg",
        directory
    )

    val authority = "${context.packageName}.fileprovider"

    return FileProvider.getUriForFile(
        context,
        authority,
        file
    )
}
@Composable
fun Logpage(viewModel: ReportViewModel, innerPadding: PaddingValues) {
    TrafficReportForm(
        viewModel = viewModel,
        innerPadding = innerPadding
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrafficReportForm(modifier: Modifier = Modifier, innerPadding: PaddingValues, viewModel: ReportViewModel) // 1st - parameter, 2nd - type, 3rd - value
{
    val context = LocalContext.current

    val reportTypes = stringArrayResource(R.array.report_types).toList()
    var selectedType by rememberSaveable { mutableStateOf(reportTypes[0]) } // store what is selected; default - first
    var dropdownOpened by rememberSaveable { mutableStateOf(false) } // store if opened; default - not
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    var description by rememberSaveable { mutableStateOf("") } // store what is in description; default - empty
    var severity by rememberSaveable { mutableFloatStateOf(3f) } // store severity; default middle (Slider uses Float) [web:111]

    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var tempCameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        imageUri = uri
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = tempCameraUri
        }
        tempCameraUri = null
    }
    // A launcher to request the CAMERA permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission given! Now we can safely trigger the camera
            val uri = createImageUri(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            // User said no. Show a message explaining why we need it
            Toast.makeText(context, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
        }
    }
    // column  layout (left to right)
    Column(

        modifier = modifier // update with styling
            .fillMaxSize()    // height/width = 100%
            .verticalScroll(rememberScrollState())      //  make form scrollable (when rotated)
            .padding(
                start = dimensionResource(R.dimen.generic_padding),
                end = dimensionResource(R.dimen.generic_padding),
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + dimensionResource(R.dimen.generic_padding)
            ),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.generic_padding)) // like CSS: gap: 16px
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
                    .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable)

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // gallery
            Button(
                onClick = {
                    galleryLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Gallery")
            }

            // camera
            // Updated Camera button
            Button(
                onClick = {
                    val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.CAMERA
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        // Already have permission? Go straight to camera
                        val uri = createImageUri(context)
                        tempCameraUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        // No permission? Ask for it!
                        permissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Camera")
            }
        }


        imageUri?.let {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                coil.compose.AsyncImage(
                    model = it,
                    contentDescription = "Selected Image",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Button(onClick = { // submit button
            if (description.isBlank()) { // handle blank
                Log.w(TAG, "Blocked submit: empty description")
                Toast.makeText(
                    context,
                    context.getString(R.string.report_not_submitted),
                    Toast.LENGTH_SHORT
                ).show()
                return@Button // exit button click
            }
            Log.d( // log logic
                TAG,
                "Report submitted: type=$selectedType, description=$description, severity=${severity.toInt()}"  //!TODO move to strings
            )
            viewModel.addReport(    //save to viewmodel
                type = selectedType,
                description = description,
                severity = severity.toInt(),
                latitude = currentLocation.latitude,
                longitude = currentLocation.longitude,
                imageUri = imageUri
            )
            description = ""    // Clear form
            severity = 3f
            selectedType = reportTypes[0]
            imageUri = null

            Toast.makeText(
                context,
                context.getString(R.string.report_submitted),
                Toast.LENGTH_SHORT
            ).show()


        },modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(stringResource(R.string.submit_report))
        }

    }
}