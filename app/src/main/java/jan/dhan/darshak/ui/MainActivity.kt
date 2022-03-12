package jan.dhan.darshak.ui

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.bhardwaj.navigation.SlideGravity
import com.bhardwaj.navigation.SlidingRootNavBuilder
import com.google.android.material.card.MaterialCardView
import jan.dhan.darshak.R
import jan.dhan.darshak.databinding.ActivityMainBinding
import jan.dhan.darshak.viewmodels.MainActivityViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityViewModel by viewModels()

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
        val slidingRootNavBuilder = SlidingRootNavBuilder(this)
            .withMenuOpened(false)
            .withGravity(SlideGravity.RIGHT)
            .withMenuLayout(R.layout.navigation_drawer)
            .inject()

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
    }

    private fun initialise() {
    }
}