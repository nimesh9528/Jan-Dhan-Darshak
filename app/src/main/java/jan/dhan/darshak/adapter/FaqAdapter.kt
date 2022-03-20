package jan.dhan.darshak.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import jan.dhan.darshak.R
import jan.dhan.darshak.modals.Faq


class FaqAdapter(
    private val faq: ArrayList<Faq>
) : RecyclerView.Adapter<FaqAdapter.FaqViewHolder>() {

    inner class FaqViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFaqNumber: TextView = itemView.findViewById(R.id.tvFaqNumber)
        val tvFaqQuestion: TextView = itemView.findViewById(R.id.tvFaqQuestion)
        val tvFaqDescription: TextView = itemView.findViewById(R.id.tvFaqDescription)
        val ivHideButton: ImageView = itemView.findViewById(R.id.ivHideButton)
        val ivShowButton: ImageView = itemView.findViewById(R.id.ivShowButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        return FaqViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.single_faq, parent, false)
        )
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        val singleFaq = faq[position]

        holder.tvFaqNumber.text = singleFaq.number
        holder.tvFaqQuestion.text = singleFaq.question
        holder.tvFaqDescription.text = singleFaq.description

        holder.tvFaqQuestion.setOnClickListener {
            if (holder.ivHideButton.visibility == View.VISIBLE) {
                showAndHideDescription(holder, false)
            } else {
                showAndHideDescription(holder, true)
            }
        }

        holder.ivHideButton.setOnClickListener {
            showAndHideDescription(holder, false)
        }

        holder.ivShowButton.setOnClickListener {
            showAndHideDescription(holder, true)
        }
    }

    private fun showAndHideDescription(holder: FaqViewHolder, show: Boolean) {
        if (show) {
            holder.tvFaqDescription.visibility = View.VISIBLE

            holder.ivShowButton.also {
                it.visibility = View.INVISIBLE
                it.isClickable = false
                it.isFocusable = false
            }

            holder.ivHideButton.also {
                it.visibility = View.VISIBLE
                it.isClickable = true
                it.isFocusable = true
            }
        } else {
            holder.tvFaqDescription.visibility = View.GONE

            holder.ivHideButton.also {
                it.visibility = View.INVISIBLE
                it.isClickable = false
                it.isFocusable = false
            }
            holder.ivShowButton.also {
                it.visibility = View.VISIBLE
                it.isClickable = true
                it.isFocusable = true
            }
        }
    }

    override fun getItemCount() = faq.size
}