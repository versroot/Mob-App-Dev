package dk.itu.moapd.x9.myta.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.storage.FirebaseStorage
import dk.itu.moapd.x9.myta.BuildConfig
import dk.itu.moapd.x9.myta.viewmodel.model.Report
class ReportRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val root: DatabaseReference = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_DATABASE_URL).reference,
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    companion object {
        private const val PATH_REPORTS = "reports"
        private const val CHILD_TIMESTAMP = "timestamp"
    }
    
    fun currentUserId(): String? = auth.currentUser?.uid

    fun reportsQuery(): Query = root
        .child(PATH_REPORTS)
        .orderByChild(CHILD_TIMESTAMP)


    fun insertReport(
        userId: String,
        type: String,
        description: String,
        severity: Int,
        latitude: Double?,
        longitude: Double?,
        imageUrl: String? = null,
        now: Long = System.currentTimeMillis()
    ) {
        val key = root
            .child(PATH_REPORTS)
            .push()
            .key ?: return
            
        val report = Report(
            uid = userId,
            type = type,
            description = description,
            severity = severity,
            latitude = latitude,
            longitude = longitude,
            imageUrl = imageUrl,
            timestamp = now
        )
        
        root
            .child(PATH_REPORTS)
            .child(key)
            .setValue(report)
    }

    fun uploadImage(userId: String, imageUri: Uri, onSuccess: (String) -> Unit) {
        val filename = "image_${System.currentTimeMillis()}.jpg"
        val storageRef = storage.reference
            .child("images")
            .child(userId)
            .child(filename)

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                Log.e("ReportRepository", "Upload failed", e)
            }
    }
    
    fun deleteReport(userId: String, key: String){
        root
            .child(PATH_REPORTS)
            .child(key)
            .removeValue()
    }
}
