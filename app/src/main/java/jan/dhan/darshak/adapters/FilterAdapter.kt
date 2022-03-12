package jan.dhan.darshak.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import jan.dhan.darshak.R


class FilterAdapter(
    private val dataSet: List<String>,
    private val onClick: (TextView) -> Unit
) :
    RecyclerView.Adapter<FilterAdapter.ViewHolder>() {

    class ViewHolder(view: View, val onClick: (TextView) -> Unit) : RecyclerView.ViewHolder(view) {
        private val tvFilter: TextView = view.findViewById(R.id.tvFilter)

        fun bind(filterText: String) {
            tvFilter.text = filterText
        }

        init {
            tvFilter.setOnClickListener {
                onClick(it as TextView)
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.single_filter, viewGroup, false)

        return ViewHolder(view, onClick)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(dataSet[position])
    }

    override fun getItemCount() = dataSet.size
}
