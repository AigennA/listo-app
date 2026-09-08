# Play Console — Data safety form answers

These answers describe Listo **as it is built today**: all list data is local, and the
only data collection comes from the Google AdMob banner. Re-check this form if an
analytics or crash-reporting SDK is ever added.

## Data collection and sharing

| Question | Answer |
|---|---|
| Does your app collect or share any of the required user data types? | **Yes** — via the AdMob ads SDK |
| Is all of the user data collected by your app encrypted in transit? | **Yes** |
| Do you provide a way for users to request that their data is deleted? | **No** — no account exists and the app itself sends no data anywhere |

## Data types to declare

### Device or other IDs → Device or other IDs
- **Collected:** Yes
- **Shared:** Yes (with Google, for ad serving)
- **Processed ephemerally:** No
- **Required or optional:** Required
- **Purpose:** Advertising or marketing

> This is the advertising ID used by AdMob. It is the only data type the app causes to
> be collected.

### Everything else
Declare **not collected** for: personal info, financial info, health and fitness,
messages, photos and videos, audio, files and docs, calendar, contacts, app activity,
web browsing, app info and performance.

> Shopping lists, items, prices, budgets and settings are stored only in the device's
> local Room database and DataStore. They are never transmitted, so they are **not**
> declared as collected.

## Related Play Console declarations

| Declaration | Answer |
|---|---|
| Ads — does your app contain ads? | **Yes**, banner ads |
| In-app purchases | **No** |
| Target audience | 13+ / general audience — **not** designed for children |
| Content rating questionnaire | Utility / productivity, no violence, no sexual content, no profanity, no gambling, no user-generated content sharing → expect Everyone / PEGI 3 |
| Government app | No |
| Financial features | No |
| Health apps | No |
| Data deletion URL | Not applicable (no account) |
| Privacy policy URL | https://aigenna.github.io/listo-app/privacy-policy.html |

## Sensitive permission declarations

| Permission | Status |
|---|---|
| `POST_NOTIFICATIONS` | Runtime permission, no Console declaration needed |
| `INTERNET` | No declaration needed |
| `RECEIVE_BOOT_COMPLETED` | No declaration needed |
| `SCHEDULE_EXACT_ALARM` | **Removed from the manifest.** List reminders use an inexact windowed alarm, so no Console declaration is required. |
| `com.google.android.gms.permission.AD_ID` | Merged in by the ads SDK; covered by declaring the advertising-ID data type above |
