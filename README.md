# TaxiDZ — carnet de courses et gains pour chauffeurs de taxi (Algérie)

Offline-first Android app (Kotlin, Jetpack Compose Material 3, Room). 100% hors-ligne, sans compte, sans GPS en v1 (saisie manuelle).

**Fonctions v1 :**
- Journal des courses : départ, destination, tarif (DA)
- Statistiques jour / 7 jours : brut, carburant, net, total général
- Suivi carburant / dépenses (montant DA, litres et note en option)
- Français / العربية avec RTL
- Suppression d'entrées (courses, dépenses)

**Stack (verrouillée) :** AGP 8.6.1 · Kotlin 1.9.25 · KSP 1.9.25-1.0.20 · Compose BOM 2024.09.02 · Room 2.6.1 · minSdk 26 / targetSdk 34.

## Build local
```sh
./gradlew :app:assembleDebug
```

## CI
`.github/workflows/ci.yml` : tests unitaires → lint → assembleDebug → artefact APK → release `debug-latest`.

## Tests
```sh
./gradlew :app:testDebugUnitTest
```
Logique pure testée : `EarningsCalculator` (sommes, net, totaux/jour, moyenne hebdo, validation, filtres).

## Confidentialité
Aucun réseau, aucun compte, aucune localisation. Données dans Room local (`taxidz.db`), sauvegarde Android standard.

Voir `docs/` : recherche, plan, limites, coûts, plan 10 premiers clients.
