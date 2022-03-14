package jan.dhan.darshak.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import jan.dhan.darshak.R
import jan.dhan.darshak.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
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
    private var lastKnownLocation: Location? = null
    private var lastCameraPosition: CameraPosition? = null
    private var locationPermissionGranted = false

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

        currentLocation = LatLng(28.6089031, 77.2115711)

        Places.initialize(this@MainActivity, getString(R.string.maps_api_key))
        placesClient = Places.createClient(this@MainActivity)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this@MainActivity)

        (supportFragmentManager.findFragmentById(R.id.fragment_google_maps) as SupportMapFragment).getMapAsync(
            this@MainActivity
        )

        voiceResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val spokenText =
                        result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

                    if (!spokenText.isNullOrEmpty()) {
                        binding.etSearch.setText(spokenText[0])
                        mGoogleMap.clear()
                        selectedMarker = null
                    }
                }
            }
    }

    private fun clickListeners() {
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.main -> {
                    binding.etSearch.setText(R.string.atm)
                }

                R.id.branch -> {
                    binding.etSearch.setText(R.string.branch)
                }

                R.id.post_office -> {
                    binding.etSearch.setText(R.string.post_office)
                }

                R.id.csc -> {
                    binding.etSearch.setText(R.string.csc)
                }

                R.id.bank_mitra -> {
                    binding.etSearch.setText(R.string.bank_mitra)
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
            val cameraPosition = lastKnownLocation?.let { it1 ->
                CameraPosition
                    .builder(mGoogleMap.cameraPosition)
                    .bearing(it1.bearing)
                    .build()
            }
            cameraPosition?.let { it1 ->
                CameraUpdateFactory.newCameraPosition(
                    it1
                )
            }?.let { it2 -> mGoogleMap.animateCamera(it2) }
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

            mGoogleMap.isMyLocationEnabled = true

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
            Toast.makeText(this@MainActivity, "Favourites", Toast.LENGTH_SHORT).show()
            slidingRootNavBuilder.closeMenu(true)
        }

        slidingRootNavLayout.findViewById<TextView>(R.id.tvMissingBank)?.setOnClickListener {
            Toast.makeText(this@MainActivity, "Missing Bank", Toast.LENGTH_SHORT).show()
            slidingRootNavBuilder.closeMenu(true)
        }

        slidingRootNavLayout.findViewById<TextView>(R.id.tvFeedback)?.setOnClickListener {
            Toast.makeText(this@MainActivity, "Feedback", Toast.LENGTH_SHORT).show()
            slidingRootNavBuilder.closeMenu(true)
        }

        slidingRootNavLayout.findViewById<TextView>(R.id.tvHelp)?.setOnClickListener {
            Toast.makeText(this@MainActivity, "Help", Toast.LENGTH_SHORT).show()
            slidingRootNavBuilder.closeMenu(true)
        }

        slidingRootNavLayout.findViewById<TextView>(R.id.tvAboutUs)?.setOnClickListener {
            Toast.makeText(this@MainActivity, "About Us", Toast.LENGTH_SHORT).show()
            slidingRootNavBuilder.closeMenu(true)
        }

        slidingRootNavLayout.findViewById<TextView>(R.id.tvDisclaimer)?.setOnClickListener {
            Toast.makeText(this@MainActivity, "Disclaimer", Toast.LENGTH_SHORT).show()
            slidingRootNavBuilder.closeMenu(true)
        }

        slidingRootNavLayout.findViewById<ImageView>(R.id.ivCloseButton)?.setOnClickListener {
            slidingRootNavBuilder.closeMenu(true)
        }

        binding.ivPinnedDirectionIcon.setOnClickListener {
            Toast.makeText(this@MainActivity, "Directions", Toast.LENGTH_SHORT).show()
        }

        binding.ivPinnedCallIcon.setOnClickListener {
            Toast.makeText(this@MainActivity, "Call", Toast.LENGTH_SHORT).show()
        }

        binding.ivPinnedSaveIcon.setOnClickListener {
            Toast.makeText(this@MainActivity, "Save", Toast.LENGTH_SHORT).show()
        }

        binding.ivPinnedSpeak.setOnClickListener {
            Toast.makeText(this@MainActivity, "Speak Out Load", Toast.LENGTH_SHORT).show()
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
            selectedMarkerLocation = marker.position
            selectedMarker = marker

            if (previousSelectedMarker != null)
                previousSelectedMarker?.setIcon(bitmapFromVector(R.drawable.icon_marker))

            selectedMarker?.setIcon(bitmapFromVector(R.drawable.icon_marker_selected))
            previousSelectedMarker = selectedMarker

            true
        }
        mGoogleMap.setOnMapClickListener {
            if (selectedMarker != null)
                selectedMarker?.setIcon(bitmapFromVector(R.drawable.icon_marker))

            selectedMarkerLocation = currentLocation
            selectedMarker = null
            previousSelectedMarker = null
        }

        getLocationPermission()
        updateLocationUI()
        getDeviceLocation()
    }

    private fun updateLocationUI() {
        try {
            if (locationPermissionGranted) {
                mGoogleMap.isMyLocationEnabled = true
                mGoogleMap.uiSettings.isMyLocationButtonEnabled = true
            } else {
                mGoogleMap.isMyLocationEnabled = false
                mGoogleMap.uiSettings.isMyLocationButtonEnabled = false
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
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this@MainActivity) { task ->
                    if (task.isSuccessful) {
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            currentLocation = LatLng(task.result.latitude, task.result.longitude)
                            mGoogleMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ), DEFAULT_ZOOM
                                )
                            )
                        }
                    } else {
                        mGoogleMap.moveCamera(
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
}