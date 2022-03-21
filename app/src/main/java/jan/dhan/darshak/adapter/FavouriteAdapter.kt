package jan.dhan.darshak.adapter

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
import jan.dhan.darshak.data.Location
import jan.dhan.darshak.ui.activity.MainActivity


class FavouriteAdapter(
    private var places: ArrayList<Location>,
) : RecyclerView.Adapter<FavouriteAdapter.FavouriteViewHolder>() {

    inner class FavouriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteViewHolder {
        return FavouriteViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.single_location, parent, false)
        )
    }

    override fun onBindViewHolder(holder: FavouriteViewHolder, position: Int) {
        val place = places[position]
        val heading = place.name
        val address = place.address
        val latitude = place.latitude?.toDouble()
        val longitude = place.longitude?.toDouble()
        val rating = place.rating
        val ratingCount = "(${place.ratingCount})"
        val open = if (place.open.toBoolean())
            "<font color=\"${
                holder.itemView.context.resources.getColor(
                    R.color.green_color,
                    holder.itemView.context.theme
                )
            }\">${holder.itemView.context.resources.getString(R.string.open_now)}</font>"
        else
            "<font color=\"${
                holder.itemView.context.resources.getColor(
                    R.color.navigationSelected,
                    holder.itemView.context.theme
                )
            }\">${holder.itemView.context.resources.getString(R.string.closed)}</font>"

        val compatOpen = if (place.open
                .toBoolean()
        ) holder.itemView.context.resources.getString(R.string.open_now) else holder.itemView.context.resources.getString(R.string.closed)
        val close = if (!place.close
                .isNullOrEmpty() && place.close != "null"
        ) "${place.close}" else ""

        val timings = "$open $close"
        val compatTimings = "$compatOpen $close"
        val phoneNumber = place.phoneNumber

        holder.ivSaveIcon.visibility = View.GONE
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

        holder.ivSpeak.setOnClickListener {
            (holder.itemView.context as MainActivity).sayOutLoud("$heading")
        }

        holder.ivShareIcon.setOnClickListener {
            Intent(Intent.ACTION_SEND).also {
                it.type = "text/plain"
                it.putExtra(Intent.EXTRA_SUBJECT, "Location")
                it.putExtra(
                    Intent.EXTRA_TEXT,
                    "${heading}\n$address\nhttps://www.google.co.id/maps/@$latitude,$longitude"
                )
                holder.itemView.context.startActivity(Intent.createChooser(it, "Share using:"))
            }
        }

        holder.ivCallIcon.setOnClickListener {
            if (!phoneNumber.isNullOrEmpty())
                holder.itemView.context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")))
            else
                Toast.makeText(holder.itemView.context, "Phone number not Provided.", Toast.LENGTH_SHORT).show()
        }

        holder.ivDirectionIcon.setOnClickListener {
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=$latitude,$longitude")
            ).also {
                it.`package` = "com.google.android.apps.maps"
                if (it.resolveActivity(holder.itemView.context.packageManager) != null)
                    holder.itemView.context.startActivity(it)
            }
        }
    }

    override fun getItemCount() = places.size

    fun updateList(locations: List<Location>) {
        places.clear()
        places = locations as ArrayList
        notifyDataSetChanged()
    }
}