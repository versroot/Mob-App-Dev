package dk.itu.moapd.x9.myta.pages

import androidx.compose.foundation.layout.Box
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
import dk.itu.moapd.x9.myta.ReportViewModel

@Composable
fun Latestpage(viewModel: ReportViewModel) {
    val latestReport = viewModel.getLatestReport()
    var showReport by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = { showReport = true }) {
            Text("Get latest report")
        }

        if (showReport) {
            AlertDialog(
                onDismissRequest = { showReport = false },
                title = {
                    Text(
                        text = "Report summary:",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    if (latestReport != null) {
                        Text(
                            """
                            Type: ${latestReport.type}
                            Description:${latestReport.description}
                            Severity: ${latestReport.severity}
                            """.trimIndent()
                        )
                    } else {
                        Text("No incidents yet.")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showReport = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}