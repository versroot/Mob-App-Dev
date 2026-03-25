package dk.itu.moapd.x9.myta

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class Report(
    val type: String,
    val description: String,
    val severity: Int,
    val timestamp: Long = System.currentTimeMillis()
)

// Viewmodel: persistent storage of data even if activities are restarted / accessible across pages
class ReportViewModel : ViewModel() {
    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()
    fun addReport(type: String, description: String, severity: Int) {
        _reports.update { current ->
            current + Report(type = type, description = description, severity =
                severity)
        }
    }
    fun getLatestReport(): Report? = _reports.value.maxByOrNull { it.timestamp }
}
