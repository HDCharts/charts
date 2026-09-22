---
name: hdc-gif
description: Record or update HDCharts docs GIF scenarios locally against a running emulator.
---

# Record Docs GIF Scenarios

## Prerequisite

An Android emulator must already be running (check with `adb devices`)
before any command below. This skill never starts, stops, or picks an
emulator — that stays a manual step. If `adb devices` is empty, ask the
user to start one first.

## Guardrails

Follow [AGENTS.md](../../AGENTS.md). CI owns `validateDocsGifBaselines`;
never run it locally. Recording is fine to run locally when the user asks.

## Commands

List available scenarios (discovered via `@RecordGif` in `:androidApp`):

```bash
./gradlew listDocsGifScenarios
```

Record one scenario:

```bash
./gradlew recordDocsGif -PgifScenario=<name> --no-daemon
```

Record every scenario:

```bash
./gradlew recordDocsGifs --no-daemon
```

If the scenario source changed, check lint first:

```bash
./gradlew :androidApp:ktlintMainSourceSetCheck --no-daemon
```

## Notes

- Output lands in `gif-baselines/<scenario>.gif` by default (the
  `gifRecorder { outputDir }` default in
  `sample/androidApp/build.gradle.kts`), unless `-PgifOutputDir` or
  `-PgifContentRoot` overrides it.
- If `./gradlew` can't find `adb`/`ffmpeg`/`gifsicle`, set `ANDROID_HOME`
  (e.g. `ANDROID_HOME=$HOME/Library/Android/sdk`) and make sure `ffmpeg`
  and `gifsicle` are on `PATH`.
- Each recording reinstalls the debug app/test APK and drives the real
  device, so it takes roughly a minute or two per scenario.
