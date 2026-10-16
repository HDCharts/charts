package io.github.hdcharts.sampleapi

import io.github.hdcharts.sampleapi.earthquake.EarthquakeRepository
import io.github.hdcharts.sampleapi.earthquake.emsc.EmscApi
import io.github.hdcharts.sampleapi.earthquake.emsc.EmscEarthquakeApi
import io.github.hdcharts.sampleapi.earthquake.emsc.EmscEarthquakeRepository
import io.github.hdcharts.sampleapi.http.sampleHttpClient
import io.github.hdcharts.sampleapi.seismogram.EarthScopeSeismogramRepository
import io.github.hdcharts.sampleapi.seismogram.SeismogramRepository
import io.github.hdcharts.sampleapi.seismogram.fdsn.FdsnStationApi
import io.github.hdcharts.sampleapi.seismogram.seedlink.SeedLinkApi
import org.koin.dsl.module
import org.koin.dsl.onClose

/** Lazy API bindings shared by every sample app platform. */
val sampleApiModule =
    module {
        single { sampleHttpClient() } onClose { it?.close() }
        single<EmscApi> { EmscEarthquakeApi(get()) }
        single { FdsnStationApi(get()) }
        single { SeedLinkApi(get()) }
        single<EarthquakeRepository> { EmscEarthquakeRepository(get()) }
        single<SeismogramRepository> {
            EarthScopeSeismogramRepository(
                sensitivity = get<FdsnStationApi>()::sensitivity,
                listen = get<SeedLinkApi>()::listen,
            )
        }
    }
