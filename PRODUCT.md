# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users

People who play audio files stored on their Android device, including Quran recitations.

## Product Purpose

Resonix is an Android audio player for listening to locally available music and Quran audio. Success means users can find and play their device audio in the interface style they prefer.

## Capabilities and Constraints

- The existing Android app uses Kotlin, Jetpack Compose, Material 3, Hilt, Room, and Media3.
- Arabic and English are supported, including right-to-left layout for Arabic.
- Quran classification and playback protection are part of the existing product and must remain available.
- The app offers five selectable visual themes. The first four are based on the images in `ui theme/theme1` through `theme4`; the fifth is an authorized Android/Compose port of the user's Neumorphism Music Player fork.
- Selectable launcher icons are based on the images in `ui theme/icon`.
- Design references are inspiration for layout and visual treatment; their artwork, photos, album covers, logos, brand names, and lyrics must not be copied into the app or prototype.
- A local browser prototype previews all five themes. Android builds must run through GitHub Actions, not local Gradle.
- Users should be able to view the prototype from a phone browser while the phone and development computer are reachable on the same local network.

## Evidence on Hand

- Existing Android implementation in `app/src/main`.
- Four sets of visual references and an icon set in the ignored `ui theme/` folder.
- No user-owned album artwork was identified as an asset for the prototype; use neutral cover placeholders.

## Product Principles

- Keep local audio playback straightforward.
- Treat Quran playback and its protection as a first-class use case.
- Preserve Arabic/English usability and correct layout direction.
- Let users choose the visual theme and launcher icon that suit them.
