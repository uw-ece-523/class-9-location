package edu.uw.ee523.locationdemo

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import edu.uw.ee523.locationdemo.databinding.ActivityMainBinding
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var locationCallback: LocationCallback
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Ask for permissions
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(ACCESS_FINE_LOCATION, false) -> {
                    // Precise location access granted.
                    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                }
                permissions.getOrDefault(ACCESS_COARSE_LOCATION, false) -> {
                    // Only approximate location access granted.
                } else -> {
                // No location access granted.
            }
            }
        }



        if (ActivityCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION))
            return
        }

        // Get the FusedLocationProviderClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0 ?: return
                for (location in p0.locations){
                    // Update UI with location data
                    binding.latitudeText.text = "lat: " + location?.latitude.toString()
                    binding.longitudeText.text = "long: " + location?.longitude.toString()
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    fun startUpdatesButtonHandler(view: View) {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        mFusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback, Looper.getMainLooper())
    }

    fun stopUpdatesButtonHandler(view: View) {
        mFusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    fun fetchAddressButtonHandler(view: View) {
        mFusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                binding.latitudeText.text = "lat: " + location?.latitude.toString()
                binding.longitudeText.text = "long: " + location?.longitude.toString()

                if (location != null) {
                    getAddress(location)
                }
            }

        /*
        val cts = CancellationTokenSource()
        mFusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                binding.latitudeText.text = "lat: " + location?.latitude.toString()
                binding.longitudeText.text = "long: " + location?.longitude.toString()

                if (location != null) {
                    getAddress(location)
                }
            }
        */
    }

    fun getAddress(location: Location) {
        val geocoder = Geocoder(this, Locale.getDefault())
        // Address found using the Geocoder.
        var addresses: List<Address>? = null

        try {
            // Using getFromLocation() returns an array of Addresses for the area immediately
            // surrounding the given latitude and longitude. The results are a best guess and are
            // not guaranteed to be accurate.
            addresses = geocoder.getFromLocation(
                location.getLatitude(),
                location.getLongitude(),  // In this sample, we get just a single address.
                5
            )
        } catch (ioException: IOException) {
            // Catch network or other I/O problems.
            binding.addressTextView.text = "service not available"

        } catch (illegalArgumentException: IllegalArgumentException) {
            // Catch invalid latitude or longitude values.
            binding.addressTextView.text = "invalid lat/lng"

        }

        // Handle case where no address was found.

        // Handle case where no address was found.
        if (addresses == null || addresses.size == 0) {
            if (binding.addressTextView.text.isEmpty()) {
                binding.addressTextView.text = "No address fond" //getString(R.string.no_address_found);
            }

        } else {
            Log.i("ADDRESS", addresses.toString())
            val address = addresses[0]
            val addressFragments = ArrayList<String?>()

            // Fetch the address lines using {@code getAddressLine},
            // join them, and send them to the thread. The {@link android.location.address}
            // class provides other options for fetching address details that you may prefer
            // to use. Here are some examples:
            // getLocality() ("Mountain View", for example)
            // getAdminArea() ("CA", for example)
            // getPostalCode() ("94043", for example)
            // getCountryCode() ("US", for example)
            // getCountryName() ("United States", for example)
            for (i in 0..address.maxAddressLineIndex) {
                addressFragments.add(address.getAddressLine(i))
            }
            binding.addressTextView.text = addressFragments[0]
        }
    }
}
