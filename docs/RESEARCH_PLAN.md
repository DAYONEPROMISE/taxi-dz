# TaxiDZ — Research, Plan, Limits, Costs, Go-to-market

## RESEARCH (vérifié, 2026-10-10)
- Pattern éprouvé localement : `arsurvey-app` (AGP 8.6.1 + Kotlin 1.9.25 + KSP 1.9.25-1.0.20 + Compose BOM 2024.09.02 + Room 2.6.1) compile et produit un APK via GitHub Actions (`ubuntu-latest`, Temurin 17, `platforms;android-34`, `build-tools;34.0.0`). TaxiDZ réutilise exactement ce rail.
- Versions fixées (pas d'hypothèses) : `androidx.activity:activity-compose:1.9.2`, `lifecycle 2.8.6`, `coroutines 1.8.1`, `junit 4.13.2`, `gradle 8.9`, `compileSdk/targetSdk 34`, `minSdk 26`.
- Localisation : ressources `values-fr` + `values-ar`, `supportsRtl=true`, `CompositionLocalProvider(LocalLayoutDirection)` + `config.setLayoutDirection` — pas de lib tierce.
- 100 % offline : Room local uniquement, zéro permission réseau/GPS en v1 (aucune permission dans le manifeste).

## VALIDATE
- Besoin : chauffeurs de taxi algériens suivent gains au papier ; smartphones Android anciens (minSdk 26 couvre ~99 % du parc actif).
- Risques : saisie manuelle (erreurs) → atténué par validation (montants > 0, plafond 1 000 000 DA) ; pas de GPS v1 → assumé, demandé plus tard.

## SELECT
- Kotlin + Compose Material 3 + Room + Coroutines/Flow. Aucune dépendance exotique. Code 100 % original (clean-room, MIT).

## PLAN (architecture)
```
MainActivity (Compose + tabs Courses/Gains/Carburant)
  └─ TaxiDzViewModel (AndroidViewModel)
       ├─ Room: TaxiDzDatabase [Trip | FuelExpense] via TripDao / FuelDao (Flow)
       └─ combine(trips, fuel) → StatsUi (jour, 7 j, brut/carburant/net, détail/jour)
logic/EarningsCalculator — pur JVM, testé (sommes, net, dailyTotals, weeklyAverage, validate*, filtres)
ui/AppLocale — FR/AR persisté (SharedPreferences), appliqué via AppCompatDelegate + wrapContext
```

## TEST TABLE
| # | Test | Type | Attendu | Résultat |
|---|------|------|---------|----------|
| 1 | `sums fares and fuel and computes net` | unit (JVM) | 800 / 200 / 600 | CI |
| 2 | `empty lists give zero` | unit | 0 partout | CI |
| 3 | `daily totals group by day oldest first` | unit | 400+300 groupés | CI |
| 4 | `weekly average divides 7-day sum by 7` | unit | 100.0 | CI |
| 5 | `validation accepts good input and rejects bad` | unit | 10 cas | CI |
| 6 | `range filters are inclusive` | unit | bornes incluses | CI |
| 7 | `lintDebug` | lint | 0 erreur fatale | CI |
| 8 | `assembleDebug` | build | APK produit | CI |
| 9 | Smoke app manuelle (émulateur/Pixel, FR puis AR) | manuel | 3 onglets OK, ajout/suppression OK | à faire à réception de l'APK |

## LIMITATIONS v1
- Pas de GPS/compteur automatique (saisie manuelle).
- Pas d'export CSV/PDF, pas de sauvegarde cloud, pas de multi-véhicule.
- Devise DA uniquement ; périodes : jour + 7 jours (pas de mois).
- Thème clair Material 3 manuel (pas de dynamic color), pas de mode sombre dédié.

## COSTS
- Dev : 0 (temps agent). Build : GitHub Actions minutes publiques gratuites. Backend : 0 (aucun). Play Store (optionnel) : 25 $ une fois — non requis (distribution APK directe).

## FIRST-10-CUSTOMERS PLAN
1. 3 stations de taxi à Alger (place ciblée) : installer l'APK, saisir 1 journée ensemble.
2. 2 groupes Facebook/WhatsApp de chauffeurs (Alger, Oran) : vidéo 60 s en darija/FR.
3. 2 chauffeurs VTC : tester l'onglet carburant (argument net vs brut).
4. 2 coopératives : proposer saisie du soir + bilan hebdo le vendredi.
5. 1 chauffeur-relais : parrainage (1 semaine gratuite de support WhatsApp).
- Objectif : 10 utilisateurs actifs/jour à J+14, retour GPS/export pour v2.
