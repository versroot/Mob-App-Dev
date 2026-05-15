package dk.itu.moapd.x9.myta.viewmodel.model

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