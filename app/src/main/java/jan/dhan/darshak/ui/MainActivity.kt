package jan.dhan.darshak.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.bhardwaj.navigation.SlideGravity
import com.bhardwaj.navigation.SlidingRootNavBuilder
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
    }

    private fun initialise() {
        SlidingRootNavBuilder(this)
            .withMenuOpened(false)
            .withGravity(SlideGravity.RIGHT)
            .withMenuLayout(R.layout.navigation_drawer)
            .inject()
    }
}