package com.example.distancecounter

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.location.Location
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class PathTracker(
    private val epsilon: Float = 0.1f,       // m/s² threshold for "stop"
    private val angleThreshold: Float = 10f, // degrees for curve detection
    private val minDistance: Double = 0.5,   // meters
    private val autoCloseDistance: Double = 1.0,
    private val stopTimeThreshold: Long = 500 // ms
) : SensorEventListener {

    private var lastNode: PathNode? = null
    private var previousSegmentVector: DoubleArray? = null
    private val path = mutableListOf<PathNode>()

    private var isRunning = false
    private var lastStopTime: Long? = null

    var isAutoClosing = false

    private var lastLinearAccelMagnitude: Float = 0f

    fun getPath(): List<PathNode> = path.toList()
    fun getIsRunning(): Boolean = isRunning

    fun startRecording() {
        path.clear()
        lastNode = null
        previousSegmentVector = null
        lastStopTime = null
        isRunning = true
    }

    fun stopRecording() {
        isAutoClosing = false
        isRunning = false
    }

    fun quickClosePath() {
        if (!isRunning && path.isNotEmpty()) {
            path.add(path.first())
        }
        stopRecording()
    }

    private fun calculateDistance(p1: PathNode, p2: PathNode): Double {
        return sqrt(doubleArrayOf(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z).sumOf { it * it })
    }

    fun calculatePathDistance(): Double {
        var d = 0.0
        for (i in 0 until path.lastIndex) {
            d += calculateDistance(path[i], path[i + 1])
        }
        return d
    }

    fun addLocation(location: Location) {
        if (!isRunning) return

        val node = latLonAltToECEF(location)
        val last = lastNode

        if (last == null) {
            path.add(node)
            lastNode = node
            return
        }

        val deltaVector = doubleArrayOf(node.x - last.x, node.y - last.y, node.z - last.z)
        val deltaDistance = sqrt(deltaVector.sumOf { it * it })

        var addNode = false

        // Minimum distance threshold
        if (deltaDistance < minDistance) return

        // Angle-based detection
        previousSegmentVector?.let { prevVector ->
            val dot = deltaVector.zip(prevVector).sumOf { it.first * it.second }
            val magProduct = sqrt(deltaVector.sumOf { it * it }) *
                    sqrt(prevVector.sumOf { it * it })
            val angle = acos((dot / magProduct).coerceIn(-1.0, 1.0)) * (180 / Math.PI)
            if (angle > angleThreshold) addNode = true
        }

        // Stop detection with time threshold
        val currentTime = System.currentTimeMillis()
        if (lastLinearAccelMagnitude < epsilon) {
            if (lastStopTime == null) {
                lastStopTime = currentTime
            } else if (currentTime - lastStopTime!! >= stopTimeThreshold) {
                addNode = true
                lastStopTime = null // reset after registering stop
            }
        } else {
            lastStopTime = null
        }

        if (addNode) {
            path.add(node)

            if (isAutoClosing && path.size > 1 && calculateDistance(path.first(), node) < autoCloseDistance)
                stopRecording()

            previousSegmentVector = deltaVector
            lastNode = node
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            val (x, y, z) = event.values
            lastLinearAccelMagnitude = sqrt(x * x + y * y + z * z)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun latLonAltToECEF(location: Location): PathNode {
        val lat = Math.toRadians(location.latitude)
        val lon = Math.toRadians(location.longitude)
        val h = location.altitude

        val a = 6378137.0
        val f = 1.0 / 298.257223563
        val e2 = 2 * f - f * f

        val sinLat = sin(lat)
        val cosLat = cos(lat)

        val N = a / sqrt(1 - e2 * sinLat * sinLat)

        val x = (N + h) * cosLat * cos(lon)
        val y = (N + h) * cosLat * sin(lon)
        val z = (N * (1 - e2) + h) * sinLat

        return PathNode(x, y, z)
    }
}