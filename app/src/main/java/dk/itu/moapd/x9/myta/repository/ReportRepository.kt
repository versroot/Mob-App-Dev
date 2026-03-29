package dk.itu.moapd.x9.myta.repository

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import dk.itu.moapd.x9.myta.BuildConfig
import dk.itu.moapd.x9.myta.viewmodel.Report


class ReportRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val root: DatabaseReference = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_DATABASE_URL).reference,
) {
    companion object {
        private const val PATH_REPORTS = "reports"

        //The child key for the "timestamp" field in the database.
        private const val CHILD_TIMESTAMP = "timestamp"
    }
    fun currentUserId(): String? = auth.currentUser?.uid

    /**
     * Returns a query to retrieve all dummies for the current user.
     *
     * @param userId The ID of the user for whom to retrieve the dummies.
     *
     * @return A query to retrieve all dummies for the current user.
     */
    fun reportsQuery(userId: String): Query = root
        .child(PATH_REPORTS)
        .child(userId)
        .orderByChild(CHILD_TIMESTAMP)

    //Add/update/delete report to the database.
    fun insertReport(userId: String, type: String, description: String, severity: Int, now: Long = System.currentTimeMillis()) {
        val key = root
            .child(PATH_REPORTS)
            .child(userId)
            .push()
            .key ?: return
        val report = Report(type = type, description = description, severity = severity, timestamp = now)
        root
            .child(PATH_REPORTS)
            .child(userId)
            .child(key)
            .setValue(report)
    }
    fun updateReport(userId: String, key: String, type: String, description: String, severity: Int, now: Long = System.currentTimeMillis()) {
        val report = Report(type = type, description = description, severity = severity, timestamp = now)
        root
            .child(PATH_REPORTS)
            .child(userId)
            .child(key)
            .setValue(report)
    }
    fun deleteReport(userId: String, key: String) {
        root
            .child(PATH_REPORTS)
            .child(userId)
            .child(key)
            .removeValue()
    }
}