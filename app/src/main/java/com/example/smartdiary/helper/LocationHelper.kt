package com.smartdiary.helper

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class LocationHelper(private val context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        onSuccess: (latitude: Double, longitude: Double) -> Unit,
        onError: () -> Unit
    ) {
        val cts = CancellationTokenSource()
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    onSuccess(location.latitude, location.longitude)
                } else {
                    // tenta última localização conhecida como fallback
                    fusedClient.lastLocation
                        .addOnSuccessListener { last ->
                            if (last != null) onSuccess(last.latitude, last.longitude)
                            else onError()
                        }
                        .addOnFailureListener { onError() }
                }
            }
            .addOnFailureListener { onError() }
    }
}