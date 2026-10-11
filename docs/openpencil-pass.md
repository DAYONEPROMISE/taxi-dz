# OpenPencil pass — taxi-dz

## Design (Shared/OpenPencil/taxi-dz/)
- `design.html` (earnings hero + trip form + 3-tab nav, DZ tokens) → `design.fig`
- `lint --preset accessibility`: 0 errors, 0 warnings

## Code changes
- `MainActivity.kt`: 3 bottom-nav icons used `contentDescription = null`; now use the tab
  labels (`tab_trips`/`tab_stats`/`tab_fuel`). Inline dynamic-color theme + FR/AR locale +
  validation errors untouched.

## APKs
`ci.yml` adds `assembleRelease` (unsigned) + `taxidz-release` artifact + a versioned
`release-v*-b*` GitHub Release (debug flow unchanged).
