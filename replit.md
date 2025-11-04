# GesPay Admin - Android Application

## Overview
GesPay Admin is an Android mobile application for managing payment lokets (kiosks/stations). The application is built with modern Android development practices using Kotlin and follows the MVVM (Model-View-ViewModel) architecture pattern with Clean Architecture principles.

**Project Type:** Android Mobile Application (Native)  
**Language:** Kotlin  
**Build System:** Gradle (Kotlin DSL)  
**Min SDK:** 24 (Android 7.0)  
**Target SDK:** 34 (Android 14)

## Current State
This is a GitHub import that has been configured to run in the Replit environment. The project can be built to generate an APK file that can be installed on Android devices or emulators.

## Project Architecture

### Technology Stack
- **Language:** Kotlin 1.9.22
- **Architecture:** MVVM + Clean Architecture
- **Dependency Injection:** Hilt (Dagger)
- **Networking:** Retrofit + OkHttp
- **Async Operations:** Kotlin Coroutines + Flow
- **Navigation:** Android Navigation Component
- **UI:** Android View Binding (XML layouts)
- **Security:** AndroidX Security Crypto for secure data storage

### Project Structure
```
app/src/main/
├── java/com/proyek/maganggsp/
│   ├── data/              # Data layer (API, DTOs, repositories)
│   │   ├── api/           # Retrofit API interfaces
│   │   ├── dto/           # Data Transfer Objects
│   │   └── repositoryImpl/ # Repository implementations
│   ├── domain/            # Business logic layer
│   │   ├── model/         # Domain models
│   │   ├── repository/    # Repository interfaces
│   │   └── usecase/       # Use cases (business logic)
│   ├── presentation/      # UI layer (Activities, Fragments, ViewModels)
│   │   ├── login/         # Login screen
│   │   ├── main/          # Main container activity
│   │   ├── home/          # Home screen with loket monitoring
│   │   ├── detail_loket/  # Loket detail and transactions
│   │   ├── profile/       # Profile management
│   │   └── transaction/   # Transaction history
│   ├── di/                # Dependency injection modules
│   └── util/              # Utility classes and helpers
└── res/                   # Resources (layouts, drawables, strings, etc.)
```

### Key Features
1. **Authentication:** Admin login system
2. **Loket Management:** Monitor and manage payment kiosks/stations
3. **Transaction Monitoring:** View transaction logs and receipts
4. **Loket Status Tracking:** Track loket status (normal, flagged, blocked)
5. **Profile Management:** Update admin profile information
6. **Search History:** Search through loket history

### API Configuration
- **Debug Base URL:** `https://dev-api.gespay.co.id/`
- **Release Base URL:** `https://api.gespay.co.id/`
- The app communicates with a backend API for all data operations

## Build Instructions

### Building the APK
The project uses Gradle as the build system. To build the application:

```bash
./gradlew assembleDebug    # Build debug APK
./gradlew assembleRelease  # Build release APK
```

The generated APK will be located at:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

### Running Tests
```bash
./gradlew test             # Run unit tests
./gradlew connectedAndroidTest  # Run instrumented tests
```

## Dependencies
The project uses the following major dependencies:
- **AndroidX Core & AppCompat** - Core Android libraries
- **Material Design Components** - UI components following Material Design
- **Navigation Component** - Fragment navigation
- **Lifecycle Components** - ViewModel and LiveData
- **Hilt** - Dependency injection
- **Retrofit** - REST API client
- **OkHttp** - HTTP client with logging
- **Gson** - JSON serialization
- **Coroutines** - Asynchronous programming
- **Shimmer** - Loading placeholders
- **Security Crypto** - Encrypted SharedPreferences

## Development Notes

### Important Configuration
1. The app requires internet permissions (already configured in AndroidManifest.xml)
2. Clear text traffic is enabled for development purposes
3. The app uses portrait orientation for all screens
4. Session management is handled in MainActivity

### Build Configuration
- The project uses buildConfig feature to inject configuration values
- Debug and release builds use different API base URLs
- ProGuard is disabled for both debug and release builds (can be enabled for production)

## Deployment
This is a mobile application and cannot be deployed as a web service. To distribute:
1. Build the APK using Gradle
2. Distribute the APK file to users for installation on Android devices
3. For production, sign the APK and publish to Google Play Store or other distribution channels

## Recent Changes
- 2025-11-04: Initial project import and Replit environment setup
- Configured Java/Kotlin development toolchain
- Set up build workflow for generating APK files
