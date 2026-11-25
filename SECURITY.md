# Security & Privacy Policy

## Offline-First Guarantee

PaisaBol is built with strict privacy principles for financial notifications:

- **No `INTERNET` permission** is declared in `AndroidManifest.xml` — the app cannot transmit notifications, amounts, or history over any network.
- **No third-party SDKs, ads, or analytics** are bundled.
- **Local storage only**: Received payment history is stored solely in a private SQLite database on your device (`paisabol_payments.db`) and can be cleared anytime from the **Payments** screen.
- **OTP & Debit Exclusion**: The notification parser explicitly rejects OTPs, verification codes, MPIN/TPIN messages, and outgoing debit notifications.

## Supported Versions

| Version | Supported |
| ------- | --------- |
| 1.0.x   | Yes       |

## Reporting a Vulnerability

If you discover a security or privacy issue, please **do not** open a public issue with sensitive details. Instead, report it privately via GitHub Security Advisories on this repository or contact the maintainer (`@shasantx`).

Please include:
- A clear description of the issue and steps to reproduce
- Android version and device model
- Any suggested remediation if known

We aim to acknowledge reports within 48 hours and address confirmed issues promptly.
