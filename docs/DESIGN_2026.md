# TaxiDZ — 2026 UI/UX pass (no Penpot access: tokens implemented directly in Compose M3)

No Penpot MCP/browser tool is available to this builder, so the shared 2026
direction is implemented directly as Material 3 tokens in code. Any Penpot
library published later can map 1:1 to the tokens below.

## Design tokens (M3, mirrored in `MainActivity.kt` + `themes.xml`)
- Color: Material 3 dynamic color (`dynamicLightColorScheme` /
  `dynamicDarkColorScheme` on API 31+, fallback `lightColorScheme` /
  `darkColorScheme`). System dark mode respected via `isSystemInDarkTheme()`.
  No hard-coded backgrounds; splash/activity theme is `DayNight` with
  transparent system bars.
- Shape/type: default M3 shapes + `titleSmall/titleMedium/titleLarge`,
  `headlineSmall`, `bodySmall`. Cards for trip/fuel rows and stat summaries.
- Components: `Scaffold` + `TopAppBar` + `NavigationBar` (3 tabs),
  `OutlinedTextField`, `Button` (full-width primary actions), `Card` list rows,
  `IconButton` delete actions.
- Icons: Material Symbols filled (`DirectionsCar`, `BarChart`,
  `LocalGasStation`, `Delete`) — no emoji icons.
- Dark mode: full — dynamic dark scheme + DayNight base theme.
- FR/AR RTL: `supportsRtl=true`, `LocalLayoutDirection` forced from app
  locale (FR=Ltr, AR=Rtl), `values-fr` + `values-ar` strings, locale switch in
  TopAppBar recreates activity.
- Edge-to-edge: `enableEdgeToEdge()`, transparent status/nav bars, scaffold
  padding + `consumeWindowInsets`.
- Adaptive: single-column phone-first; full-width fields/cards scroll in
  `LazyColumn`, works landscape/tablet width without fixed sizes.
- Accessibility: contentDescription on all delete actions
  (`R.string.delete_item` in FR/AR), numeric keyboards for amounts, visible
  error text in `colorScheme.error`, 48dp+ M3 touch targets by default.

## Screens (Trips / Stats / Fuel — unchanged IA)
1. Trips: 3 fields (Départ, Destination, Tarif DA) + add button + card list
   (route, fare • date, delete).
2. Stats: today card (total • count), 7-day card (brut/carburant/net/total),
   per-day rows.
3. Fuel: amount + litres + note + add + card list with delete.

## Verification
- `./gradlew :app:testDebugUnitTest` (CI), `lintDebug`, `assembleDebug` in
  `.github/workflows/ci.yml`; manual FR/AR + light/dark smoke on device.
