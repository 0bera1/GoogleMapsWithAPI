package com.example.mapsproject

import android.Manifest
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.mapsproject.databinding.ActivityMapsBinding
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback ,GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    var followBoolean : Boolean? = null
    private lateinit var SharedPreferences : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        registerLauncher()

        SharedPreferences = getSharedPreferences("com.example.mapsproject", MODE_PRIVATE)
        followBoolean = false
    }


    @SuppressLint("ShowToast")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)
 /*
        // Add a marker in Sydney and move the camera
        val egeBilMuh = LatLng(38.4580060289213, 27.212962053908527)
        mMap.addMarker(MarkerOptions().position(egeBilMuh).title("Marker in Ege Univerity Computer Engineering Department"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(egeBilMuh,18f))
    */
        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                followBoolean = SharedPreferences.getBoolean("followBoolean",false)
                if (!followBoolean!!){
                    mMap.clear()
                    val userLocation = LatLng(location.latitude,location.longitude)
                    mMap.addMarker(MarkerOptions().position(userLocation).title("Your Location"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,18f))
                    SharedPreferences.edit().putBoolean("followBoolean",true).apply()
                }

            }
        }

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
            //İzin verilmemişse
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                Snackbar.make(binding.root,"İzin gerekli",Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver"){
                    //izin isteyeceğiz
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }.show()
            }else {
                // izin isteyeceğiz
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }else{
            //izin verilmişse
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1,1f,locationListener)
            val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastKnownLocation != null) {
                val lastLatLng = LatLng(lastKnownLocation.latitude,lastKnownLocation.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng,18f))
            }
        }

    }
    private fun registerLauncher(){
        permissionLauncher=registerForActivityResult(ActivityResultContracts.RequestPermission()){result ->
            if (result){
                if (ContextCompat.checkSelfPermission(this@MapsActivity,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (lastKnownLocation != null) {
                        val lastLatLng = LatLng(lastKnownLocation.latitude,lastKnownLocation.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng,18f))
                    }
                }
            }else {
                //izin verilmemişse
                Toast.makeText(this@MapsActivity,"You need Permission! ",Toast.LENGTH_LONG).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onMapLongClick(p0: LatLng) {
        mMap.clear()

        //geocoder
        val geocoder = Geocoder(this,Locale.getDefault())
        var address = ""

        try {

            geocoder.getFromLocation(p0.latitude,p0.longitude,1,Geocoder.GeocodeListener { addressList ->
                val firstAddress = addressList.first()
                val countryName = firstAddress.countryName
                val streetName = firstAddress.thoroughfare
                val num = firstAddress.subThoroughfare

                address += num
                address += streetName
                println(address)

            })

        }catch (e: Exception){
            e.printStackTrace()
        }
        mMap.addMarker(MarkerOptions().position(p0).title("Your Selected Location"))
    }

}
// 38.4580060289213, 27.212962053908527