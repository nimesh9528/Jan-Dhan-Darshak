package jan.dhan.darshak.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jan.dhan.darshak.adapter.LanguageAdapter
import jan.dhan.darshak.data.Language
import jan.dhan.darshak.databinding.LanguageSheetBinding


class LanguageFragment : BottomSheetDialogFragment() {

    private var _binding: LanguageSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LanguageSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val languageList = arrayListOf(
            Language(
                languageId = "en",
                languageName = "English",
                languageNameInEnglish = "English",
                selected = true
            ),
            Language(
                languageId = "hi",
                languageName = "हिंदी",
                languageNameInEnglish = "Hindi",
                selected = false
            ),
            Language(
                languageId = "gu",
                languageName = "ગુજરાતી",
                languageNameInEnglish = "Gujarati",
                selected = false
            ),
            Language(
                languageId = "ta",
                languageName = "தமிழ்",
                languageNameInEnglish = "Tamil",
                selected = false
            ),
            Language(
                languageId = "te",
                languageName = "తెలుగు",
                languageNameInEnglish = "Telegu",
                selected = false
            ),
            Language(
                languageId = "kn",
                languageName = "ಕನ್ನಡ",
                languageNameInEnglish = "Kannada",
                selected = false
            ),
            Language(
                languageId = "ml",
                languageName = "മലയാളം",
                languageNameInEnglish = "Malayalam",
                selected = false
            ),
            Language(
                languageId = "mr",
                languageName = "मराठी",
                languageNameInEnglish = "Marathi",
                selected = false
            ),
            Language(
                languageId = "ne",
                languageName = "नेपाली",
                languageNameInEnglish = "Nepali",
                selected = false
            ),
            Language(
                languageId = "pa",
                languageName = "ਪੰਜਾਬੀ",
                languageNameInEnglish = "Punjabi",
                selected = false
            ),
            Language(
                languageId = "ur",
                languageName = "اردو",
                languageNameInEnglish = "Urdu",
                selected = false
            )
        )

        val languageAdapter = LanguageAdapter(languageList) {
            Toast.makeText(context, "${it.languageId}", Toast.LENGTH_SHORT).show()
        }

        binding.rvLanguages.also {
            it.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            it.adapter = languageAdapter
            it.addItemDecoration(
                DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
            )
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