package io.github.hdcharts.sampleapi.earthquake

/** Coarse latitude and longitude boxes; good enough to group quakes, not to draw borders. */
internal fun continentOf(
    latitude: Double,
    longitude: Double,
): Continent =
    when {
        // Alaska and the Aleutian arc, which crosses the antimeridian.
        latitude >= 45 && (longitude <= -140 || longitude >= 165) -> Continent.NorthAmerica
        longitude <= -170 -> Continent.Oceania
        longitude <= -30 -> americas(latitude, longitude)
        longitude <= 40 -> europeOrAfrica(latitude, longitude)
        longitude <= 60 -> westAsiaOrNeighbours(latitude)
        else -> asiaOrOceania(latitude, longitude)
    }

private fun americas(
    latitude: Double,
    longitude: Double,
): Continent =
    when {
        latitude >= 13 -> Continent.NorthAmerica
        longitude <= -120 -> Continent.Oceania
        longitude <= -77 && latitude >= 7 -> Continent.NorthAmerica
        else -> Continent.SouthAmerica
    }

private fun europeOrAfrica(
    latitude: Double,
    longitude: Double,
): Continent =
    when {
        // The Algerian and Tunisian coast reaches further north than southern Spain.
        longitude >= -1.5 && longitude < 12 && latitude < 37.2 -> Continent.Africa
        latitude >= 36 -> Continent.Europe
        longitude >= 32 && latitude >= 29 -> Continent.Asia
        else -> Continent.Africa
    }

private fun westAsiaOrNeighbours(latitude: Double): Continent =
    when {
        latitude >= 50 -> Continent.Europe
        latitude < 12 -> Continent.Africa
        else -> Continent.Asia
    }

private fun asiaOrOceania(
    latitude: Double,
    longitude: Double,
): Continent =
    when {
        latitude < -12 -> Continent.Oceania
        longitude >= 134 && latitude < 0 -> Continent.Oceania
        longitude >= 140 && latitude < 24 -> Continent.Oceania
        longitude >= 165 -> Continent.Oceania
        else -> Continent.Asia
    }
