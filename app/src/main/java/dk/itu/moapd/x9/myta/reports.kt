package dk.itu.moapd.x9.myta

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
data class Report(
    val type: String,
    val description: String,
    val severity: Int,
    val timestamp: Long = System.currentTimeMillis()
)

// Viewmodel: persistent storage of data even if activities are restarted / accessible across pages
class ReportViewModel : ViewModel() {
    private val _reports = mutableStateListOf<Report>()
    val reports: List<Report> = _reports

    fun addReport(type: String, description: String, severity: Int) {
        val report = Report(
            type = type,
            description = description,
            severity = severity
        )
        _reports.add(report)
    }

    fun getLatestReport(): Report? = _reports.maxByOrNull { it.timestamp }
}
