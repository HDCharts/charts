package io.github.hdcharts.sampleapi.earthquake.emsc

import io.github.hdcharts.sampleapi.earthquake.Earthquake
import io.github.hdcharts.sampleapi.earthquake.continentOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
internal data class EmscFeatureCollectionDto(
    val features: List<EmscFeatureDto>,
)

@Serializable
internal data class EmscFeatureDto(
    val properties: EmscPropertiesDto,
)

@Serializable
internal data class EmscPropertiesDto(
    val unid: String,
    val time: String,
    val lat: Double,
    val lon: Double,
    val mag: Double? = null,
    @SerialName("flynn_region") val region: String? = null,
)

/** One WebSocket message; [action] is `create` for a new event and `update` for a revision. */
@Serializable
internal data class EmscLiveMessageDto(
    val action: String,
    val data: EmscFeatureDto,
)

/** Null when the event has no magnitude yet or an unreadable time. */
internal fun EmscPropertiesDto.toEarthquake(): Earthquake? {
    val magnitude = mag ?: return null
    val quakeTime = Instant.parseOrNull(time) ?: return null
    return Earthquake(
        id = unid,
        time = quakeTime,
        magnitude = magnitude,
        region = region.orEmpty(),
        continent = continentOf(latitude = lat, longitude = lon),
        latitude = lat,
        longitude = lon,
    )
}
