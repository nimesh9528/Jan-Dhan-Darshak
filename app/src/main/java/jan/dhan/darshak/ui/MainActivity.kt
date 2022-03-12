package jan.dhan.darshak.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
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

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment?
        val navController = navHostFragment!!.navController
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController)

        SlidingRootNavBuilder(this)
            .withMenuOpened(false)
            .withGravity(SlideGravity.RIGHT)
            .withMenuLayout(R.layout.navigation_drawer)
            .inject()
    }
}