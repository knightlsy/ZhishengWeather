![Zhisheng Weather · ZHISHENG WEATHER TERMINAL](assets/banner.png)

<p align="center">
  <b>Weather belongs on the first screen.</b><br/>
  A phosphor-terminal weather app for Android. No ads, no account, ready the moment it installs.<br/>
  <sub>ZHISHENG CORE · SENSOR-1 · FORECAST-2 · DISPLAY-3</sub>
</p>

<p align="center">
  <a href="https://github.com/ZhishengZZ/ZhishengWeather/releases/latest">
    <img alt="Download the public APK" src="https://img.shields.io/badge/DOWNLOAD_PUBLIC_APK_·_v0.0.3_·_12_MB-FF6F1E?style=for-the-badge&labelColor=10151C"/>
  </a>
</p>

<p align="center">
  <a href="https://github.com/ZhishengZZ/ZhishengWeather/actions/workflows/build.yml"><img alt="Build" src="https://github.com/ZhishengZZ/ZhishengWeather/actions/workflows/build.yml/badge.svg?branch=main&style=flat-square"/></a>
  <img alt="Android 8.0+" src="https://img.shields.io/badge/Android-8.0%2B-31C9DB?style=flat-square"/>
  <img alt="Current version 0.0.3" src="https://img.shields.io/badge/current-0.0.3-31C9DB?style=flat-square"/>
  <img alt="No ads, accounts, or trackers" src="https://img.shields.io/badge/ads_·_accounts_·_trackers-zero-31C9DB?style=flat-square"/>
  <img alt="MIT License" src="https://img.shields.io/badge/license-MIT-31C9DB?style=flat-square"/>
</p>

<p align="center">
  <a href="README.md">简体中文</a> · <b>English</b>
</p>

---

## A quick look

<table>
  <tr>
    <td align="center"><a href="assets/screenshot-home.jpg"><img src="assets/screenshot-home.jpg" width="160" alt="Home: current conditions, alert, hourly, precipitation"/></a></td>
    <td align="center"><a href="assets/screenshot-details.jpg"><img src="assets/screenshot-details.jpg" width="160" alt="Telemetry and air quality"/></a></td>
    <td align="center"><a href="assets/screenshot-cities.jpg"><img src="assets/screenshot-cities.jpg" width="160" alt="Saved cities"/></a></td>
    <td align="center"><a href="assets/screenshot-add-city.jpg"><img src="assets/screenshot-add-city.jpg" width="160" alt="City search"/></a></td>
    <td align="center"><a href="assets/screenshot-settings.jpg"><img src="assets/screenshot-settings.jpg" width="160" alt="Settings: feeds, location, units"/></a></td>
  </tr>
  <tr>
    <td align="center"><sub>Home</sub></td>
    <td align="center"><sub>Telemetry &amp; air</sub></td>
    <td align="center"><sub>Cities</sub></td>
    <td align="center"><sub>Search</sub></td>
    <td align="center"><sub>Settings</sub></td>
  </tr>
</table>

<p align="center"><sub>Click a screenshot for the full image. Addresses, battery level, and other personal details have been removed.</sub></p>

## Why I built it

Before leaving home, what I want to know is pretty specific: how cold it feels, whether rain is coming, and what the air is like.

Weather apps today want me to sit through a five-second splash ad and dismiss a membership popup first, with the weather itself buried on the third screen. I didn't want to register an account just to check the sky, so I wrote my own. Open Zhisheng Weather and current conditions, alerts, the next 24 hours, and the daily outlook are all on the first screen; humidity, pressure, air quality, and life indices are one scroll down.

The look is personal too: black background, thin rules, a single phosphor accent, and as much information per screen as it can hold. It won't be for everyone. If you like terminal interfaces, you'll recognize it immediately.

## What it answers

- **Take an umbrella?** The next two hours of precipitation are drawn as a bar chart — when the rain starts and how long it lasts, at a glance. QWeather goes down to the minute; the public Open-Meteo feed uses 15-minute steps.
- **What to wear?** Temperature, feels-like, wind, and the dressing index sit together, so you're not guessing from a single number.
- **When does it cool down?** A 24-hour temperature curve and a 15-day high/low outlook both stay on the home screen.
- **Open the windows?** Beyond AQI, you can expand PM2.5, PM10, O₃, NO₂, SO₂, and CO readings.
- **Severe weather coming?** Alerts are colored by level with full text. Fields the provider doesn't supply stay empty — the app doesn't pad them with estimates.

Further down the list: saved cities, sunrise and sunset, moon phase with moonrise and moonset, yesterday's weather for comparison, auxiliary typhoon data, and home-screen widgets in 2x2, 4x2, and 4x4. Long-press the launcher icon to refresh, search for a city, or open settings. Rain gets falling data-rain, snow gets drifting specks, fog gets a breathing noise layer, and thunderstorms get scanlines. All four effects stay behind the content. There are three intensity levels; 0.0.3 makes Vivid noticeably stronger while leaving Subtle alone.

Temperature, wind-speed, and pressure units are independently configurable, and any section you don't use can be hidden.

## Public and full builds

Releases carry the public build. It is not a trial: current conditions, the 24-hour forecast, the 15-day outlook, air quality, short-term precipitation, city search, and widgets all work out of the box.

| | Public build | Full build |
|:--|:--|:--|
| How to get it | Download from [Releases](https://github.com/ZhishengZZ/ZhishengWeather/releases/latest) | Clone the source and build it |
| Data path | Open-Meteo + Xiaomi Weather | QWeather primary, Xiaomi and Open-Meteo supplementing |
| Setup | None | Your own QWeather Ed25519 credentials |
| Difference | Ready to use; QWeather-only fields stay empty | Adds official alerts, minute-level precipitation, and life indices, subject to your account's API access |

If you just want to use the app, grab the public build. The full build can't be shipped preconfigured — developer credentials must not travel inside a public APK.

## Where the data comes from

The worst thing a weather app can do is show a blank screen when a feed dies, so three providers are wired in. Any one of them can drop without taking the UI down:

| Provider | Setup | Main coverage |
|:--|:--|:--|
| QWeather | Your own developer credentials | Current conditions, alerts, hourly/daily, minute precipitation, AQI, life indices |
| Xiaomi Weather | None | Domestic weather, city search, yesterday's weather, typhoons, and other auxiliary fields |
| Open-Meteo | None | Global current/hourly/daily, AQI, 15-minute precipitation, and gap-filling |

Settings let you pin Auto, QWeather, Xiaomi, or Open-Meteo manually, and show which provider actually served the selected city. Auto mode degrades by availability: if the primary feed returns fewer than two hourly entries, the timeline is rebuilt in the city's local timezone and topped up to 24 hours from Open-Meteo; daily forecasts are similarly extended to 15 days, so overseas cities fill out too. Coverage differs between providers, and whatever a provider can't supply is left visibly empty rather than faked into a complete-looking result.

Moon phase, moonrise, and moonset are calculated on-device when a provider does not supply them. This adds no extra network request.

QWeather uses Ed25519-signed JWTs, and public builds forcibly clear those credentials — no developer key can end up in the APK, and the repository carries none.

## The icons are custom too

<p align="center"><img src="assets/icons_grid.png" width="560" alt="Phosphor-terminal weather icon set"/></p>

These aren't assembled from a stock library: 15 weather glyphs made specifically for this project — pure black background, single-cyan duotone, day and night variants, sharing the same contours and brightness relationships as the interface. Before bundling, each one went through a local image pass:

```text
1024² source ─▶ luminance-to-alpha keying ─▶ edge smoothing ─▶ 512 px normalize ─▶ bundle
```

Clear, partly cloudy, overcast, fog, light rain, heavy rain, thunderstorm, snow, wind, and sleet — the full set.

## Install

1. Download [`zhisheng-weather-v0.0.3.apk`](https://github.com/ZhishengZZ/ZhishengWeather/releases/download/v0.0.3/zhisheng-weather-v0.0.3.apk) (about 12 MB).
2. Install it on Android 8.0 or later.
3. First launch shows Beijing by default; search and save your own cities from there.

The APK ships only through GitHub, so Android will ask you to allow installs from unknown sources — that's a warning about the install channel, not an extra permission the app wants. If that bothers you, build it yourself with the steps above; the output is the same.

## What changed in 0.0.3

0.0.3 does not add another weather card to the home screen. It fixes the problems that surfaced while using 0.0.2: moon data, launcher widgets and shortcuts, location changes, provider status, and the Vivid ambience level.

Before release, the project passed 15 unit tests, Android Lint, Debug and public Release builds, plus an upgrade and shortcut check on a Xiaomi phone running Android 16. The detailed record is in [STABILITY_0.0.3.md](STABILITY_0.0.3.md); the APK is on the [v0.0.3 Release](https://github.com/ZhishengZZ/ZhishengWeather/releases/tag/v0.0.3) page.

## Build from source

You need JDK 17 and Android SDK 34. The Gradle Wrapper is included.

```bash
git clone https://github.com/ZhishengZZ/ZhishengWeather.git
cd ZhishengWeather
```

Build without any credentials and you get the public data path. For the QWeather primary feed, put your SDK path and credentials in the root `local.properties` (already git-ignored):

```properties
sdk.dir=<Android SDK path>
qw.host=<API host>
qw.project_id=<Project ID>
qw.kid=<Key ID>
qw.private_key=<single-line Ed25519 private key>
```

Then:

```bash
./gradlew assembleDebug                     # on Windows: .\gradlew.bat assembleDebug
./gradlew assembleRelease                   # full release; bring your own keystore/zhisheng.jks
./gradlew assembleRelease -PpublicBuild     # public build; credentials forcibly cleared, signed with the in-repo public key
```

The in-repo public key only keeps public builds upgrade-compatible with each other. It is not a private or trusted identity credential.

Stack: Kotlin 2.0.21 + Jetpack Compose + Material 3, MVVM (ViewModel / StateFlow), Retrofit + OkHttp + kotlinx-serialization, DataStore, BouncyCastle for Ed25519. `minSdk 26`, `targetSdk 34`. See [CONTRIBUTING.md](CONTRIBUTING.md) for the code layout and commit conventions.

## Permissions and data

The whole app declares three permissions:

| Permission | Purpose |
|:--|:--|
| Internet | Fetch weather data and city-search results |
| Network state | Check whether a connection is available |
| Approximate location | Optional; requested only after you enable location and tap “Locate again” |

There is no ad SDK, no analytics, no account system, and no project-operated backend. Saved cities and settings stay in local storage. Weather requests send the selected city's coordinates to the active provider; when you use location, the coordinates are also used to resolve a city name. Once location is enabled and permission has been granted, the app rechecks the city when it returns to the foreground. It does not track location in the background. The relevant code lives under [`app/src/main/kotlin/com/zhisheng/weather/data`](app/src/main/kotlin/com/zhisheng/weather/data) — go read it.

## Known limitations

Laid out here so you don't discover them after installing:

- The public build carries no QWeather credentials, so official alerts and life indices may be absent there
- Open-Meteo's short-term precipitation comes in 15-minute steps, not a minute-by-minute radar nowcast
- Typhoon and yesterday's weather depend on an auxiliary feed; when it returns nothing, those sections stay empty
- Cross-provider alerts are deduplicated by exact title, so differently worded copies of the same alert can both appear
- This is still an early project. For safety decisions, follow your local meteorological authority

## Changelog

<details open>
<summary><b>0.0.3 // STABILITY PASS</b> — no new weather sections; make the current app dependable</summary>

- Moon phase now follows the selected city's date; missing moonrise and moonset times are calculated on-device and displayed
- Fixed widget layouts that some launchers could not inflate, with regression checks for all three sizes
- Added launcher shortcuts for refresh, city search, and settings
- Increased particle density, movement, and thunderstorm scan frequency in Vivid; Subtle is unchanged
- Provider settings now show the source actually serving the selected city, including connecting, active, available, and not-configured states
- Location requests a fresh position first and rechecks the city on foreground return, without background tracking
- Replaced borrowed terminal names with Zhisheng Weather's own wording
- Current gate: 15 unit tests pass, Lint reports zero errors, and both public and full builds compile

</details>

<details>
<summary><b>0.0.2 // FEED SELECT</b> — selectable feeds, home-screen widgets, ambience effects, opt-in location</summary>

- Four feed choices: Auto / QWeather / Xiaomi / Open-Meteo; Open-Meteo is now a standalone primary feed running the full pipeline key-free
- Home-screen widgets in 2x2, 4x2, and 4x4, in the same terminal dress as the app
- Rain, snow, fog, and thunderstorm ambience effects, three intensities, fully switchable
- Location is now opt-in and off by default; coarse location only, no Google Play Services involved
- Settings rebuilt: feeds / location / units / visible sections / effects / about
- A batch of visual fixes: sun icon shown at night, moon phase stuck on waning crescent (the lunation index was benchmarked 30 years off), inverted daily highs and lows, the hourly curve rendered as broken half-arcs, the back button quitting the app, rotation losing the current page, and misaligned expanded alert cards

**0.0.1 Preview // FIRST DROP** — first public preview: the phosphor-terminal UI, 15 custom icons, three-feed fusion, and the `-PpublicBuild` public build pipeline.

Full notes on the [Releases](https://github.com/ZhishengZZ/ZhishengWeather/releases) page.

</details>

## License

- Code is under the [MIT License](LICENSE). Issues and PRs are welcome ([contributing guide](CONTRIBUTING.md) · [code of conduct](CODE_OF_CONDUCT.md) · [security notes](SECURITY.md)).
- The interface, icon set, and terminal copy are original to Zhisheng Weather. Keep attribution when reusing project artwork.
- Weather data remains the property of [QWeather](https://www.qweather.com/), [Open-Meteo](https://open-meteo.com/), and Xiaomi Weather, and is provided for reference only.
- Bring your own developer credentials for the QWeather feed — and please don't commit your keys to a public repository either.

---

<p align="center"><sub>ZHISHENG WEATHER TERMINAL // PATTERN BLUE · built with phosphor and Kotlin</sub></p>
