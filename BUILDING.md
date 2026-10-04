# Building locally

Use Android Studio to import this project. Android Studio will use the configured Android Gradle Plugin/Kotlin versions and can generate the Gradle wrapper.

Alternatively, from an Android SDK/Gradle environment, generate a wrapper with:

    gradle wrapper --gradle-version 8.9

Then build with:

    ./gradlew assembleDebug
