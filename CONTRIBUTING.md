# Contributing to PaisaBol

Thank you for helping improve **PaisaBol** for Nepalese users and merchants! Every contribution — from adding a new bank's notification format to fixing bugs or improving Nepali TTS pronunciation — is warmly welcomed.

## Ways to Contribute

1. **Add or improve Nepali bank / wallet notification patterns** in `PaymentParser.kt` with unit tests in `PaymentParserTest.kt`.
2. **Report missed or misparsed payment notifications** using the [Bank App Support Request](.github/ISSUE_TEMPLATE/bank_app_request.md) template (remember to redact personal names, phone numbers, and account numbers!).
3. **Improve Nepali voice announcements** in `NepaliSpeechFormatter.kt` and `NepaliSpeechEngine.kt`.
4. **Fix bugs or polish the Compose UI** while keeping the app focused, fast, and minimal.

## Development Setup

1. Fork this repository and clone your fork:
   ```bash
   git clone https://github.com/<your-username>/Payment-Speaker.git
   cd Payment-Speaker
   ```
2. Open the project in Android Studio (JDK 21 / Android SDK 36) or build from the command line:
   ```bash
   ./gradlew testDebugUnitTest assembleDebug
   ```
3. Create a feature branch from `main`:
   ```bash
   git checkout -b feat/your-improvement
   ```

## Adding a New Bank / Wallet Parser Rule

When adding support for a new Nepali wallet or bank notification:

1. Update `exactPackageSources` or `bankPackageKeywords` in `app/src/main/java/com/paisabol/paymentspeaker/domain/PaymentParser.kt`.
2. Add at least one positive test (incoming payment) and one negative test (outgoing debit / OTP) in `app/src/test/java/com/paisabol/paymentspeaker/PaymentParserTest.kt`.
3. Run `./gradlew testDebugUnitTest` and confirm all tests pass.

## Pull Request Guidelines

- Keep PRs focused on a single change.
- Follow official Kotlin code style (`kotlin.code.style=official`).
- Never introduce `INTERNET` permissions, analytics trackers, or third-party data collection — PaisaBol is strictly offline and private.
- Ensure CI (`testDebugUnitTest` and `assembleRelease`) passes cleanly.
