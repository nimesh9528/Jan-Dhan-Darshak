package jan.dhan.darshak.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.bhardwaj.navigation.SlideGravity
import com.bhardwaj.navigation.SlidingRootNav
import com.bhardwaj.navigation.SlidingRootNavBuilder
import com.bhardwaj.navigation.SlidingRootNavLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import jan.dhan.darshak.R
import jan.dhan.darshak.databinding.ActivityMainBinding
import jan.dhan.darshak.viewmodels.MainActivityViewModel


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<MaterialCardView>
    private lateinit var slidingRootNavBuilder: SlidingRootNav
    private lateinit var slidingRootNavlayout: SlidingRootNavLayout
    private lateinit var bottomSheetDialog: BottomSheetDialog

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
                    Toast.makeText(this@MainActivity, "Main", Toast.LENGTH_SHORT).show()
                }

                R.id.branch -> {
                    Toast.makeText(this@MainActivity, "Branch", Toast.LENGTH_SHORT).show()
                }

                R.id.post_office -> {
                    Toast.makeText(this@MainActivity, "Post Office", Toast.LENGTH_SHORT).show()
                }

                R.id.csc -> {
                    Toast.makeText(this@MainActivity, "CSC", Toast.LENGTH_SHORT).show()
                }

                R.id.bank_mitra -> {
                    Toast.makeText(this@MainActivity, "Bank Mitra", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }

        binding.ivVoiceSearch.setOnClickListener {
            Toast.makeText(this@MainActivity, "Voice Search Icon", Toast.LENGTH_SHORT).show()
        }

        binding.ivMenu.setOnClickListener {
            if (slidingRootNavBuilder.isMenuOpened)
                slidingRootNavBuilder.closeMenu(true)
            else
                slidingRootNavBuilder.openMenu(true)
        }

        binding.mcvLayerContainer.setOnClickListener {
            Toast.makeText(this@MainActivity, "Map Type Icon", Toast.LENGTH_SHORT).show()
        }

        binding.mcvNorthFacingContainer.setOnClickListener {
            Toast.makeText(this@MainActivity, "Face North Icon", Toast.LENGTH_SHORT).show()
        }

        binding.mcvCurrentContainer.setOnClickListener {
            Toast.makeText(this@MainActivity, "Current Location Icon", Toast.LENGTH_SHORT).show()
        }

        binding.mcvDirectionContainer.setOnClickListener {
            Toast.makeText(this@MainActivity, "Directions Icon", Toast.LENGTH_SHORT).show()
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

        binding.mcvLayerContainer.setOnClickListener {
            bottomSheetDialog.setContentView(R.layout.modal_bottom_sheet)
            bottomSheetDialog.setCanceledOnTouchOutside(false)

            bottomSheetDialog.findViewById<TextView>(R.id.tvDefaultMapType)?.setOnClickListener {
                Toast.makeText(this@MainActivity, "Default", Toast.LENGTH_SHORT).show()
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.findViewById<TextView>(R.id.tvSatelliteMapType)?.setOnClickListener {
                Toast.makeText(this@MainActivity, "Satellite", Toast.LENGTH_SHORT).show()
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.findViewById<TextView>(R.id.tvTerrainMapType)?.setOnClickListener {
                Toast.makeText(this@MainActivity, "Terrain", Toast.LENGTH_SHORT).show()
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.findViewById<ImageView>(R.id.ivCloseButton)?.setOnClickListener {
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()
        }

        slidingRootNavlayout.findViewById<TextView>(R.id.tvFavouriteLocation)?.setOnClickListener {
            Toast.makeText(this@MainActivity, "Favourites", Toast.LENGTH_SHORT).show()
            slidingRootNavBuilder.closeMenu(true)
        }

        slidingRootNavlayout.findViewById<TextView>(R.id.tvMissingBank)?.setOnClickListener {
            Toast.makeText(this@MainActivity, "Missing Bank", Toast.LENGTH_SHORT).show()
            slidingRootNavBuilder.closeMenu(true)
        }

        slidingRootNavlayout.findViewById<TextView>(R.id.tvFeedback)?.setOnClickListener {
            Toast.makeText(this@MainActivity, "Feedback", Toast.LENGTH_SHORT).show()
            slidingRootNavBuilder.closeMenu(true)
        }

        slidingRootNavlayout.findViewById<TextView>(R.id.tvHelp)?.setOnClickListener {
            Toast.makeText(this@MainActivity, "Help", Toast.LENGTH_SHORT).show()
            slidingRootNavBuilder.closeMenu(true)
        }

        slidingRootNavlayout.findViewById<TextView>(R.id.tvAboutUs)?.setOnClickListener {
            Toast.makeText(this@MainActivity, "About Us", Toast.LENGTH_SHORT).show()
            slidingRootNavBuilder.closeMenu(true)
        }

        slidingRootNavlayout.findViewById<TextView>(R.id.tvDisclaimer)?.setOnClickListener {
            Toast.makeText(this@MainActivity, "Disclaimer", Toast.LENGTH_SHORT).show()
            slidingRootNavBuilder.closeMenu(true)
        }

        slidingRootNavlayout.findViewById<ImageView>(R.id.ivCloseButton)?.setOnClickListener {
            slidingRootNavBuilder.closeMenu(true)
        }
    }

    private fun initialise() {
        slidingRootNavBuilder = SlidingRootNavBuilder(this)
            .withMenuOpened(false)
            .withGravity(SlideGravity.RIGHT)
            .withMenuLayout(R.layout.navigation_drawer)
            .inject()

        slidingRootNavlayout = slidingRootNavBuilder.layout!!
        bottomSheetBehavior = BottomSheetBehavior.from(binding.mcvBottomSheetContainer)
        bottomSheetDialog = BottomSheetDialog(this@MainActivity)
    }
}