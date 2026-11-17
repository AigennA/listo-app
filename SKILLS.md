# Skills and techniques in Listo

Listo is a native Android shopping-list app, built and prepared for Google Play from
scratch. This page maps what the project covers to the code that shows it, so each
point can be checked in a minute.

| Area | Used in Listo |
|---|---|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose, Material 3 (Compose BOM 2024.06) |
| Architecture | MVVM with data / domain / UI layers, use cases, repositories |
| Dependency injection | Hilt 2.51 (activities, ViewModels and broadcast receivers) |
| Persistence | Room 2.6 with migrations, DataStore Preferences |
| Async | Coroutines and Flow (`StateFlow` UI state) |
| Testing | JUnit: 82 JVM unit tests in 20 classes, 9 instrumented tests |
| Build | Gradle (Kotlin DSL, version catalog), KSP, R8 minify and resource shrinking |
| Platform | Notifications, alarms, boot receiver, share sheet, adaptive icons |
| Monetisation & privacy | AdMob banner behind Google UMP consent |

---

## 1. Compose UI

- **Navigation shell with a bottom bar** shown only on top-level screens; tabs keep their
  state and cross-fade, detail screens slide in.
  [LisToNavigation.kt](app/src/main/java/com/listo/app/ui/navigation/LisToNavigation.kt),
  [BottomTab.kt](app/src/main/java/com/listo/app/ui/navigation/BottomTab.kt)
- **Gestures and sheets:** swipe a list card left to open its actions (the card springs
  back instead of being dismissed), with a modal bottom sheet for rename / share / copy /
  delete. [ListsScreen.kt](app/src/main/java/com/listo/app/ui/screens/lists/ListsScreen.kt)
- **Custom vector icons:** the Lists tab layers two vectors so the card follows the tab
  tint while the sprout keeps its own colour.
  [BottomNavBar.kt](app/src/main/java/com/listo/app/ui/navigation/BottomNavBar.kt)
- **Reusable components** shared between screens, e.g. the rename and delete dialogs.
  [ListDialogs.kt](app/src/main/java/com/listo/app/ui/components/ListDialogs.kt)

## 2. Architecture

- **Layers:** Room entities and DAOs → repositories → use cases → ViewModels → screens.
  Screens never touch the database directly.
  [domain/usecases](app/src/main/java/com/listo/app/domain/usecases)
- **Pure, testable UI state:** grouping items by category, the Bought section, progress
  and totals live in a plain data class, not in Composables.
  [ListDetailViewModel.kt](app/src/main/java/com/listo/app/ui/viewmodel/ListDetailViewModel.kt)
- **Small, single-purpose use cases**, e.g. copying a list with every item unticked.
  [DuplicateShoppingListUseCase.kt](app/src/main/java/com/listo/app/domain/usecases/DuplicateShoppingListUseCase.kt)
- **Debug-only code kept out of release:** a Hilt broadcast receiver in the `debug`
  source set seeds demo data for reproducible screenshots.
  [DemoDataReceiver.kt](app/src/debug/java/com/listo/app/debug/DemoDataReceiver.kt)

## 3. Persistence

- **Room** with explicit schema migrations (2 → 3 → 4).
  [DatabaseModule.kt](app/src/main/java/com/listo/app/di/DatabaseModule.kt)
- **DataStore** for theme, accent and onboarding state; synchronous SharedPreferences
  only where a value is needed before the Activity is attached (the in-app language).
  [PreferencesManager.kt](app/src/main/java/com/listo/app/utils/PreferencesManager.kt),
  [LocaleHelper.kt](app/src/main/java/com/listo/app/utils/LocaleHelper.kt)
- **Data compatibility:** removed accent colours map to the closest remaining one, so a
  saved preference never silently resets.
  [AccentColor.kt](app/src/main/java/com/listo/app/data/models/AccentColor.kt)

## 4. Testing

- **Test-first fixes:** bugs are reproduced by a failing test before they are fixed, e.g.
  dates that kept the language of first use.
  [DateUtilsLocaleTest.kt](app/src/test/java/com/listo/app/utils/DateUtilsLocaleTest.kt)
- **Logic kept free of Android types** so it runs on the JVM: catalog matching, price
  formatting, share text, colour contrast, tab rules.
  [app/src/test](app/src/test/java/com/listo/app)
- **Tests that guard resources:** one test parses the `strings.xml` files to make sure
  every translation lists the same items in the same order.
  [CatalogResourcesTest.kt](app/src/test/java/com/listo/app/utils/CatalogResourcesTest.kt)
- **Instrumented tests** for the Room DAO and repository behaviour.
  [app/src/androidTest](app/src/androidTest)

## 5. Internationalisation

- **Three languages** (English, Svenska, Türkçe) switchable inside the app at runtime.
- **Built-in data translated without a migration:** default categories are stored with
  stable English keys and translated only for display; user-made categories are left as
  typed. [CategoryLabels.kt](app/src/main/java/com/listo/app/utils/CategoryLabels.kt)
- **Localised suggestions:** the item catalog comes from per-language string arrays,
  paired with English keys so icons and auto-categories still work in every language.
  [ItemCatalog.kt](app/src/main/java/com/listo/app/utils/ItemCatalog.kt)
- **Currency from the device region, not the UI language:** an English UI on a Swedish
  phone shows kronor. [PriceFormat.kt](app/src/main/java/com/listo/app/utils/PriceFormat.kt)

## 6. Accessibility and theming

- **WCAG AA contrast by construction:** each of the 12 accent colours is adjusted until
  text reaches a 4.5:1 ratio on both the light and the dark background, and tests check
  every combination. [ColorContrast.kt](app/src/main/java/com/listo/app/ui/theme/ColorContrast.kt),
  [AccentTonesTest.kt](app/src/test/java/com/listo/app/ui/theme/AccentTonesTest.kt)
- **Light, dark and system** themes; TalkBack labels and roles on the colour picker.
- **Adaptive launcher icon** with a themed (monochrome) layer and an Android 7 fallback.

## 7. Android platform

- **Reminders:** a daily nudge and per-list reminders with `AlarmManager`, re-scheduled
  after reboot, with the Android 13 notification permission flow.
  [NotificationUtils.kt](app/src/main/java/com/listo/app/utils/NotificationUtils.kt)
- **Intents:** share a list as plain text; feedback opens the user's own mail app.
  [ListShare.kt](app/src/main/java/com/listo/app/utils/ListShare.kt),
  [SupportLinks.kt](app/src/main/java/com/listo/app/utils/SupportLinks.kt)
- **Edge-to-edge** layout with correct inset handling under the bottom bar.

## 8. Privacy and release preparation

- **Offline by design:** no account, no server; lists stay on the device. The privacy
  policy and Play data-safety answers are written to match the code.
  [privacy-policy.html](docs/privacy-policy.html), [data-safety.md](store/data-safety.md)
- **Consent before ads:** the ads SDK starts only after Google UMP consent where the law
  requires it, with a way back to the choice in Settings.
  [ads](app/src/main/java/com/listo/app/ads)
- **Store assets as code:** screenshots are framed and re-encoded to Play's format by a
  script (headless Chrome + System.Drawing), so they can be rebuilt after any UI change.
  [render-play-screenshots.ps1](store/graphics/render-play-screenshots.ps1)
- **Release checklist** tracking every Play requirement and its status.
  [RELEASE-CHECKLIST.md](store/RELEASE-CHECKLIST.md)

## 9. Way of working

- **Git flow:** every change on its own `feature/` or `fix/` branch, merged with
  `--no-ff`; `develop` is protected and `main` is kept equal to it.
- **Commit messages explain why**, not just what — see the history.
- **No hardcoded values or user-facing text in code:** strings, colours and limits live
  in resources or named constants; code, comments and data are in English.
