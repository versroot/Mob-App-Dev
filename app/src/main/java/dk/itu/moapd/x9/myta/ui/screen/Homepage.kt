package dk.itu.moapd.x9.myta.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.res.integerResource
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.itu.moapd.x9.myta.R
import dk.itu.moapd.x9.myta.viewmodel.Report
import dk.itu.moapd.x9.myta.viewmodel.ReportViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun Homepage(viewModel: ReportViewModel, innerPadding: PaddingValues) {
    val reports by viewModel.reports.collectAsStateWithLifecycle()

    val configuration = LocalConfiguration.current.orientation
    val margin = if (configuration == Configuration.ORIENTATION_LANDSCAPE) { dimensionResource(R.dimen.spacing_small)
    } else { dimensionResource(R.dimen.spacing_large) }
    val top = if (configuration == Configuration.ORIENTATION_LANDSCAPE) { dimensionResource(R.dimen.spacing_small)
    } else { dimensionResource(R.dimen.spacing_xlarge) }
    val nCards = if (configuration == Configuration.ORIENTATION_LANDSCAPE) { integerResource(R.integer.ncards_horizontal)
    } else { integerResource(R.integer.ncards_vertical) }

    Column( modifier = Modifier
        .fillMaxSize()
        .padding(start = dimensionResource(R.dimen.screen_padding_horizontal), end = dimensionResource(R.dimen.screen_padding_horizontal), top = top),
            horizontalAlignment = Alignment.CenterHorizontally
    )
        {
        Text(text = stringResource(R.string.heading), style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold)

        Spacer(modifier = Modifier.padding(top =  margin))

        LazyVerticalGrid(
            columns = GridCells.Fixed(nCards),   // 2 cards per row in landscape
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(
                bottom = innerPadding.calculateBottomPadding() + dimensionResource(R.dimen.spacing_small)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.card_padding_horizontal)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.card_padding_horizontal))
        ) {
            items(reports,key = { it.key }) { report ->
                ReportItem(report = report, viewModel = viewModel)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportItem(
    report: Report,
    viewModel: ReportViewModel,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = { Text("Delete this report?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteReport(report.key)
                }
                )
                { Text("Confirm", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                }
                )
                { Text("No") }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.spacing_small))
            .combinedClickable(
                onClick = { /**/ },
                onLongClick = {
                    showDeleteDialog = true
                }
            )
    ) {
        Row(modifier = Modifier
            .fillMaxWidth().padding(vertical = dimensionResource(R.dimen.card_padding_vertical), horizontal = dimensionResource(R.dimen.card_padding_horizontal))
        ) {
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = dimensionResource(R.dimen.card_padding_horizontal))
            ) {
                Text(
                    text = stringResource(R.string.report_severity, report.severity),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(text = formatTime24(report.timestamp), style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.spacing_medium)))
            }
            Column(
                modifier = Modifier.weight(2f)
            ) {
                Text(text = report.type, style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold)
                Text(text = report.description, style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.spacing_medium)))
            }
        }
    }
}

fun formatTime24(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}