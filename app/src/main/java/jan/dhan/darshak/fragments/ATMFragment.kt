package jan.dhan.darshak.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import jan.dhan.darshak.R
import jan.dhan.darshak.adapters.FilterAdapter
import jan.dhan.darshak.databinding.FragmentAtmBinding

class ATMFragment : Fragment() {
    private var _binding: FragmentAtmBinding? = null
    private val binding get() = _binding!!

    private lateinit var filterAdapter: FilterAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAtmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFilterOptions()
    }

    private fun setupFilterOptions() {
        val dataSets = listOf("Relevance", "Distance", "Open Now", "Top-Rated")
        filterAdapter = FilterAdapter(dataSet = dataSets) {
            onFilterClicked(it)
        }

        val recyclerView = binding.rvFilter
        recyclerView.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = filterAdapter
        recyclerView.overScrollMode = View.OVER_SCROLL_NEVER
    }

    private fun onFilterClicked(filter: TextView) {
        filter.setTypeface(filter.typeface, Typeface.BOLD)
        filter.setTextColor(resources.getColor(R.color.blue_color, activity?.theme))
        filterList()
    }

    private fun filterList() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}