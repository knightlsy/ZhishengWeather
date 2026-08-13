![Zhisheng Weather · ZHISHENG WEATHER TERMINAL](assets/banner.png)

<p align="center">
  <img src="assets/app-icon.png" width="96" alt="Zhisheng Weather app icon"/><br/>
  <b>Open it and get the weather.</b><br/>
  A dense, phosphor-terminal weather app for Android. No ads, accounts, or analytics SDK.
</p>

<p align="center">
  <a href="https://github.com/ZhishengZZ/ZhishengWeather/releases/latest">
    <img alt="Download the public APK" src="https://img.shields.io/badge/DOWNLOAD_PUBLIC_APK_·_v0.0.4-FF6F1E?style=for-the-badge&labelColor=10151C"/>
  </a>
</p>

<p align="center">
  <a href="https://github.com/ZhishengZZ/ZhishengWeather/actions/workflows/build.yml"><img alt="Build" src="https://github.com/ZhishengZZ/ZhishengWeather/actions/workflows/build.yml/badge.svg?branch=main&style=flat-square"/></a>
  <img alt="Android 8.0+" src="https://img.shields.io/badge/Android-8.0%2B-31C9DB?style=flat-square"/>
  <img alt="Current version 0.0.4" src="https://img.shields.io/badge/current-0.0.4-31C9DB?style=flat-square"/>
  <img alt="No ads, accounts, or tracking" src="https://img.shields.io/badge/ads_·_accounts_·_tracking-none-31C9DB?style=flat-square"/>
  <img alt="MIT License" src="https://img.shields.io/badge/license-MIT-31C9DB?style=flat-square"/>
</p>

<p align="center">
  <a href="README.md">简体中文</a> · <b>English</b>
</p>

---

## Screens

<table>
  <tr>
    <td align="center"><a href="assets/screenshot-home.jpg"><img src="assets/screenshot-home.jpg" width="160" alt="Zhisheng Weather home screen"/></a></td>
    <td align="center"><a href="assets/screenshot-details.jpg"><img src="assets/screenshot-details.jpg" width="160" alt="Telemetry and air quality"/></a></td>
    <td align="center"><a href="assets/screenshot-cities.jpg"><img src="assets/screenshot-cities.jpg" width="160" alt="Saved cities"/></a></td>
    <td align="center"><a href="assets/screenshot-add-city.jpg"><img src="assets/screenshot-add-city.jpg" width="160" alt="City search"/></a></td>
    <td align="center"><a href="assets/screenshot-settings.jpg"><img src="assets/screenshot-settings.jpg" width="160" alt="Settings"/></a></td>
  </tr>
  <tr>
    <td align="center"><sub>Home</sub></td>
    <td align="center"><sub>Telemetry &amp; air</sub></td>
    <td align="center"><sub>Cities</sub></td>
    <td align="center"><sub>Search</sub></td>
    <td align="center"><sub>Settings</sub></td>
  </tr>
</table>

<p align="center"><sub>Click a screenshot to open the full image.</sub></p>

## About the project

When I check the weather, I usually want four answers quickly: the current temperature, when rain is due, whether the air is decent, and whether the next few days will turn colder. Zhisheng Weather puts those answers in one vertical feed. There is no splash ad and no account screen.

The interface uses a phosphor-terminal look: black background, thin dividers, cyan for regular data, and orange for signals that need attention. Current conditions, alerts, hourly weather, and short-term precipitation come first. Air quality, life indices, and moon data follow below.

## What it shows

- Current conditions, feels-like temperature, wind, and pressure for your location or saved cities
- A 24-hour forecast, 15-day high/low outlook, and the next two hours of precipitation
- Weather alerts, six air-pollutant readings, and common life indices
- Sunrise, sunset, moon phase, moonrise, moonset, and yesterday's weather
- Saved cities and home-screen widgets in 2x2, 4x2, and 4x4 sizes
- Launcher shortcuts for refresh, city search, and settings

Rain, snow, fog, and thunderstorms each have an optional background effect. Intensity is adjustable, and every effect can be turned off. Temperature, wind-speed, and pressure units are configured separately. Page sections can also be hidden.

Fields that a provider does not supply stay empty; the app does not fill them with estimates.

## Public and full builds

GitHub Releases provide the public APK, which works immediately after installation. The full build must be compiled from source with your own QWeather developer credentials.

| | Public build | Full build |
|:--|:--|:--|
| Get it | Download from [Releases](https://github.com/ZhishengZZ/ZhishengWeather/releases/latest) | Clone and build the source |
| Default data path | Xiaomi Weather + Open-Meteo | QWeather first, with Xiaomi and Open-Meteo as supplements |
| Extra setup | None | QWeather Ed25519 credentials |
| Main difference | Everyday weather works out of the box | QWeather alerts, minute precipitation, and life indices are available according to account permissions |

The public build is the right choice for normal use. The full build cannot be distributed preconfigured because developer credentials do not belong in a public APK.

## Data providers

The app connects to QWeather, Xiaomi Weather, and Open-Meteo. Settings can select a provider manually or use Auto, and show which provider actually returned the selected city's data.

| Provider | Setup | Main coverage |
|:--|:--|:--|
| QWeather | Personal developer credentials | Current conditions, alerts, hourly/daily, minute precipitation, AQI, life indices |
| Xiaomi Weather | None | Weather in China, city search, yesterday's weather, and typhoon support data |
| Open-Meteo | None | Global current/hourly/daily, AQI, 15-minute precipitation, and gap filling |

Auto tries QWeather in a full build, then Xiaomi Weather and Open-Meteo. If the primary feed returns too few hourly entries, the app rebuilds the timeline in the city's local time and fills it to 24 hours with Open-Meteo. Daily forecasts use the same approach to reach 15 days.

Moon phase is calculated on-device for the selected city's date. If the provider does not return moonrise or moonset, the app calculates them from the date and coordinates without making another request.

QWeather requests use Ed25519-signed JWTs. The `-PpublicBuild` task clears the QWeather configuration during the build, so the public APK and repository contain no developer credentials.

## Icons

<p align="center">
  <img src="assets/app-icon.png" width="144" alt="Zhisheng Weather app icon"/>
</p>

The launcher icon uses three clear weather elements: sun, cloud, and rain. Its dark base and cyan-orange palette match the app interface.

<p align="center"><img src="assets/icons_grid.png" width="560" alt="Zhisheng Weather icon set"/></p>

The app also includes 15 custom weather glyphs for clear, cloudy, overcast, fog, rain, thunderstorms, snow, wind, and sleet. They were drawn for this project rather than assembled from a general icon library.

## Install

1. Download the latest public APK from [Releases](https://github.com/ZhishengZZ/ZhishengWeather/releases/latest).
2. Install it on Android 8.0 or later.
3. The first launch shows Beijing. Use search to save your own cities.

The APK is distributed through GitHub. Android may ask you to allow the current app to install unknown-source files. That prompt refers to the download channel; Zhisheng Weather is not requesting another system permission.

## Version 0.0.4

Version 0.0.4 responds to test feedback by rebuilding the widgets and incorporating stability fixes, followed by offline caching, background widget refresh, and a polish pass.

- Reworked all three widgets into 22dp rounded weather instruments with substantially larger readings and glyphs
- Added date, feels-like, humidity, wind, rain probability, provider, and update time to widgets, with colors aligned to the app theme
- Widgets now refresh in the background every hour; snapshots older than 3 hours show "x hours ago" and older than 24 hours show "stale"
- Added offline caching: when the network or all providers fail, the last successful data is shown with an "x minutes ago" marker
- Global 25s request deadline and provider circuit breaking keep dead providers from stalling every refresh
- Included all seven stability fixes from the 0.0.3 test branch
- Alerts are color-coded by the four-tier national scale (blue/yellow/orange/red); added rain-band distance, AQI health advice, and hourly AQI (Xiaomi provider)
- City data corruption now self-heals from a backup; location lookup is capped at 15s and rejects low-accuracy fixes

Release checks cover 41 unit tests, Android Lint, and Debug, public Release, and full Release builds. See [RELEASE_0.0.4.md](RELEASE_0.0.4.md) for the full record.

## Build from source

You need JDK 17 and Android SDK 34. The Gradle Wrapper is included.

```bash
git clone https://github.com/ZhishengZZ/ZhishengWeather.git
cd ZhishengWeather
```

Without credentials, the project builds the public data path. To use QWeather as the primary feed, put your SDK path and credentials in the root `local.properties`; the file is already ignored by Git.

```properties
sdk.dir=<Android SDK path>
qw.host=<API host>
qw.project_id=<Project ID>
qw.kid=<Key ID>
qw.private_key=<single-line Ed25519 private key>
```

```bash
./gradlew assembleDebug                     # Windows: .\gradlew.bat assembleDebug
./gradlew assembleRelease                   # full build; configure your own signing key
./gradlew assembleRelease -PpublicBuild     # public build; clears credentials and uses the in-repo public key
```

The bundled `keystore/public.jks` only keeps public builds upgrade-compatible with each other. It is not a private signing identity.

Main stack: Kotlin 2.0.21, Jetpack Compose, Material 3, ViewModel / StateFlow, Retrofit, OkHttp, kotlinx-serialization, DataStore, and BouncyCastle. `minSdk 26`, `targetSdk 34`. See [CONTRIBUTING.md](CONTRIBUTING.md) for code layout and commit conventions.

## Permissions and data

The app declares three permissions:

| Permission | Purpose |
|:--|:--|
| Internet | Fetch weather and city-search data |
| Network state | Check whether the device is online |
| Approximate location | Optional; requested only after location is enabled and a new fix is requested |

There is no ad SDK, analytics, account system, or project-operated backend. Saved cities and settings stay on the device. Weather requests send the selected city's coordinates to the active provider. Location coordinates are also used to resolve a city name.

After location is enabled and permission is granted, the app rechecks the city at intervals when returning to the foreground. It does not collect location in the background. Relevant code is under [`app/src/main/kotlin/com/zhisheng/weather/data`](app/src/main/kotlin/com/zhisheng/weather/data).

## Known limitations

- The public build has no QWeather credentials, so official alerts and life indices may be unavailable
- Open-Meteo short-term precipitation uses 15-minute intervals rather than minute-by-minute radar nowcasting
- Typhoon and yesterday's weather depend on an auxiliary feed; those sections stay empty when it returns nothing
- Alerts are deduplicated by exact title, so differently worded copies of one alert may both appear
- This is an early release. For safety decisions, follow your local meteorological authority

## Changelog

<details open>
<summary><b>0.0.4 // WIDGET OVERHAUL</b></summary>

- Rebuilt all three widgets with rounded panels, larger type and glyphs, and a clearer information hierarchy
- Included the 0.0.3 test stability patches; 41 unit tests pass and Lint reports zero errors
- Offline cache fallback, background widget refresh, provider circuit breaking, color-coded alerts

</details>

<details>
<summary><b>0.0.3 // STABILITY PASS</b></summary>

- Fixed moon data, widgets, launcher shortcuts, city relocation, and provider status
- Adjusted the Vivid ambience level without changing Subtle
- Standardized the terminal name and replaced the launcher icon
- 15 unit tests pass, Lint reports zero errors, and public and full builds compile

</details>

<details>
<summary><b>0.0.2 // FEED SELECT</b></summary>

- Added provider selection, three widget sizes, weather ambience, and optional location
- Made Open-Meteo available as a standalone provider
- Fixed night icons, the hourly curve, back navigation, and rotation state

</details>

**0.0.1 Preview** was the first public build, with the phosphor-terminal interface, 15 weather glyphs, three-provider data path, and public build task.

See [Releases](https://github.com/ZhishengZZ/ZhishengWeather/releases) for complete version notes.

## License

- Code is released under the [MIT License](LICENSE). Issues and pull requests are welcome.
- The interface, launcher icon, weather glyphs, and terminal copy are project artwork. Keep attribution when reusing them.
- Weather data belongs to [QWeather](https://www.qweather.com/), [Open-Meteo](https://open-meteo.com/), and Xiaomi Weather and is provided for reference only.
- Keep personal QWeather credentials out of public repositories.

---

<p align="center"><sub>ZHISHENG WEATHER TERMINAL // PATTERN BLUE · Kotlin / Android</sub></p>
