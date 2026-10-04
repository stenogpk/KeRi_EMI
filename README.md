# KeRi EMI Calculator

**Developed by Shartendu**

A native Android loan and EMI estimator built with Kotlin and Jetpack Compose.

## Features
- Live reducing-balance EMI calculation
- Adjustable loan amount, annual interest rate and tenure
- Processing, insurance and documentation fee inputs
- In-hand disbursal and total cost summary
- Principal / interest / charges breakdown chart
- Year-wise principal, interest and outstanding balance schedule
- Offline-first; no account, analytics SDK or network permission

## Calculation model
EMI uses the standard reducing-balance formula. Fees are shown separately and treated as upfront deductions from the sanctioned principal for displayed in-hand disbursal. Actual lender fee treatment can differ.

## Build
Requires JDK 17 and Gradle 8.11.1:
```bash
gradle testDebugUnitTest
gradle assembleDebug
```
APK output: `app/build/outputs/apk/debug/app-debug.apk`

GitHub Actions runs unit tests, builds the APK and uploads it as a workflow artifact on pushes to `main`.

## App identity
- Name: KeRi EMI Calculator
- Package: `com.keri.emi`
- Minimum Android: 6.0 (API 23)
- Target Android: 16 (API 36)
