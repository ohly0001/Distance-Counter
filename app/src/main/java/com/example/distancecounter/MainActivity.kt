package com.example.distancecounter

import android.Manifest
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.distancecounter.ui.theme.DistanceCounterTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

var currentlyDrawing = false

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var pathTracker: PathTracker

    private lateinit var sensorManager: SensorManager
    private lateinit var linearSensor: Sensor

    private fun registerSensor() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        linearSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)!!
        sensorManager.registerListener(pathTracker, linearSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    private fun requestLocationPermission() {
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) pathTracker = PathTracker()
        }.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun setContent() {
        enableEdgeToEdge()
        setContent {
            DistanceCounterTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    menu()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        requestLocationPermission()
        registerSensor()
    }


}

@Composable
fun menu() {
    val toggleText = remember { mutableStateOf("Start") }

    Column {
        Button(
            onClick = {

            }
        ) {
            Text("Add Point")
        }
        Button(
            onClick = {

            }
        ) {
            Text("Delete Point")
        }
        Button(
            onClick = {

            }
        ) {
            Text("Delete All Points")
        }
        Button(
            onClick = {

            }
        ) {
            Text("Join Path")
        }
        Button(
            onClick = {
                currentlyDrawing = !currentlyDrawing
                toggleText.value = if (currentlyDrawing) "Stop" else "Start"
            }
        ) {
            Text(text = "${toggleText.value} Drawing")
        }
    }
}