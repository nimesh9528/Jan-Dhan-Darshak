package jan.dhan.darshak.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import jan.dhan.darshak.R
import jan.dhan.darshak.adapter.PlacesAdapter.PlacesViewHolder

class PlacesAdapter(
    private val context: Context,
    private val places: ArrayList<HashMap<String?, String?>?>
) : RecyclerView.Adapter<PlacesViewHolder>() {

    var currentLocation: LatLng? = null

    inner class PlacesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val clSinglePlace: ConstraintLayout = itemView.findViewById(R.id.clSinglePlace)
        val tvResultHeading: TextView = itemView.findViewById(R.id.tvResultHeading)
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        val tvTimings: TextView = itemView.findViewById(R.id.tvTimings)
        val ivSpeak: ImageView = itemView.findViewById(R.id.ivSpeak)
        val ivDirectionIcon: ImageView = itemView.findViewById(R.id.ivDirectionIcon)
        val ivCallIcon: ImageView = itemView.findViewById(R.id.ivCallIcon)
        val ivSaveIcon: ImageView = itemView.findViewById(R.id.ivSaveIcon)
        val ivShareIcon: ImageView = itemView.findViewById(R.id.ivShareIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlacesViewHolder {
        return PlacesViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.single_location, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PlacesViewHolder, position: Int) {
        val googlePlace = places[position]
        val latitude = googlePlace?.get("lat")?.toDouble()
        val longitude = googlePlace?.get("lng")?.toDouble()
        val heading = googlePlace?.get("place_name")
        val location =
            if (latitude != null && longitude != null) LatLng(latitude, longitude) else null
        val address = googlePlace?.get("vicinity")

        if (heading != null)
            holder.tvResultHeading.text = heading

        if (address != null)
            holder.tvAddress.text = address
    }

    override fun getItemCount() = places.size

    fun updateList(newPlaces: ArrayList<HashMap<String?, String?>?>, location: LatLng) {
        places.clear()
        places.addAll(newPlaces)
        currentLocation = location
        notifyItemRangeInserted(0, newPlaces.size)
    }
}