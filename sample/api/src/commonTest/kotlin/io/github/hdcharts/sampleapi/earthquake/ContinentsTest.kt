package io.github.hdcharts.sampleapi.earthquake

import kotlin.test.Test
import kotlin.test.assertEquals

class ContinentsTest {
    @Test
    fun continentOf_groupsSeismicPlacesUnderTheExpectedContinent() {
        val places =
            listOf(
                Place("Tokyo", 35.7, 139.7, Continent.Asia),
                Place("Kathmandu", 27.7, 85.3, Continent.Asia),
                Place("Jakarta", -6.2, 106.8, Continent.Asia),
                Place("Tehran", 35.7, 51.4, Continent.Asia),
                Place("Erzurum", 39.9, 41.3, Continent.Asia),
                Place("Port Moresby", -9.4, 147.2, Continent.Oceania),
                Place("Wellington", -41.3, 174.8, Continent.Oceania),
                Place("Tonga", -21.1, -175.2, Continent.Oceania),
                Place("Tahiti", -17.6, -149.4, Continent.Oceania),
                Place("Guam", 13.4, 144.8, Continent.Oceania),
                Place("Los Angeles", 34.0, -118.2, Continent.NorthAmerica),
                Place("Anchorage", 61.2, -149.9, Continent.NorthAmerica),
                Place("Adak, Aleutians", 51.9, -176.6, Continent.NorthAmerica),
                Place("Rat Islands, Aleutians", 51.8, 178.0, Continent.NorthAmerica),
                Place("Hawaii", 19.3, -155.4, Continent.NorthAmerica),
                Place("San Jose, Costa Rica", 9.9, -84.1, Continent.NorthAmerica),
                Place("Santiago", -33.4, -70.6, Continent.SouthAmerica),
                Place("Bogota", 4.7, -74.1, Continent.SouthAmerica),
                Place("Athens", 38.0, 23.7, Continent.Europe),
                Place("Izmir", 38.4, 27.1, Continent.Europe),
                Place("Reykjavik", 64.1, -21.9, Continent.Europe),
                Place("Azores", 37.7, -25.7, Continent.Europe),
                Place("Almeria", 36.8, -2.5, Continent.Europe),
                Place("Algiers", 36.8, 3.1, Continent.Africa),
                Place("Nairobi", -1.3, 36.8, Continent.Africa),
                Place("Madagascar", -18.9, 47.5, Continent.Africa),
            )

        places.forEach { place ->
            assertEquals(
                expected = place.continent,
                actual = continentOf(latitude = place.latitude, longitude = place.longitude),
                message = place.name,
            )
        }
    }

    private data class Place(
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val continent: Continent,
    )
}
