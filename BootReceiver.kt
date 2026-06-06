name: Build Release (Play Store)

# Manual trigger. Requires these repository secrets to be set:
#   KEYSTORE_BASE64     - base64 of your release keystore (.jks)
#   KEYSTORE_PASSWORD   - keystore password
#   KEY_ALIAS           - key alias
#   KEY_PASSWORD        - key password
# See README "Publishing to Google Play" for how to create these.

on:
  workflow_dispatch:

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'

      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: '8.10.2'

      - name: Decode keystore
        run: echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > azkar-release.jks

      - name: Build signed AAB and APK
        env:
          KEYSTORE_FILE: ${{ github.workspace }}/azkar-release.jks
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: |
          gradle bundleRelease --no-daemon
          gradle assembleRelease --no-daemon

      - name: Upload AAB (for Play Store upload)
        uses: actions/upload-artifact@v4
        with:
          name: azkar-tv-display-aab
          path: app/build/outputs/bundle/release/app-release.aab

      - name: Upload signed APK
        uses: actions/upload-artifact@v4
        with:
          name: azkar-tv-display-release-apk
          path: app/build/outputs/apk/release/app-release.apk
