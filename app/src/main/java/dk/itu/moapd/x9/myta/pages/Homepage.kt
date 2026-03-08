package dk.itu.moapd.x9.myta.pages

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import dk.itu.moapd.x9.myta.ReportViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import dk.itu.moapd.x9.myta.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import dk.itu.moapd.x9.myta.Report

@Composable
fun Homepage(viewModel: ReportViewModel) {
    val reports = viewModel.reports

    Column( modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .padding( vertical = 20.dp)
            .padding(bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
    )
        {
        Text(text = stringResource(R.string.heading), style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold)

        Spacer(modifier = Modifier.padding(top = 16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(reports) { report ->
                ReportItem(report)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportItem(report: Report, modifier: Modifier = Modifier) {
    Card(modifier = modifier.padding(8.dp).fillMaxWidth()) {
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