# Listo — Google Play release checklist

Status as of 8 September 2026. Ticked items are done and in the repo; unticked items
still need work. Items marked **BLOCKER** must be fixed before a public release.

## 1. Store assets — done

- [x] Phone screenshots (6), English — `store/screenshots/phone-en/`
- [x] App icon 512×512 PNG — `store/graphics/icon-512.png`
- [x] Feature graphic 1024×500 PNG — `store/graphics/feature-graphic-1024x500.png`
- [x] Listing copy in English, Turkish, Swedish — `store/listing/`
- [x] Privacy policy page — `docs/privacy-policy.html`
- [x] Data safety form answers — `store/data-safety.md`
- [ ] Replace `REPLACE_WITH_CONTACT_EMAIL` in the privacy policy with a real address
- [ ] Publish the policy: repo Settings → Pages → source `main` / `/docs`, giving
      `https://aigenna.github.io/listo-app/privacy-policy.html`
- [ ] Optional: Turkish and Swedish screenshot sets (switch the in-app language and
      recapture — the same six screens)

## 2. Known defects — fixed

- [x] **Crash when saving a list reminder.** `ListReminderScheduler.schedule()` used
      `AlarmManager.setAlarmClock()`, which has needed `SCHEDULE_EXACT_ALARM` since
      Android 14 — the app threw `SecurityException` and died. It now schedules with
      `setWindow()` inside `AppDefaults.REMINDER_WINDOW_MS` (10 minutes), and
      `SCHEDULE_EXACT_ALARM` has been removed from the manifest. Verified on an
      emulator: the reminder saves, the alarm is registered with the system and the
      app no longer crashes.
- [x] **Onboarding was shown on every launch.** `SplashScreen` now asks
      `SplashViewModel` where to go (`StartDestination.afterSplash`), and
      `OnboardingScreen` persists `setOnboardingDone(true)` before navigating away.
      Covered by `StartDestinationTest`; verified with a cold restart on an emulator.

## 3. Monetisation and consent

- [ ] Replace Google's **test** AdMob ids with the real ones:
      `AndroidManifest.xml` app id and `AdBanner.kt` banner unit id. Wire them so debug
      builds keep the test ids and only release builds use the real ones.
- [ ] Add Google's User Messaging Platform (UMP) consent flow before
      `MobileAds.initialize`, and a Settings entry so EEA/UK users can change their
      choice. (Draft implementation already written — `AdsInitializer`, `ConsentManager`
      and unit tests are parked outside the repo.)
- [ ] Configure the GDPR consent message in the AdMob console.
- [ ] Wire the Settings → "Privacy Policy" row to open the published policy URL
      (currently an empty `onClick`).

## 4. Build and signing

- [ ] Create an upload keystore and keep it out of the repo
      (`keystore.properties` and `*.jks` are already gitignored):
      ```
      keytool -genkeypair -v -keystore listo-upload.jks -keyalg RSA -keysize 2048 \
              -validity 10000 -alias listo
      ```
      Back it up — losing it means losing the ability to update the app.
- [ ] Add a release `signingConfig` that reads from `keystore.properties`
- [ ] Enrol in Play App Signing (default for new apps)
- [ ] Confirm `versionCode = 1`, `versionName = "1.0.0"` for the first release
- [ ] `./gradlew bundleRelease` → upload `app/build/outputs/bundle/release/app-release.aab`
- [ ] Check the release build with R8 enabled actually runs (minify + resource shrinking
      are already on for release)

## 5. Play Console setup

- [ ] Create the app: name "Listo", default language English, app type **App**, **Free**
- [ ] Store listing per language (copy from `store/listing/`)
- [ ] Upload icon, feature graphic, screenshots
- [ ] Content rating questionnaire
- [ ] Data safety form (`store/data-safety.md`)
- [ ] Ads declaration → contains ads
- [ ] Target audience → 13+, not designed for children
- [ ] Privacy policy URL
- [ ] Countries and regions
- [ ] Internal testing track first; read the pre-launch report before production

## 6. Nice to have before launch

- [ ] The launcher icon (`ic_launcher_foreground.xml`) is a dark navy "L" card that does
      not match the app's green sprout brand used in the splash, onboarding and store
      icon. Aligning them would make the installed app match the store listing.
- [ ] The system date picker renders in stock teal rather than the app's accent colour.
- [ ] Onboarding pages leave a large empty gap between the subtitle and the pager dots.
