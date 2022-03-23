package jan.dhan.darshak.ui.activity

import android.Manifest
import android.animation.Animator
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bhardwaj.navigation.SlideGravity
import com.bhardwaj.navigation.SlidingRootNav
import com.bhardwaj.navigation.SlidingRootNavBuilder
import com.bhardwaj.navigation.SlidingRootNavLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import jan.dhan.darshak.R
import jan.dhan.darshak.adapter.PlacesAdapter
import jan.dhan.darshak.databinding.ActivityMainBinding
import jan.dhan.darshak.data.Location
import jan.dhan.darshak.data.NearbyPointsApi
import jan.dhan.darshak.ui.viewmodels.MainViewModel
import jan.dhan.darshak.ui.fragments.ExplanationFragment
import jan.dhan.darshak.ui.fragments.FormFragment
import jan.dhan.darshak.ui.fragments.LanguageFragment
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject
import android.location.Location as Locals

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnMapReadyCallback, TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<MaterialCardView>
    private lateinit var slidingRootNavBuilder: SlidingRootNav
    private lateinit var slidingRootNavLayout: SlidingRootNavLayout
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var mGoogleMap: GoogleMap
    private lateinit var currentLocation: LatLng
    private var selectedMarker: Marker? = null
    private var previousSelectedMarker: Marker? = null
    private var selectedMarkerLocation: LatLng? = null
    private lateinit var voiceResult: ActivityResultLauncher<Intent>
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var lastKnownLocation: Locals? = null
    private var lastCameraPosition: CameraPosition? = null
    private var locationPermissionGranted = false
    private lateinit var apiKey: String
    private lateinit var currentLanguage: String
    private var selectedCategory: String = "atm"
    private var selectedFilter: String = "prominence"
    private var clickedFromBottomNavigation: Boolean = false
    private var placesList: ArrayList<HashMap<String?, String?>?> = arrayListOf()
    private lateinit var placesAdapter: PlacesAdapter
    private var textToSpeech: TextToSpeech? = null
    private val explanationFragment = ExplanationFragment()
    private val languageFragment = LanguageFragment()
    private val formFragment = FormFragment()
    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var googlePlaces: NearbyPointsApi

    companion object {
        private const val DEFAULT_ZOOM = 14F
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private const val KEY_LOCATION = "location"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            lastCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }

        initialise()
        clickListeners()
    }

    private fun initialise() {
        currentLanguage = Locale.getDefault().language
        apiKey = getString(R.string.maps_api_key)

        slidingRootNavBuilder = SlidingRootNavBuilder(this)
            .withMenuOpened(false)
            .withDragDistance(225)
            .withRootViewScale(0.85F)
            .withGravity(SlideGravity.RIGHT)
            .withMenuLayout(R.layout.navigation_drawer)
            .inject()

        slidingRootNavLayout = slidingRootNavBuilder.layout!!
        bottomSheetBehavior = BottomSheetBehavior.from(binding.mcvBottomSheetContainer)
        bottomSheetDialog = BottomSheetDialog(this@MainActivity)

        Places.initialize(this@MainActivity, apiKey)
        placesClient = Places.createClient(this@MainActivity)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this@MainActivity)

        (supportFragmentManager.findFragmentById(R.id.fragment_google_maps) as SupportMapFragment).getMapAsync(
            this@MainActivity
        )

        textToSpeech = TextToSpeech(this@MainActivity, this@MainActivity)

        voiceResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val spokenText =
                        result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

                    if (!spokenText.isNullOrEmpty()) {
                        binding.etSearch.setText(spokenText[0])
                        selectedMarker = null
                    }
                }
            }

        placesAdapter = PlacesAdapter(this@MainActivity, placesList) { location ->
            mainViewModel.insertLocation(location)
        }

        binding.rvLocationList.also {
            it.layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            it.adapter = placesAdapter
            it.overScrollMode = View.OVER_SCROLL_NEVER
            it.addItemDecoration(
                DividerItemDecoration(this@MainActivity, DividerItemDecoration.VERTICAL)
            )
        }
    }

    private fun clickListeners() {
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.main -> {
                    selectedCategory = "atm"
                    selectedMarker = null
                    clickedFromBottomNavigation = true

                    binding.etSearch.setText(R.string.atm)
                    if (selectedFilter == "openNow") {
                        getNearbyPointsFromAPI(
                            type = "atm",
                            radius = 10000,
                            openNow = "true"
                        )
                    } else {
                        getNearbyPointsFromAPI(
                            type = "atm",
                            radius = if (selectedFilter == "distance") null else 10000,
                            rankBy = selectedFilter
                        )
                    }
                }

                R.id.branch -> {
                    selectedCategory = "bank"
                    selectedMarker = null
                    clickedFromBottomNavigation = true

                    binding.etSearch.setText(R.string.branch)
                    if (selectedFilter == "openNow") {
                        getNearbyPointsFromAPI(
                            type = "bank",
                            radius = 10000,
                            openNow = "true"
                        )
                    } else {
                        getNearbyPointsFromAPI(
                            type = "bank",
                            radius = if (selectedFilter == "distance") null else 10000,
                            rankBy = selectedFilter
                        )
                    }
                }

                R.id.post_office -> {
                    selectedCategory = "post_office"
                    selectedMarker = null
                    clickedFromBottomNavigation = true

                    binding.etSearch.setText(R.string.post_office)
                    if (selectedFilter == "openNow") {
                        getNearbyPointsFromAPI(
                            type = "post_office",
                            radius = 10000,
                            openNow = "true"
                        )
                    } else {
                        getNearbyPointsFromAPI(
                            type = "post_office",
                            radius = if (selectedFilter == "distance") null else 10000,
                            rankBy = selectedFilter
                        )
                    }
                }

                R.id.csc -> {
                    selectedCategory = "Jan Seva Kendra"
                    selectedMarker = null
                    clickedFromBottomNavigation = true

                    binding.etSearch.setText(R.string.csc)
                    if (selectedFilter == "openNow") {
                        getNearbyPointsFromAPI(
                            keyword = "csc",
                            radius = if (selectedFilter == "distance") null else 10000,
                            openNow = "true"
                        )
                    } else {
                        getNearbyPointsFromAPI(
                            keyword = "csc",
                            radius = if (selectedFilter == "distance") null else 10000,
                            rankBy = selectedFilter
                        )
                    }
                }

                R.id.bank_mitra -> {
                    selectedCategory = "Bank Mitra"
                    selectedMarker = null
                    clickedFromBottomNavigation = true

                    binding.etSearch.setText(R.string.bank_mitra)
                    if (selectedFilter == "openNow") {
                        getNearbyPointsFromAPI(
                            keyword = "Bank Mitra",
                            radius = if (selectedFilter == "distance") null else 10000,
                            openNow = "true"
                        )
                    } else {
                        getNearbyPointsFromAPI(
                            keyword = "Bank Mitra",
                            radius = if (selectedFilter == "distance") null else 10000,
                            rankBy = selectedFilter
                        )
                    }
                }
            }
            true
        }

        binding.ivVoiceSearch.setOnClickListener {
            voiceResult.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
            })
        }

        binding.ivMenu.setOnClickListener {
            if (slidingRootNavBuilder.isMenuOpened)
                slidingRootNavBuilder.closeMenu(true)
            else
                slidingRootNavBuilder.openMenu(true)
        }

        binding.mcvNorthFacingContainer.setOnClickListener {
            val cameraPosition = CameraPosition
                .builder(mGoogleMap.cameraPosition)
                .bearing(lastKnownLocation!!.bearing)
                .build()
            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }

        binding.mcvCurrentContainer.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) return@setOnClickListener

            val cameraPosition = CameraPosition
                .builder(mGoogleMap.cameraPosition)
                .zoom(DEFAULT_ZOOM)
                .target(LatLng(currentLocation.latitude, currentLocation.longitude))
                .build()
            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }

        binding.mcvDirectionContainer.setOnClickListener {
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=" + selectedMarkerLocation?.latitude + "," + selectedMarkerLocation?.longitude)
            ).also {
                it.`package` = "com.google.android.apps.maps"
                if (it.resolveActivity(packageManager) != null)
                    startActivity(it)
            }
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

            selectedFilter = "prominence"
            val typeFilter: List<String> = listOf("atm", "branch", "post office")

            if (typeFilter.contains(binding.etSearch.text.toString().lowercase())) {
                getNearbyPointsFromAPI(
                    type = selectedCategory,
                    radius = 10000,
                    rankBy = selectedFilter
                )
            } else {
                getNearbyPointsFromAPI(
                    keyword = binding.etSearch.text.toString().lowercase(),
                    radius = 10000,
                    rankBy = selectedFilter
                )
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

            selectedFilter = "distance"

            val typeFilter: List<String> = listOf("atm", "branch", "post office")

            if (typeFilter.contains(binding.etSearch.text.toString().lowercase())) {
                getNearbyPointsFromAPI(
                    type = selectedCategory,
                    rankBy = selectedFilter
                )
            } else {
                getNearbyPointsFromAPI(
                    keyword = binding.etSearch.text.toString().lowercase(),
                    rankBy = selectedFilter
                )
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

            selectedFilter = "openNow"

            val typeFilter: List<String> = listOf("atm", "branch", "post office")

            if (typeFilter.contains(binding.etSearch.text.toString().lowercase())) {
                getNearbyPointsFromAPI(
                    type = selectedCategory,
                    radius = 10000,
                    openNow = "true"
                )
            } else {
                getNearbyPointsFromAPI(
                    keyword = binding.etSearch.text.toString().lowercase(),
                    openNow = "true",
                    radius = 10000,
                )
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

            selectedFilter = "prominence"

            val typeFilter: List<String> = listOf("atm", "branch", "post office")

            if (typeFilter.contains(binding.etSearch.text.toString().lowercase())) {
                getNearbyPointsFromAPI(
                    type = selectedCategory,
                    radius = 10000,
                    rankBy = selectedFilter
                )
            } else {
                getNearbyPointsFromAPI(
                    keyword = binding.etSearch.text.toString().lowercase(),
                    radius = 10000,
                    rankBy = selectedFilter
                )
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

                        if (!clickedFromBottomNavigation) {
                            if (selectedFilter == "openNow") {
                                getNearbyPointsFromAPI(
                                    keyword = s.toString().lowercase(),
                                    radius = if (selectedFilter == "distance") null else 10000,
                                    openNow = "true"
                                )
                            } else {
                                getNearbyPointsFromAPI(
                                    keyword = s.toString().lowercase(),
                                    radius = if (selectedFilter == "distance") null else 10000,
                                    rankBy = selectedFilter
                                )
                            }
                        } else clickedFromBottomNavigation = false
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
                mGoogleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.findViewById<TextView>(R.id.tvSatelliteMapType)?.setOnClickListener {
                mGoogleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.findViewById<TextView>(R.id.tvTerrainMapType)?.setOnClickListener {
                mGoogleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.findViewById<ImageView>(R.id.ivCloseButton)?.setOnClickListener {
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()
        }

        slidingRootNavLayout.findViewById<TextView>(R.id.tvFavouriteLocation)?.setOnClickListener {
            slidingRootNavBuilder.closeMenu(true)

            explanationFragment.arguments = bundleOf(
                "heading" to getString(R.string.favourite_locations),
                "description" to "Description",
                "recyclerView" to true,
            )
            explanationFragment.show(supportFragmentManager, "favourite locations")
        }

        slidingRootNavLayout.findViewById<TextView>(R.id.tvMissingBank)?.setOnClickListener {
            slidingRootNavBuilder.closeMenu(true)

            formFragment.arguments = bundleOf(
                "heading" to getString(R.string.missing_bank),
            )
            formFragment.show(supportFragmentManager, "missing bank")
        }

        slidingRootNavLayout.findViewById<TextView>(R.id.tvFeedback)?.setOnClickListener {
            slidingRootNavBuilder.closeMenu(true)

            formFragment.arguments = bundleOf(
                "heading" to getString(R.string.feedback)
            )
            formFragment.show(supportFragmentManager, "feedback")
        }

        slidingRootNavLayout.findViewById<TextView>(R.id.tvHelp)?.setOnClickListener {
            slidingRootNavBuilder.closeMenu(true)

            explanationFragment.arguments = bundleOf(
                "heading" to getString(R.string.faq),
                "description" to "Description",
                "recyclerView" to true,
            )
            explanationFragment.show(supportFragmentManager, "Faq")
        }

        slidingRootNavLayout.findViewById<TextView>(R.id.tvAboutUs)?.setOnClickListener {
            slidingRootNavBuilder.closeMenu(true)

            explanationFragment.arguments = bundleOf(
                "heading" to getString(R.string.about_us),
                "description" to getString(R.string.about_us_text),
                "recyclerView" to false,
            )
            explanationFragment.show(supportFragmentManager, "about_us")
        }

        slidingRootNavLayout.findViewById<TextView>(R.id.tvDisclaimer)?.setOnClickListener {
            slidingRootNavBuilder.closeMenu(true)

            explanationFragment.arguments = bundleOf(
                "heading" to getString(R.string.disclaimer),
                "description" to getString(R.string.disclaimer_text),
                "recyclerView" to false,
            )
            explanationFragment.show(supportFragmentManager, "disclaimer")
        }

        slidingRootNavLayout.findViewById<TextView>(R.id.tvChangeLanguage)?.setOnClickListener {
            slidingRootNavBuilder.closeMenu(true)
            languageFragment.show(supportFragmentManager, "change language")
        }

        slidingRootNavLayout.findViewById<ImageView>(R.id.ivCloseButton)?.setOnClickListener {
            slidingRootNavBuilder.closeMenu(true)
        }
    }

    private fun bitmapFromVector(vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(this@MainActivity, vectorResId)
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )

        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        mGoogleMap.setOnMarkerClickListener { marker ->
            if (marker.title != "Current Location") {
                selectedMarkerLocation = marker.position
                selectedMarker = marker

                if (previousSelectedMarker != null)
                    previousSelectedMarker?.setIcon(bitmapFromVector(R.drawable.icon_marker))

                selectedMarker?.setIcon(bitmapFromVector(R.drawable.icon_marker_selected))
                previousSelectedMarker = selectedMarker
                fetchPinnedData(marker.snippet)
            }
            true
        }
        mGoogleMap.setOnMapClickListener {
            if (selectedMarker != null)
                selectedMarker?.setIcon(bitmapFromVector(R.drawable.icon_marker))

            if (selectedMarker?.title == "Current Location")
                selectedMarker?.setIcon(bitmapFromVector(R.drawable.icon_current_location))

            selectedMarkerLocation = currentLocation
            selectedMarker = null
            previousSelectedMarker = null
            hideAndShowPinnedLocation()
        }

        getLocationPermission()
        updateLocationUI()
        getDeviceLocation()
    }

    private fun fetchPinnedData(pinnedId: String?) {
        hideShowProgressBar(showProgressbar = true)

        val placeFields = listOf(
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.RATING,
            Place.Field.PHONE_NUMBER,
            Place.Field.USER_RATINGS_TOTAL,
            Place.Field.OPENING_HOURS,
            Place.Field.UTC_OFFSET,
            Place.Field.WEBSITE_URI
        )
        val request = FetchPlaceRequest.newInstance(pinnedId!!, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response: FetchPlaceResponse ->
                val place = response.place
                binding.tvPinnedHeading.text = place.name?.toString()
                binding.tvPinnedAddress.text = place.address?.toString()
                binding.rbPinnedRatings.rating = place.rating?.toString()?.toFloat() ?: 0F

                val userRatingCount =
                    if (place.userRatingsTotal?.toString() == "null") "" else "(${place.userRatingsTotal?.toString()})"
                binding.tvPinnedRatingCount.text = userRatingCount

                val open = if (place.isOpen == true)
                    "<font color=\"${
                        resources.getColor(R.color.green_color, theme)
                    }\">${resources.getString(R.string.open_now)}</font>"
                else
                    "<font color=\"${
                        resources.getColor(R.color.navigationSelected, theme)
                    }\">${resources.getString(R.string.closed)}</font>"

                val compatOpen =
                    if (place.isOpen == true) resources.getString(R.string.open_now) else resources.getString(
                        R.string.closed
                    )

                var close = ""
                val closesTimings =
                    place.openingHours?.periods?.get(0)?.close?.time?.hours.toString()
                if (closesTimings.isNotEmpty() && closesTimings != "null") {
                    close = if (closesTimings.toInt() > 12)
                        "· ${resources.getString(R.string.closes)} ${closesTimings.toInt() % 12} PM"
                    else
                        "· ${resources.getString(R.string.closes)} ${closesTimings.toInt() % 12} AM"
                }

                val timings = "$open $close"
                val compatTimings = "$compatOpen $close"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    binding.tvPinnedTimings.setText(
                        Html.fromHtml(timings, HtmlCompat.FROM_HTML_MODE_LEGACY),
                        TextView.BufferType.SPANNABLE
                    )
                } else {
                    binding.tvPinnedTimings.text = compatTimings
                }

                binding.ivPinnedDirectionIcon.setOnClickListener {
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("google.navigation:q=${selectedMarkerLocation?.latitude},${selectedMarkerLocation?.longitude}")
                    ).also {
                        it.`package` = "com.google.android.apps.maps"
                        if (it.resolveActivity(packageManager) != null)
                            startActivity(it)
                    }
                }

                binding.ivPinnedCallIcon.setOnClickListener {
                    if (!place.phoneNumber?.toString().isNullOrEmpty())
                        startActivity(
                            Intent(
                                Intent.ACTION_DIAL,
                                Uri.parse("tel:${place.phoneNumber?.toString()}")
                            )
                        )
                    else
                        Toast.makeText(
                            this@MainActivity,
                            "Phone number not Provided.",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                }

                binding.ivPinnedSaveIcon.setOnClickListener {
                    mainViewModel.insertLocation(
                        Location(
                            id = pinnedId,
                            name = place.name,
                            address = place.address,
                            latitude = selectedMarkerLocation?.latitude.toString(),
                            longitude = selectedMarkerLocation?.longitude.toString(),
                            open = place.isOpen?.toString(),
                            close = close,
                            rating = place.rating?.toString(),
                            ratingCount = place.userRatingsTotal?.toString(),
                            phoneNumber = place.phoneNumber?.toString(),
                            website = place.websiteUri?.toString(),
                            timeStamp = System.currentTimeMillis()
                        )
                    )
                    Toast.makeText(this@MainActivity, "Saved", Toast.LENGTH_SHORT).show()
                }

                binding.ivPinnedSpeak.setOnClickListener {
                    sayOutLoud("${binding.tvPinnedHeading.text}")
                }

                hideAndShowPinnedLocation()
                hideShowProgressBar(showProgressbar = false)

            }.addOnFailureListener {
                hideShowProgressBar(showProgressbar = false)
            }
    }

    private fun hideAndShowPinnedLocation() {
        if (selectedMarker != null) {
            binding.mcvPinnedContainer.visibility = View.VISIBLE

            binding.bottomNavigation
                .animate()
                .alpha(0.0F)
                .setDuration(500)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {}
                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
                        binding.bottomNavigation.visibility = View.GONE
                    }
                })

            binding.mcvBottomSheetContainer
                .animate()
                .alpha(0.0F)
                .setDuration(500)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {}
                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
                        binding.mcvBottomSheetContainer.visibility = View.GONE
                    }
                })

            binding.mcvCurrentContainer
                .animate()
                .alpha(0.0F)
                .setDuration(500)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {}
                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
                        binding.mcvCurrentContainer.visibility = View.GONE
                    }
                })

            binding.mcvDirectionContainer
                .animate()
                .alpha(0.0F)
                .setDuration(500)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {}
                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
                        binding.mcvDirectionContainer.visibility = View.GONE
                    }
                })

            binding.mcvLayerContainer
                .animate()
                .alpha(0.0F)
                .setDuration(500)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {}
                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
                        binding.mcvLayerContainer.visibility = View.GONE
                    }
                })

            binding.mcvNorthFacingContainer
                .animate()
                .alpha(0.0F)
                .setDuration(500)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {}
                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
                        binding.mcvNorthFacingContainer.visibility = View.GONE
                    }
                })

        } else {
            binding.mcvPinnedContainer.visibility = View.GONE

            binding.bottomNavigation
                .animate()
                .alpha(1.0F)
                .setDuration(500)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {}
                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
                        binding.bottomNavigation.visibility = View.VISIBLE
                    }
                })

            binding.mcvBottomSheetContainer
                .animate()
                .alpha(1.0F)
                .setDuration(500)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {}
                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
                        binding.mcvBottomSheetContainer.visibility = View.VISIBLE
                    }
                })

            binding.mcvCurrentContainer
                .animate()
                .alpha(1.0F)
                .setDuration(500)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {}
                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
                        binding.mcvCurrentContainer.visibility = View.VISIBLE
                    }
                })

            binding.mcvDirectionContainer
                .animate()
                .alpha(1.0F)
                .setDuration(500)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {}
                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
                        binding.mcvDirectionContainer.visibility = View.VISIBLE
                    }
                })

            binding.mcvLayerContainer
                .animate()
                .alpha(1.0F)
                .setDuration(500)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {}
                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
                        binding.mcvLayerContainer.visibility = View.VISIBLE
                    }
                })

            binding.mcvNorthFacingContainer
                .animate()
                .alpha(1.0F)
                .setDuration(500)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {}
                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
                        binding.mcvNorthFacingContainer.visibility = View.VISIBLE
                    }
                })
        }
    }

    private fun updateLocationUI() {
        try {
            if (!locationPermissionGranted) {
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this@MainActivity) { task ->
                    if (task.isSuccessful) {
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            currentLocation = LatLng(task.result.latitude, task.result.longitude)

                            val markerOptions = MarkerOptions()
                            markerOptions.position(currentLocation)
                            markerOptions.title("Current Location")
                            markerOptions.icon(bitmapFromVector(R.drawable.icon_current_location))
                            mGoogleMap.addMarker(markerOptions)

                            mGoogleMap.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ), DEFAULT_ZOOM
                                )
                            )

                            val linearLayout = binding.llFilter
                            val linearLayoutChildCount = linearLayout.childCount

                            for (i in 0 until linearLayoutChildCount) {
                                val materialCardView =
                                    linearLayout.getChildAt(i) as MaterialCardView
                                val textView = materialCardView.getChildAt(0) as TextView

                                if (textView.text == resources.getString(R.string.relevance))
                                    textView.setTextColor(
                                        resources.getColor(
                                            R.color.blue_color,
                                            theme
                                        )
                                    )
                                else
                                    textView.setTextColor(resources.getColor(R.color.black, theme))
                            }

                            if (selectedFilter == "openNow") {
                                getNearbyPointsFromAPI(
                                    type = selectedCategory,
                                    radius = if (selectedFilter == "distance") null else 10000,
                                    openNow = "true"
                                )
                            } else {
                                getNearbyPointsFromAPI(
                                    type = selectedCategory,
                                    radius = if (selectedFilter == "distance") null else 10000,
                                    rankBy = selectedFilter
                                )
                            }
                        }
                    } else {
                        mGoogleMap.animateCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(currentLocation, DEFAULT_ZOOM)
                        )
                        mGoogleMap.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mGoogleMap.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }

    private fun getNearbyPointsFromAPI(
        keyword: String? = null,
        openNow: String? = null,
        type: String? = null,
        rankBy: String? = null,
        radius: Int? = null,
        location: String = "${currentLocation.latitude} ${currentLocation.longitude}",
        language: String = currentLanguage,
        api: String = apiKey
    ) {
        hideShowProgressBar(showProgressbar = true)
        mGoogleMap.clear()
        placesList.clear()
        previousSelectedMarker = null

        placesAdapter.removeAll()

        if (placesList.size > 0) {
            binding.rvLocationList.visibility = View.VISIBLE
            binding.ivNoDataIcon.visibility = View.GONE
        } else {
            binding.rvLocationList.visibility = View.GONE
            binding.ivNoDataIcon.visibility = View.VISIBLE
        }


        val call = googlePlaces.getPlaces(
            keyword = keyword,
            openNow = openNow,
            type = type,
            location = location,
            language = language,
            rankBy = rankBy,
            radius = radius,
            api = api
        )

        Log.d("ADITYA", "getNearbyPointsFromAPI: ${call.request().url()}")

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>?, response: Response<String>?) {
                if (response != null) {
                    showOnMap(response.body())
                }
            }

            override fun onFailure(call: Call<String>?, t: Throwable?) {
                hideShowProgressBar(showProgressbar = false)
                Log.d("ADITYA", "FAIL 1: $t")
            }
        })
    }

    fun showOnMap(data: String?) {
        var array: JSONArray? = null
        val single: JSONObject
        try {
            single = JSONObject(data!!)
            array = single.getJSONArray("results")
        } catch (e: JSONException) {
            hideShowProgressBar(showProgressbar = false)
            e.printStackTrace()
        }

        (0 until array?.length()!!).forEach { i ->
            val map = HashMap<String?, String?>()

            try {
                val json = array[i] as JSONObject
                map["id"] = json.getString("place_id")

                val placeFields = listOf(
                    Place.Field.OPENING_HOURS,
                    Place.Field.UTC_OFFSET,
                    Place.Field.PHONE_NUMBER,
                    Place.Field.WEBSITE_URI
                )
                val request = FetchPlaceRequest.newInstance(map["id"].toString(), placeFields)

                placesClient.fetchPlace(request)
                    .addOnSuccessListener { response: FetchPlaceResponse ->
                        val place = response.place
                        map["name"] = if (!json.isNull("name")) json.getString("name") else ""
                        map["address"] =
                            if (!json.isNull("vicinity")) json.getString("vicinity") else ""
                        map["latitude"] = json.getJSONObject("geometry").getJSONObject("location")
                            .getString("lat")
                        map["longitude"] = json.getJSONObject("geometry").getJSONObject("location")
                            .getString("lng")
                        map["rating"] =
                            if (!json.isNull("rating")) json.getString("rating") else "0"
                        map["ratingCount"] =
                            if (!json.isNull("user_ratings_total")) json.getString("user_ratings_total") else "0"
                        map["open"] = if (!place.isOpen?.toString()
                                .isNullOrEmpty()
                        ) place.isOpen?.toString() else "false"

                        val closesTimings =
                            place.openingHours?.periods?.get(0)?.close?.time?.hours.toString()
                        if (closesTimings.isNotEmpty() && closesTimings != "null") {
                            if (closesTimings.toInt() > 12)
                                map["close"] =
                                    "· ${resources.getString(R.string.closes)} ${closesTimings.toInt() % 12} PM"
                            else
                                map["close"] =
                                    "· ${resources.getString(R.string.closes)} ${closesTimings.toInt() % 12} AM"
                        } else {
                            map["close"] = ""
                        }
                        map["phoneNumber"] = if (!place.phoneNumber?.toString()
                                .isNullOrEmpty()
                        ) place.phoneNumber?.toString() else ""
                        map["website"] = if (!place.websiteUri?.toString()
                                .isNullOrEmpty()
                        ) place.websiteUri?.toString() else ""

                        placesList.add(map)

                        placesAdapter.updateList(currentLocation, placesList.size - 1)

                        if (placesList.size > 0) {
                            binding.rvLocationList.visibility = View.VISIBLE
                            binding.ivNoDataIcon.visibility = View.GONE
                        } else {
                            binding.rvLocationList.visibility = View.GONE
                            binding.ivNoDataIcon.visibility = View.VISIBLE
                        }

                        val markerOptions = MarkerOptions()
                        markerOptions.position(
                            LatLng(
                                map["latitude"]!!.toDouble(),
                                map["longitude"]!!.toDouble()
                            )
                        )
                        markerOptions.title(map["name"])
                        markerOptions.snippet(map["id"])
                        markerOptions.icon(bitmapFromVector(R.drawable.icon_marker))
                        val marker = mGoogleMap.addMarker(markerOptions)

                        if (!map["open"].isNullOrEmpty()) {
                            marker?.tag = map["open"] + " " + map["id"]
                        }
                    }.addOnFailureListener {
                        hideShowProgressBar(showProgressbar = false)
                    }
            } catch (e: JSONException) {
                hideShowProgressBar(showProgressbar = false)
                e.printStackTrace()
            }
        }
        mGoogleMap.addMarker(
            MarkerOptions().position(currentLocation).title("Current Location")
                .icon(bitmapFromVector(R.drawable.icon_current_location))
        )
        hideShowProgressBar(showProgressbar = false)
    }

    fun zoomToCurrentSelectedPlace(location: LatLng) {
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 20F))
    }

    fun hideShowProgressBar(showProgressbar: Boolean) {
        if (showProgressbar) {
            binding.progressBar.visibility = View.VISIBLE
            binding.ivMenu.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.ivMenu.visibility = View.VISIBLE
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        updateLocationUI()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech!!.language = Locale.getDefault()
        }
    }

    fun sayOutLoud(message: String) {
        textToSpeech!!.speak(message, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onDestroy() {
        if (textToSpeech != null) {
            textToSpeech!!.stop()
            textToSpeech!!.shutdown()
        }
        super.onDestroy()
    }
}