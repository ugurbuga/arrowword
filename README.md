# Arrowword

Arrowword is a Kotlin Multiplatform (KMP) crossword-style puzzle game built with Compose Multiplatform.

This repository was developed with a **VibeCoding** workflow (iterative, AI-assisted, fast feedback loops).

## Targets

- **Android**
- **iOS**
- **Desktop (JVM)**

## Project Structure

- **`composeApp/`**
  - **`src/commonMain/`**: shared Kotlin + Compose UI
  - **`src/androidMain/`**: Android entry point
  - **`src/iosMain/`**: iOS entry point
  - **`src/jvmMain/`**: Desktop entry point
- **`iosApp/`**: Xcode project and iOS app host

## Dataset

The Turkish clue/answer dataset is stored as a JSON file:

- `composeApp/src/commonMain/composeResources/files/words/dataset_tr.json`

Each entry looks like:

```json
{"value": "KİRAZ", "text": "Kırmızı küçük meyve"}
```

## Build & Run

### Android

```bash
./gradlew :composeApp:assembleDebug
```

### Desktop (JVM)

Run the app:

```bash
./gradlew :composeApp:run
```

Build a distributable:

```bash
./gradlew :composeApp:createDistributable
```

### iOS

Open `iosApp/` in Xcode and run the `iosApp` scheme.

Alternatively, build from terminal:

```bash
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -destination 'generic/platform=iOS Simulator' clean build
```