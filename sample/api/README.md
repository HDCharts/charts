# sample-api

Clients for the public APIs behind the demo's "Real data from public APIs" section.

## Purpose

Real network data, kept apart from the generated data in `sample-shared`:

- `EarthquakeRepository`: observes history and live reports as consistent earthquake snapshots. It refreshes after connecting, retries failed refreshes, and merges revisions by event id.
- `SeismogramRepository`: live station recordings from EarthScope SeedLink, one secure WebSocket per station, decoded from miniSEED.

## Layout

- `http`: the shared Ktor client, JSON settings, and the helper that turns any network failure into `ApiUnavailableException`.
- `earthquake`: domain models, history queries, feed state, and the repository interface.
- `earthquake/emsc`: EMSC DTOs, the remote data source, and the repository implementation.
- `seismogram`: domain models, the repository interface, reconnects, and station availability.
- `seismogram/fdsn`: FDSN station sensitivity metadata and the miniSEED (Steim1/Steim2) decoder.
- `seismogram/seedlink`: the SeedLink v3 WebSocket transport and station subscriptions.

Each station streams on its own [EarthScope SeedLink](https://docs.earthscope.org/service/seedlink) connection, so a slow or dropped station never holds back the others. Each stream resumes by recorded time after a disconnect; station history and gaps depend on the server buffer. FDSN is used only for station metadata.

## Constraints

- No dependency on the charts, Compose, `sample-shared`, or `sample/app`.
- Hosts use the repository interfaces and domain models; DTOs and the HTTP client stay internal.
- Every sample app platform includes a WebSocket-capable HTTP engine: OkHttp on Android and desktop JVM, Darwin on iOS, and JS on the web.

## Usage

The host app includes
[`sampleApiModule`](src/commonMain/kotlin/io/github/hdcharts/sampleapi/SampleApiModule.kt)
in its Koin module. The module creates one HTTP client on first use and closes it
when Koin stops. Transport calls and decoding use `Dispatchers.Default`;
the browser runs these on its event loop.
