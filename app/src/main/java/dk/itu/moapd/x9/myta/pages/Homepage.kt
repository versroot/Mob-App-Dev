package dk.itu.moapd.x9.myta.pages

import androidx.compose.foundation.layout.Arrangement
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import dk.itu.moapd.x9.myta.ReportViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import dk.itu.moapd.x9.myta.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import dk.itu.moapd.x9.myta.Report

@Composable
fun Homepage(viewModel: ReportViewModel, innerPadding: PaddingValues) {
    val reports by viewModel.reports.collectAsState()

    val configuration = LocalConfiguration.current.orientation
    val margin = if (configuration == Configuration.ORIENTATION_LANDSCAPE) { dimensionResource(R.dimen.spacing_small)
    } else { dimensionResource(R.dimen.spacing_large) }
    val top = if (configuration == Configuration.ORIENTATION_LANDSCAPE) { dimensionResource(R.dimen.spacing_small)
    } else { dimensionResource(R.dimen.spacing_xlarge) }

    Column( modifier = Modifier
        .fillMaxSize()
        .padding(start = dimensionResource(R.dimen.screen_padding_horizontal), end = dimensionResource(R.dimen.screen_padding_horizontal), top = top),
            horizontalAlignment = Alignment.CenterHorizontally
    )
        {
        Text(text = stringResource(R.string.heading), style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold)

        Spacer(modifier = Modifier.padding(top =  margin))

            if (configuration == Configuration.ORIENTATION_LANDSCAPE) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),   // 2 cards per row in landscape
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        bottom = innerPadding.calculateBottomPadding() + dimensionResource(R.dimen.spacing_small)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.card_padding_horizontal)),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.card_padding_horizontal))
                ) {
                    items(reports) { report ->
                        ReportItem(report = report)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        bottom = innerPadding.calculateBottomPadding() + dimensionResource(R.dimen.spacing_small)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.card_padding_horizontal))
                ) {
                    items(reports) { report ->
                        ReportItem(report = report)
                    }
                }
            }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportItem(report: Report, modifier: Modifier = Modifier) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.spacing_small))
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