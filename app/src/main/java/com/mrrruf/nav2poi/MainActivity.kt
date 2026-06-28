package com.mrrruf.nav2poi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.models.LaunchPinDrop
import io.hammerhead.karooext.models.OnGlobalPOIs
import io.hammerhead.karooext.models.Symbol
import io.hammerhead.karooext.models.OnLocationChanged
import kotlin.math.cos
import kotlin.math.pow
import androidx.compose.ui.graphics.Color
import kotlin.math.*

class MainActivity : ComponentActivity() {

    private  val karooSystem by lazy { KarooSystemService(this) }

    fun calculateDistance(currentLat: Double, currentLng: Double, lat: Double, lng: Double): Double {
        val dlat = currentLat - lat
        val dlng = currentLng - lng
        val a = sin(dlat/2.0).pow(2.0) + sin(dlng/2.0).pow(2.0) * cos(currentLat) * cos(lat)
        return 6378.388 * 2.0 * atan2(sqrt(a), sqrt(1.0-a))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        karooSystem.connect()
        var locationConsumerId = ""
        locationConsumerId = karooSystem.addConsumer<OnLocationChanged> { event ->
            val currentLat = event.lat
            val currentLng = event.lng
            karooSystem.removeConsumer(locationConsumerId)
            println("current position is $currentLat $currentLng")

            var globalPOIConsumerId = ""
            globalPOIConsumerId = karooSystem.addConsumer<OnGlobalPOIs> { event ->
                val pois = event.pois.sortedBy { poi ->
                    calculateDistance(currentLat, currentLng, poi.lat, poi.lng)
                }
                karooSystem.removeConsumer(globalPOIConsumerId)
                /*pois.forEach { poi ->
                    val dst = calculateDistance(currentLat, currentLng, poi.lat, poi.lng)
                    println("POI: ${poi.name}, Lat: ${poi.lat}, Lng: ${poi.lng}, Dst: $dst")
                }*/
                runOnUiThread {
                    enableEdgeToEdge()
                    setContent {
                            POIListSelection(
                                pois,
                                karooSystem
                            )
                    }
                }
            }
        }
    }
    override fun onStop() {
        karooSystem.disconnect()
        println("disconnected")
        super.onStop()
    }

}

@Composable
fun POIListSelection(items: List<Symbol.POI>, karooSystem:KarooSystemService) {


    Column {

        Column (
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.DarkGray)
        ){
            Text(
                text = "Select POI to navigate to",
                color = Color.LightGray,
                modifier = Modifier
                    .padding(16.dp, top=32.dp, bottom=16.dp)
            )
        }
        Spacer(modifier=Modifier.height(8.dp))

        Column (
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ){
            items.forEach {
                Text(
                    text = it.name.toString(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = false,
                            onClick = {
                                karooSystem.dispatch(LaunchPinDrop(it))
                            }
                        )
                        .padding(16.dp)
                )
            }
        }
    }
}