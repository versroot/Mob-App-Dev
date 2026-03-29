package dk.itu.moapd.x9.myta.viewmodel

import android.util.Log
import androidx.compose.runtime.key
import androidx.lifecycle.ViewModel
import androidx.room.util.copy
import kotlinx.coroutines.flow.MutableStateFlow
import dk.itu.moapd.x9.myta.repository.ReportRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.String

data class Report(
    val key: String = "",
    val type: String = "",
    val description: String = "",
    val severity: Int = 1,
    val timestamp: Long = 0L
)

data class ReportUi(
    val key: String,
    val type: String,
    val description: String,
    val severity: Int,
    val timestamp: Long?
)

// Viewmodel: persistent storage of data even if activities are restarted / accessible across pages
class ReportViewModel(
    private val repository: ReportRepository = ReportRepository()
) : ViewModel() {
    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()
    // The listener for Firebase Realtime Database
    private var listener: ValueEventListener? = null

    init {
        // Start observing as soon as we have a user.
        observeReports()
    }

    private fun observeReports() {
        val userId = repository.currentUserId() ?: return
        val query = repository.reportsQuery(userId)

        val valueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Map the Firebase snapshot back into a Kotlin List<Report>
                val items = snapshot.children.mapNotNull { child ->
                    val childKey = child.key ?: return@mapNotNull null
                    val report = child.getValue(Report::class.java) ?: return@mapNotNull null
                    report.copy(
                        key = childKey
                    )
                }.sortedByDescending { it.timestamp }
                _reports.update { items }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ReportViewModel", "Database error: ${error.message}")
            }
        }
        // Update the listener and add it to the query.
        listener = valueListener
        query.addValueEventListener(valueListener)
    }

    override fun onCleared() {
        super.onCleared()
        val userId = repository.currentUserId()
        val l = listener
        if (userId != null && l != null) {
            repository.reportsQuery(userId).removeEventListener(l)
        }
    }

    fun addReport(type: String, description: String, severity: Int) {
        val userId = repository.currentUserId() ?: return
        repository.insertReport(
            userId = userId,
            type = type,
            description = description,
            severity = severity
        )
    }
    fun getLatestReport(): Report? = _reports.value.maxByOrNull { it.timestamp }

    fun deleteReport(key: String) {
        val userId = repository.currentUserId() ?: return
        repository.deleteReport(userId = userId, key = key)
    }
}
