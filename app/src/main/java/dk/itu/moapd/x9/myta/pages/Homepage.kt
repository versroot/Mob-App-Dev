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
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import dk.itu.moapd.x9.myta.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import dk.itu.moapd.x9.myta.Report

@Composable
fun Homepage(viewModel: ReportViewModel, innerPadding: PaddingValues) {
    val reports = viewModel.reports

    val configuration = LocalConfiguration.current.orientation
    val margin = if (configuration == Configuration.ORIENTATION_LANDSCAPE) { 4.dp
    } else { 16.dp }
    val top = if (configuration == Configuration.ORIENTATION_LANDSCAPE) { 4.dp
    } else { 44.dp }

    Column( modifier = Modifier
        .fillMaxSize()
        .padding(start = 20.dp, end = 20.dp, top = top),
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
                        bottom = innerPadding.calculateBottomPadding() + 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                        bottom = innerPadding.calculateBottomPadding() + 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
            .padding(vertical = 4.dp)
    ) {
        Row(modifier = Modifier
                .fillMaxWidth().padding(vertical = 20.dp, horizontal = 10.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 10.dp)
            ) {
                Text(
                    text = stringResource(R.string.report_severity, report.severity),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(text = formatTime24(report.timestamp), style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp))
            }
            Column(
                modifier = Modifier.weight(2f)
            ) {
                Text(text = report.type, style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold)
                Text(text = report.description, style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp))
            }
        }

    }
}

fun formatTime24(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}