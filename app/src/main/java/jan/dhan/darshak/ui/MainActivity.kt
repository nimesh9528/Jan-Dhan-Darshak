package jan.dhan.darshak.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.bhardwaj.navigation.SlideGravity
import com.bhardwaj.navigation.SlidingRootNav
import com.bhardwaj.navigation.SlidingRootNavBuilder
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.card.MaterialCardView
import jan.dhan.darshak.R
import jan.dhan.darshak.databinding.ActivityMainBinding
import jan.dhan.darshak.viewmodels.MainActivityViewModel


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<MaterialCardView>
    private lateinit var slidingRootNavBuilder: SlidingRootNav

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.isLoading.value
            }
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialise()
        clickListeners()
    }

    private fun clickListeners() {
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.main -> {
                    Toast.makeText(applicationContext, "Main", Toast.LENGTH_SHORT).show()
                }

                R.id.branch -> {
                    Toast.makeText(applicationContext, "Branch", Toast.LENGTH_SHORT).show()
                }

                R.id.post_office -> {
                    Toast.makeText(applicationContext, "Post Office", Toast.LENGTH_SHORT).show()
                }

                R.id.csc -> {
                    Toast.makeText(applicationContext, "CSC", Toast.LENGTH_SHORT).show()
                }

                R.id.bank_mitra -> {
                    Toast.makeText(applicationContext, "Bank Mitra", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
        binding.ivVoiceSearch.setOnClickListener {
            Toast.makeText(applicationContext, "Voice Search Icon", Toast.LENGTH_SHORT).show()
        }
        binding.ivMenu.setOnClickListener {
            if (slidingRootNavBuilder.isMenuOpened)
                slidingRootNavBuilder.closeMenu(true)
            else
                slidingRootNavBuilder.openMenu(true)
        }
        binding.mcvLayerContainer.setOnClickListener {
            Toast.makeText(applicationContext, "Map Type Icon", Toast.LENGTH_SHORT).show()
        }
        binding.mcvNorthFacingContainer.setOnClickListener {
            Toast.makeText(applicationContext, "Face North Icon", Toast.LENGTH_SHORT).show()
        }
        binding.mcvCurrentContainer.setOnClickListener {
            Toast.makeText(applicationContext, "Current Location Icon", Toast.LENGTH_SHORT).show()
        }
        binding.mcvDirectionContainer.setOnClickListener {
            Toast.makeText(applicationContext, "Directions Icon", Toast.LENGTH_SHORT).show()
        }
        binding.mcvRelevanceContainer.setOnClickListener {
            val linearLayout = binding.llFilter
            val linearLayoutChildCount = linearLayout.childCount

            for (i in 0 until linearLayoutChildCount) {
                val materialCardView = linearLayout.getChildAt(i) as MaterialCardView
                val textView = materialCardView.getChildAt(0) as TextView

                if (textView.text == resources.getString(R.string.relevance))
                    textView.setTextColor(resources.getColor(R.color.blue_color, theme))
                else
                    textView.setTextColor(resources.getColor(R.color.black, theme))
            }
        }
        binding.mcvDistanceContainer.setOnClickListener {
            val linearLayout = binding.llFilter
            val linearLayoutChildCount = linearLayout.childCount

            for (i in 0 until linearLayoutChildCount) {
                val materialCardView = linearLayout.getChildAt(i) as MaterialCardView
                val textView = materialCardView.getChildAt(0) as TextView

                if (textView.text == resources.getString(R.string.distance))
                    textView.setTextColor(resources.getColor(R.color.blue_color, theme))
                else
                    textView.setTextColor(resources.getColor(R.color.black, theme))
            }
        }
        binding.mcvOpenNowContainer.setOnClickListener {
            val linearLayout = binding.llFilter
            val linearLayoutChildCount = linearLayout.childCount

            for (i in 0 until linearLayoutChildCount) {
                val materialCardView = linearLayout.getChildAt(i) as MaterialCardView
                val textView = materialCardView.getChildAt(0) as TextView

                if (textView.text == resources.getString(R.string.open_now))
                    textView.setTextColor(resources.getColor(R.color.blue_color, theme))
                else
                    textView.setTextColor(resources.getColor(R.color.black, theme))
            }
        }
        binding.mcvTopRatedContainer.setOnClickListener {
            val linearLayout = binding.llFilter
            val linearLayoutChildCount = linearLayout.childCount

            for (i in 0 until linearLayoutChildCount) {
                val materialCardView = linearLayout.getChildAt(i) as MaterialCardView
                val textView = materialCardView.getChildAt(0) as TextView

                if (textView.text == resources.getString(R.string.top_rated))
                    textView.setTextColor(resources.getColor(R.color.blue_color, theme))
                else
                    textView.setTextColor(resources.getColor(R.color.black, theme))
            }
        }
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.mcvDirectionContainer
                    .animate()
                    .scaleX(1 - slideOffset)
                    .scaleY(1 - slideOffset)
                    .alpha(1 - slideOffset * 2)
                    .setDuration(0)
                    .start()

                binding.mcvCurrentContainer
                    .animate()
                    .scaleX(1 - slideOffset)
                    .scaleY(1 - slideOffset)
                    .alpha(1 - slideOffset * 2)
                    .setDuration(0)
                    .start()

                if (binding.mcvDirectionContainer.alpha <= 0) {
                    binding.mcvDirectionContainer.isClickable = false
                    binding.mcvDirectionContainer.isFocusable = false

                    binding.mcvCurrentContainer.isClickable = false
                    binding.mcvCurrentContainer.isFocusable = false
                } else {
                    binding.mcvDirectionContainer.isClickable = true
                    binding.mcvDirectionContainer.isFocusable = true

                    binding.mcvCurrentContainer.isClickable = true
                    binding.mcvCurrentContainer.isFocusable = true
                }
            }
        })

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                when {
                    s.isNullOrEmpty() -> {
                        binding.ivSearchIcon.visibility = View.VISIBLE
                        binding.ivCloseIcon.visibility = View.GONE
                    }
                    else -> {
                        binding.ivSearchIcon.visibility = View.GONE
                        binding.ivCloseIcon.visibility = View.VISIBLE
                    }
                }
            }
        })
        binding.ivCloseIcon.setOnClickListener {
            binding.etSearch.setText("")
        }
    }

    private fun initialise() {
        slidingRootNavBuilder = SlidingRootNavBuilder(this)
            .withMenuOpened(false)
            .withGravity(SlideGravity.RIGHT)
            .withMenuLayout(R.layout.navigation_drawer)
            .inject()

        bottomSheetBehavior = BottomSheetBehavior.from(binding.mcvBottomSheetContainer)
    }
}