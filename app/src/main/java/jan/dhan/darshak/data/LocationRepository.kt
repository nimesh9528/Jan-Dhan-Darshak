package jan.dhan.darshak.data

import javax.inject.Inject

class LocationRepository @Inject constructor(
    private val locationDao: LocationDao
) {
    val allLocation = locationDao.getLocations()
    suspend fun insertLocation(location: Location) = locationDao.insertLocation(location = location)
    suspend fun deleteLocation(location: Location) = locationDao.deleteLocation(location = location)
}