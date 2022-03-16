package jan.dhan.darshak.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import jan.dhan.darshak.R
import jan.dhan.darshak.adapter.PlacesAdapter.PlacesViewHolder
import jan.dhan.darshak.ui.MainActivity


class PlacesAdapter(
    private val mContext: Context,
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
        val rbRatings: RatingBar = itemView.findViewById(R.id.rbRatings)
        val tvRatingCount: TextView = itemView.findViewById(R.id.tvRatingCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlacesViewHolder {
        return PlacesViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.single_location, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PlacesViewHolder, position: Int) {
        val place = places[position]
        val heading = place?.get("name")
        val address = place?.get("address")
        val latitude = place?.get("latitude")?.toDouble()
        val longitude = place?.get("longitude")?.toDouble()
        val rating = place?.get("rating")
        val ratingCount = "(${place?.get("ratingCount")})"
        val open = if (place?.get("open").toBoolean())
            "<font color=\"${
                mContext.resources.getColor(
                    R.color.green_color,
                    mContext.theme
                )
            }\">${mContext.resources.getString(R.string.open_now)}</font>"
        else
            "<font color=\"${
                mContext.resources.getColor(
                    R.color.navigationSelected,
                    mContext.theme
                )
            }\">${mContext.resources.getString(R.string.closed)}</font>"

        val compatOpen = if (place?.get("open").toBoolean()) mContext.resources.getString(R.string.open_now) else mContext.resources.getString(R.string.closed)
        val close = if (!place?.get("close")
                .isNullOrEmpty() && place?.get("close") != "null"
        ) "${place?.get("close")}" else ""

        val timings = "$open $close"
        val compatTimings = "$compatOpen $close"
        val phoneNumber = place?.get("phoneNumber")

        holder.tvResultHeading.text = heading
        holder.tvAddress.text = address
        holder.rbRatings.rating = rating?.toFloat() ?: 4F
        holder.tvRatingCount.text = ratingCount
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.tvTimings.setText(
                Html.fromHtml(timings, HtmlCompat.FROM_HTML_MODE_LEGACY),
                TextView.BufferType.SPANNABLE
            )
        } else {
            holder.tvTimings.text = compatTimings
        }

        holder.ivSaveIcon.setOnClickListener {
            Toast.makeText(mContext, "Save Button Clicked", Toast.LENGTH_SHORT).show()
        }

        holder.ivSpeak.setOnClickListener {
            (mContext as MainActivity).sayOutLoud("$heading")
        }

        holder.ivShareIcon.setOnClickListener {
            Intent(Intent.ACTION_SEND).also {
                it.type = "text/plain"
                it.putExtra(Intent.EXTRA_SUBJECT, "Location")
                it.putExtra(
                    Intent.EXTRA_TEXT,
                    "${heading}\n$address\nhttps://www.google.co.id/maps/@$latitude,$longitude"
                )
                mContext.startActivity(Intent.createChooser(it, "Share using:"))
            }
        }

        holder.clSinglePlace.setOnClickListener {
            (mContext as MainActivity).zoomToCurrentSelectedPlace(LatLng(latitude!!, longitude!!))
        }

        holder.ivCallIcon.setOnClickListener {
            if (!phoneNumber.isNullOrEmpty())
                mContext.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")))
            else
                Toast.makeText(mContext, "Phone number not Provided.", Toast.LENGTH_SHORT).show()
        }

        holder.ivDirectionIcon.setOnClickListener {
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=$latitude,$longitude")
            ).also {
                it.`package` = "com.google.android.apps.maps"
                if (it.resolveActivity(mContext.packageManager) != null)
                    mContext.startActivity(it)
            }
        }
    }

    override fun getItemCount() = places.size

    fun updateList(location: LatLng, position: Int) {
        currentLocation = location
        notifyItemInserted(position)
    }

    fun removeAll() {
        places.clear()
        notifyDataSetChanged()
    }
}