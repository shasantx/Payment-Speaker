# Changelog

All notable changes to **PaisaBol — Nepali Payment Speaker** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-11-25

### Added
- Custom matte hardware **PaisaBol** app logo (`assets/logo.png`) and matching Android adaptive launcher icon.
- Expanded Nepali bank coverage (Garima, Muktinath, Jyoti, Shine Resunga, Kamana Sewa, Mahalaxmi, Shangrila, Namaste Pay, Moru).
- InboxStyle (`EXTRA_TEXT_LINES`) notification parsing and relative date/time display on the Speaker screen's Recent list.
- Additional unit tests covering saved-contact SMS spoofing, balance-prefix filtering, currency-token sender exclusion, rounding edge cases, and English singular/plural phrasing.

### Changed
- Redesigned the entire interface in a calm warm-charcoal and matte-brass hardware direction: perforated acoustic speaker deck, strict 4dp spacing rhythm, consistent 18dp iconography, crisp hairline surfaces, and labeled bottom navigation dock.
- Made onboarding and bottom sheets vertically scrollable and state-preserving (`rememberSaveable`) across configuration changes and Settings transitions.
- Offloaded SQLite writes to background threads so voice announcements trigger immediately without blocking the notification listener.

### Fixed
- Fixed back-to-back payment utterances releasing volume boost, audio focus, and wake lock prematurely before queued announcements finished speaking.
- Fixed currency formatting rounding overflow (e.g., `99.996` now formats as `Rs. 100` instead of `Rs. 99.100`).
- Fixed `"credited by Rs. ..."` notifications mistakenly extracting `"Rs"` or `"Npr"` as the sender name.
- Fixed midnight rollover so today's total recalculates automatically on resume.
- Hardened SMS sender verification to reject saved personal contact names in addition to personal phone numbers.
