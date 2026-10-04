# FocusGram

FocusGram is an Android WebView-based Instagram focus client prototype.

## Goal

Reduce algorithmic scrolling while retaining access to useful Instagram functions such as:

- Direct Messages
- Notifications
- Profile pages
- Followed-content feed
- Individual shared Reel pages

The project performs its UI modifications locally in the WebView. It does not implement Instagram private APIs, credential extraction, analytics, message collection, or server-side modifications.

## Architecture
<img width="8152" height="5725" alt="diagram" src="https://github.com/user-attachments/assets/51195538-04fc-430b-baff-c2f1e7e327a1" />

## Main components

### MainActivity.kt

Responsible for:

- WebView creation
- WebView security configuration
- Instagram URL allow-listing
- URL/context detection
- back navigation
- lifecycle cleanup

### FocusInjector.kt

Injects:

- CSS selectors that hide Explore/Reels navigation elements
- a MutationObserver to reapply filtering after DOM changes
- touch/wheel prevention while viewing a `/reel/` URL

## Build

Open the project in Android Studio with Android SDK 35 installed.

Then run:

```bash
./gradlew assembleDebug
```

The debug APK will be generated under:

```text
app/build/outputs/apk/debug/app-debug.apk
```

On Windows:

```bat
gradlew.bat assembleDebug
```

## Important compatibility note

Instagram's web UI is not a stable public DOM contract. CSS selectors and URL behavior can change without notice. The implementation therefore fails closed at the UI-filtering layer: if selectors stop matching, the underlying WebView remains usable rather than crashing.

## Security choices

- HTTPS-only network configuration
- JavaScript enabled because Instagram Web requires it
- Local DOM manipulation only
- Third-party cookies disabled
- File/content access disabled
- No analytics SDK
- No credential handling outside Instagram's own web login
- No private Instagram API integration

## Limitations

This is a prototype. A production release should be tested against the current Instagram mobile web experience and reviewed for platform terms, WebView behavior, accessibility, cookie/session behavior, and Play Store requirements.

The anti-scroll behavior is intentionally scoped to individual `/reel/` contexts and does not attempt to interfere with ordinary DM interactions.
