package jan.dhan.darshak.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jan.dhan.darshak.data.Location
import jan.dhan.darshak.data.LocationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: LocationRepository
) : ViewModel() {
    val allLocation = repository.allLocation.asLiveData()

    fun insertLocation(location: Location) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertLocation(location = location)
        }
    }

    fun deleteLocation(location: Location) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteLocation(location = location)
        }
    }
}