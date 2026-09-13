# Kotlin/JS to Kotlin/Wasm migration

Use Kotlin/Wasm (`wasmJs`) for web applications that consume HDCharts.

## Before

```kotlin
js {
    browser()
}
```

## After

Replace the Kotlin/JS web target with `wasmJs`:

```kotlin
@OptIn(ExperimentalWasmDsl::class)
wasmJs {
    browser()
}
```

JVM, Android, and iOS targets are unchanged.
