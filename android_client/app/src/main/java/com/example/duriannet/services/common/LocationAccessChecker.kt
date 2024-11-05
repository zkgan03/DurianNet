package com.example.duriannet.services.common

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher

class LocationAccessChecker(
    val context: Context,
) {

    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
//
//    val isGpsEnabled: Boolean
//        get() = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
//
//    var onGPSClosed: (() -> Unit)? = null

    fun observeGpsStatus(): Flow<Boolean> = callbackFlow {

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            close(SecurityException("Location permission not granted"))
            return@callbackFlow
        }


        val gpsStatusListener = object : LocationListener {

            override fun onProviderEnabled(provider: String) {
                super.onProviderEnabled(provider)
                trySend(true)
            }

            override fun onProviderDisabled(provider: String) {
                super.onProviderDisabled(provider)
                trySend(false)
            }

            override fun onLocationChanged(location: android.location.Location) {
                // Do nothing
            }

        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, gpsStatusListener)

        // Emit the initial GPS status
        trySend(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))


        awaitClose {
            locationManager.removeUpdates(gpsStatusListener)
        }

    }


//    private val gpsStatusReceiver: BroadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
//                if (!isGpsEnabled) {
//                    onGPSClosed?.invoke()
//                }
//            }
//        }
//    }
//
//    fun registerLocationReceiver() {
//        context.registerReceiver(
//            gpsStatusReceiver,
//            IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
//        )
//    }
//
//    fun unregisterLocationReceiver() {
//        gpsStatusReceiver.let {
//            context.unregisterReceiver(it)
//        }
//    }

}