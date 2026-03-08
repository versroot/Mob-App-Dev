package dk.itu.moapd.x9.myta.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.res.stringResource
import dk.itu.moapd.x9.myta.R
import dk.itu.moapd.x9.myta.ReportViewModel

@Composable
fun Latestpage(viewModel: ReportViewModel) {
    val latestReport = viewModel.getLatestReport()
    var showReport by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { showReport = true }) {
            Text(text = stringResource(R.string.get_latest))
        }
    }

    if (showReport) {
        AlertDialog(
            onDismissRequest = { showReport = false },
            title = {
                Text(
                    text = stringResource(R.string.report_summary),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                if (latestReport != null) {
                    Text(
                        text = stringResource(
                            R.string.latest_report_details,
                            latestReport.type,
                            latestReport.description,
                            latestReport.severity
                        )
                    )
                } else {
                    Text(text = stringResource(R.string.no_reports))
                }
            },
            confirmButton = {
                TextButton(onClick = { showReport = false }) {
                    Text(stringResource(R.string.dialog_ok))
                }
            }

        )
    }
}