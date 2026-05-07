package dk.itu.moapd.x9.myta.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
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
    val uid: String = "",
    val type: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val description: String = "",
    val severity: Int = 1,
    val timestamp: Long = 0L,
    val imageUrl: String? = null
)

data class UiLocation(
    val latitude: Double? = null,
    val longitude: Double? = null
)

class ReportViewModel(
    private val repository: ReportRepository = ReportRepository()
) : ViewModel() {
    private val _currentLocation = MutableStateFlow(UiLocation())
    val currentLocation: StateFlow<UiLocation> = _currentLocation.asStateFlow()

    fun updateCurrentLocation(latitude: Double?, longitude: Double?) {
        _currentLocation.value = UiLocation(
            latitude = latitude,
            longitude = longitude
        )
    }
    
    fun clearCurrentLocation() {
        _currentLocation.value = UiLocation()
    }
    
    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()
    
    private var listener: ValueEventListener? = null

    init {
        observeReports()
    }

    fun observeReports() {
        stopObserving()

        val query = repository.reportsQuery()

        val valueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { child ->
                    val childKey = child.key ?: return@mapNotNull null
                    val report = child.getValue(Report::class.java) ?: return@mapNotNull null
                    report.copy(key = childKey)
                }.sortedByDescending { it.timestamp }
                _reports.update { items }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ReportViewModel", "Database error: ${error.message}")
            }
        }

        listener = valueListener
        query.addValueEventListener(valueListener)
    }

    private fun stopObserving() {
        val l = listener
        if (l != null) {
            repository.reportsQuery().removeEventListener(l)
            listener = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopObserving()
    }

    fun addReport(type: String, description: String, severity: Int, latitude: Double?, longitude: Double?, imageUri: Uri? = null) {
        val userId = repository.currentUserId() ?: return
        if (imageUri != null) {
            repository.uploadImage(userId, imageUri) { downloadUrl ->
                repository.insertReport(
                    userId = userId,
                    type = type,
                    description = description,
                    severity = severity,
                    latitude = latitude,
                    longitude = longitude,
                    imageUrl = downloadUrl
                )
            }
        } else {
            repository.insertReport(
                userId = userId,
                type = type,
                description = description,
                severity = severity,
                latitude = latitude,
                longitude = longitude,
                imageUrl = null
            )
        }
    }

    fun deleteReport(key: String) {
        val userId = repository.currentUserId() ?: return
        repository.deleteReport(userId = userId, key = key)
    }
}
