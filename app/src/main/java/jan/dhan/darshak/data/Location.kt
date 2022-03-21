package jan.dhan.darshak.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_table")
data class Location(
    @PrimaryKey val id: String,
    val name: String?,
    val address: String?,
    val latitude: String?,
    val longitude: String?,
    val open: String?,
    val close: String?,
    val rating: String?,
    val ratingCount: String?,
    val phoneNumber: String?,
    val website: String?,
    val timeStamp: Long
)