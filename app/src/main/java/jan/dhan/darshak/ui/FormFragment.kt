package jan.dhan.darshak.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jan.dhan.darshak.R
import jan.dhan.darshak.databinding.FormSheetsBinding


class FormFragment : BottomSheetDialogFragment() {

    private var _binding: FormSheetsBinding? = null
    private val binding get() = _binding!!
    private lateinit var dialog: BottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FormSheetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val heading = requireArguments().getString("heading")
        binding.tvExplanationHeading.text = heading

        if (heading == getString(R.string.missing_bank)) {
            binding.tvSendButton.text = getString(R.string.send_suggestion)

            binding.tvEmailIdHeading.visibility = View.VISIBLE
            binding.etEmailId.visibility = View.VISIBLE
            binding.viewEmailId.visibility = View.VISIBLE

            binding.tvBankNameHeading.visibility = View.VISIBLE
            binding.etBankName.visibility = View.VISIBLE
            binding.viewBankName.visibility = View.VISIBLE

            binding.tvSuggestionHeading.visibility = View.GONE
            binding.etSuggestion.visibility = View.GONE
            binding.viewSuggestion.visibility = View.GONE
        } else {
            binding.tvSendButton.text = getString(R.string.send_feedback)

            binding.tvEmailIdHeading.visibility = View.GONE
            binding.etEmailId.visibility = View.GONE
            binding.viewEmailId.visibility = View.GONE

            binding.tvBankNameHeading.visibility = View.GONE
            binding.etBankName.visibility = View.GONE
            binding.viewBankName.visibility = View.GONE

            binding.tvSuggestionHeading.visibility = View.VISIBLE
            binding.etSuggestion.visibility = View.VISIBLE
            binding.viewSuggestion.visibility = View.VISIBLE
        }

        binding.ivCloseButton.setOnClickListener {
            dismiss()
        }

        binding.tvSendButton.setOnClickListener {
            Toast.makeText(context, "Send Button Clicked.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}