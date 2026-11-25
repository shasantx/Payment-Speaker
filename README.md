<div align="center">

<img src="assets/logo.png" alt="PaisaBol Payment Speaker Logo" width="128" height="128" />

# PaisaBol — Nepali Payment Speaker

**An open-source Android payment soundbox app for Nepal.**  
Instantly speaks incoming **eSewa**, **Khalti**, **IME Pay**, **Fonepay**, **ConnectIPS**, and **Nepali bank** payments aloud in Nepali.

> *"ई-सेवा मार्फत ५०० रुपैयाँ प्राप्त भयो"*

[![Latest Release](https://img.shields.io/github/v/release/shasantx/Payment-Speaker?style=for-the-badge&color=E5A93C&label=Release)](https://github.com/shasantx/Payment-Speaker/releases/latest)
[![Android CI](https://img.shields.io/github/actions/workflow/status/shasantx/Payment-Speaker/android-ci.yml?style=for-the-badge&label=CI)](https://github.com/shasantx/Payment-Speaker/actions/workflows/android-ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-4CC38A.svg?style=for-the-badge)](LICENSE)
[![Android 8.0+](https://img.shields.io/badge/Android-8.0%2B-4CC38A?style=for-the-badge&logo=android&logoColor=white)](https://github.com/shasantx/Payment-Speaker/releases/latest)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-1A1A18?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-1A1A18?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/compose)

[Download APK](https://github.com/shasantx/Payment-Speaker/releases/latest) • [Features](#features) • [Supported Apps](#supported-nepali-payment-apps) • [Privacy](#privacy--security) • [Build from Source](#building-from-source) • [Contributing](CONTRIBUTING.md)

</div>

---

## Why PaisaBol?

Shopkeepers and businesses across Nepal rely on QR payments every day, yet physical soundbox hardware costs monthly fees and ties you to a single provider. **PaisaBol** turns any Android phone into a free, offline, multi-wallet **Nepali Payment Speaker** with a calm matte hardware interface and zero clutter.

## Features

- **Guided First-Launch Setup**: Clean 3-step onboarding with live interactive status checks for Notification Access, Background Speaker Alert, and Unrestricted Battery.
- **Focused Two-Screen Interface**:
  1. **Speaker** — Soundbox status deck showing today's received total (`Rs. 12,450`) in tabular figures, one-tap **Speaker on / Turn on speaker** toggle, **Test voice**, **Auto volume boost**, **Language switcher** (`नेपाली` / `Roman` / `English`), recent verified payments, and a compact Voice and permissions sheet.
  2. **Payments** — Verified incoming payments grouped by date (`Today`, `Yesterday`, older dates) with instant search (supports English, commas, and Devanagari numerals), source filters (`All`, `eSewa`, `Khalti`, `IME Pay`, `Banks`), full receipt detail sheet, and **tap-to-replay** voice announcements.
- **Reliable Nepali Voice Engine**:
  - Uses Android `TextToSpeech` with smart locale fallback (`ne-NP` → `hi-IN` → `en-IN`).
  - Handles rupees and paisa accurately (e.g., *"५०० रुपैयाँ प्राप्त भयो"* and *"१२५ रुपैयाँ ५० पैसा प्राप्त भयो"*).
  - Properly queues back-to-back customer payments while keeping audio focus, volume boost, and wake lock active until the final utterance completes.
- **Hardened Notification Filtering**:
  - Parses both English and Devanagari (`०-९`) payment alerts, including InboxStyle notifications.
  - Ignores balance figures when a transaction amount is present, and excludes currency tokens from sender names.
  - Rejects outgoing debits, OTPs, failed transactions, promotional offers, personal phone-number/contact SMS spoofing, and chat/social apps.
  - Suppresses duplicate notifications within a 10-second window.

## Supported Nepali Payment Apps

| App / Network | Detection | Voice Name |
| --- | --- | --- |
| **eSewa** | App notifications & verified SMS | ई-सेवा |
| **Khalti** | App notifications & verified SMS | खल्ती |
| **IME Pay** | App notifications & verified SMS | आइएमई पे |
| **Fonepay** | Merchant & bank alerts | फोनपे |
| **ConnectIPS** | App notifications & verified SMS | कनेक्ट आइपिएस |
| **Nepali Mobile Banking** | Nabil, NIC Asia, Global IME, Siddhartha, Himalayan, Sanima, Prabhu, Kumari, Everest, Citizens, NMB, Laxmi Sunrise, MBL, NIMB, Prime, RBB, ADBL, Nepal Bank, Garima, Muktinath, Jyoti, Shine Resunga, Kamana Sewa, Mahalaxmi, Shangrila, F1Soft BankSmart & verified bank SMS alerts | बैंक |

Missing your bank's notification format? [Open a Bank Support Request](https://github.com/shasantx/Payment-Speaker/issues/new?template=bank_app_request.md).

## Privacy & Security

PaisaBol is designed to be **100% private and offline**:

- **Zero Internet Permission**: The app does not request `android.permission.INTERNET`. Nothing ever leaves your phone.
- **No Bank Login or Cloud Sync**: Works via Android's standard `NotificationListenerService` with local SQLite storage only.
- **Strict Anti-Spoofing**: Blocks chat and social apps (WhatsApp, Viber, Messenger, Telegram, etc.) and rejects personal phone numbers and saved contact names over SMS so forged texts can never trigger false announcements.
- **Ignores OTPs & Sensitive Codes**: Explicitly skips OTP, MPIN, TPIN, and verification code alerts.

See [SECURITY.md](SECURITY.md) for full details.

## Download & Install

1. Go to [**GitHub Releases**](https://github.com/shasantx/Payment-Speaker/releases/latest) and download the latest **`PaisaBol-*.apk`**.
2. Open the APK on your Android phone (Android 8.0 / API 26 or newer) and install.
3. Follow the first-launch setup to allow **Notification access** and disable battery restrictions for reliable background alerts.
4. Tap **Test voice** to verify volume and Nepali TTS clarity — real incoming payments will then be spoken and logged automatically.

## Tech Stack & Architecture

- **Language:** Kotlin 2.0
- **UI:** Jetpack Compose + Material 3 + custom matte hardware acoustic Canvas components
- **Typography:** Bundled Plus Jakarta Sans with tabular figures (`tnum`)
- **Storage:** Native Android SQLite (`PaymentDatabase`) + reactive `StateFlow`
- **Background Listener:** `NotificationListenerService` + `ForegroundService` (`specialUse`) + `BootReceiver`
- **Voice:** Android `TextToSpeech` (`ne-NP` / `hi-IN` / `en-IN`) with `AudioFocusRequest` & `WakeLock`

```text
app/src/main/java/com/paisabol/paymentspeaker/
├── data/        # SQLite PaymentDatabase & SharedPreferences SettingsStore
├── domain/      # PaymentParser, PaymentDeduplicator, NepaliSpeechFormatter
├── model/       # PaymentRecord, PaymentSource, SpeechLanguage, AppSettings
├── service/     # PaymentNotificationListener, ForegroundService, NepaliSpeechEngine, BootReceiver
├── ui/
│   ├── components/  # Soundbox status deck, acoustic mark, badges, bottom dock
│   ├── screens/     # OnboardingScreen, SpeakerHomeScreen, PaymentsListScreen
│   └── theme/       # Warm hardware charcoal & matte brass color/typography system
└── util/        # PermissionHelper
```

## Building from Source

### Prerequisites
- JDK 21 (or Android Studio Ladybug / newer)
- Android SDK Platform 36 (`compileSdk = 36`, `minSdk = 26`)

### Commands
```bash
# Clone the repository
git clone https://github.com/shasantx/Payment-Speaker.git
cd Payment-Speaker

# Run unit tests
./gradlew testDebugUnitTest

# Build release APK
./gradlew assembleRelease
```

The compiled APK will be generated at `app/build/outputs/apk/release/app-release.apk`.

## Contributing

Contributions are welcome — especially new Nepali bank notification patterns, parser test cases, and voice improvements.  
Please read [CONTRIBUTING.md](CONTRIBUTING.md) and our [Code of Conduct](CODE_OF_CONDUCT.md) before submitting a PR.

## License

This project is open source and licensed under the **MIT License** — see [LICENSE](LICENSE) for details.
