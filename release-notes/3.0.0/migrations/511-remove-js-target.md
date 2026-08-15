# Legacy Kotlin/JS target removed

## What changed

The `js` (Kotlin/JS) target was removed from all published modules. Web support is now exclusively via Kotlin/Wasm (`wasmJs`), which is already used by the web demo and playground.

## What you should do

- If you consumed the library from Kotlin/JS, migrate your web target to Kotlin/Wasm (`wasmJs`).
- Consumers on JVM, Android, iOS, and wasmJs are unaffected.
