package jan.dhan.darshak.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jan.dhan.darshak.adapter.FaqAdapter
import jan.dhan.darshak.databinding.ExplanationSheetsBinding
import jan.dhan.darshak.modals.Faq


class ExplanationFragment : BottomSheetDialogFragment() {

    private var _binding: ExplanationSheetsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ExplanationSheetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = requireArguments().getBoolean("recyclerView")
        binding.rvFaqs.visibility = if (recyclerView) View.VISIBLE else View.GONE

        val heading = requireArguments().getString("heading")
        binding.tvExplanationHeading.text = heading

        val description = requireArguments().getString("description")
        binding.tvDescription.text = description
        binding.tvDescription.visibility = if (recyclerView) View.GONE else View.VISIBLE

        if (recyclerView) {
            val faqList = arrayListOf(
                Faq(
                    number = "1",
                    question = "Alright, but what exactly do your app do?",
                    description = "Jan Dhan Darshak, a mobile application launched to provide a citizen- centric platform for locating banking touch points such as bank branches, ATMs, Bank Correspondents (BC), Post Offices, etc. in the country."
                ),
                Faq(
                    number = "2",
                    question = "Need for the app?",
                    description = "While locator apps are a common feature for many individual banks and financial service providers, in this era of inter-operable banking services, Jan Dhan Darshak app will be in a unique position to provide a citizen centric platform for locating financial service touch points across all providers such as banks, post office, CSC, etc. These services could be availed as per the needs and convenience of the common people."
                ),
                Faq(
                    number = "3",
                    question = "What is CSC?",
                    description = "Common Service Centres (CSC) scheme is one of the mission mode projects under the Digital India Programme. CSCs are the access points for delivery of essential public utility services, social welfare schemes, healthcare, financial, education and agriculture services, apart from host of B2C services to citizens in rural and remote areas of the country. It is a pan-India network catering to regional, geographic, linguistic and cultural diversity of the country, thus enabling the Government’s mandate of a socially, financially and digitally inclusive society."
                ),
                Faq(
                    number = "4",
                    question = "What is Bank Mitra?",
                    description = "A Bank Mitra acts like an agent in facilitating bank and banking related services, especially in unbanked areas of the country. They help in areas where there is no ATMs and branches of banks. A Bank Mitra acts like an agent in facilitating bank and banking related services, especially in unbanked areas of the country. They help in areas where there is no ATMs and branches of banks"
                ),
                Faq(
                    number = "5",
                    question = "Under which Initiative this application Launched?",
                    description = "The Department of Financial Services (DFS), Ministry of Finance and National Informatics Centre (NIC) has jointly developed a mobile app called Jan Dhan Darshak as a part of financial inclusion (FI) initiative. As the name suggests, this app will act as a guide for the common people in locating a financial service touch point at a given location in the country."
                ),
                Faq(
                    number = "6",
                    question = "How can I find ATM's nearby that are open?",
                    description = "First Search ATM and then select the filter below it - Open Now. Now all the results will be filtered to your specific needs."
                ),
                Faq(
                    number = "7",
                    question = "How can I add missing bank details?",
                    description = "To add missing bank details. First go to Navigation Drawer, then select Missing bank Option. Fill the Form and click Send Suggestion. Now the details will be processed by higher authorities."
                ),
                Faq(
                    number = "8",
                    question = "What is N symbol on map represent?",
                    description = "N symbol represents north sign. On clicking it automatically rotates the map to point north side."
                ),
            )

            val faqAdapter = FaqAdapter(faqList)

            binding.rvFaqs.also {
                it.layoutManager =
                    LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
                it.adapter = faqAdapter
                it.addItemDecoration(
                    DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
                )
            }
        }

        binding.ivCloseButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}