# Dictio

> Pronunciation practice for language learners

This app aims to help language learners improve their pronunciation for wide variety of languages.

<a href='https://play.google.com/store/apps/details?id=com.mpontus.dictio&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width="153" /></a>

**DISCLAIMER: This is a proof-of-concept app and may be pulled from Play Store without advance notice.**

## Screenshots

[<img src="https://i.imgur.com/7DiQafj.jpg" width="160" />](https://i.imgur.com/7DiQafj.jpg)
[<img src="https://i.imgur.com/jUDWqvi.jpg" width="160" />](https://i.imgur.com/jUDWqvi.jpg)
[<img src="https://i.imgur.com/YkNA5Pu.jpg" width="160" />](https://i.imgur.com/YkNA5Pu.jpg)

## Installation

The source code of this app is made up of several modules: 

- `app` is the android application, main techonologies used: [Architecture Components](https://developer.android.com/topic/libraries/architecture/), [RxJava](https://github.com/ReactiveX/RxJava), [EasyFlow](https://github.com/Beh01der/EasyFlow), [Android Text-To-Speech](https://developer.android.com/reference/android/speech/tts/TextToSpeech).

- `backend` built with Google Endpoints Framwork is responsible for serving prompts as a single JSON collection and generating temporary access tokens for Google Cloud Speech service.

- `speech` module provides voice recorder and a client for Google Cloud Speech service.

Follow the steps to deploy your own backend and build the app:

1. Sign up for [Google Cloud](https://cloud.google.com/) and create a new project.
2. Install and configure [Google Cloud SDK](https://cloud.google.com/sdk/install) on your workstation
3. [Enable Google Cloud Speech API](https://cloud.google.com/speech-to-text/docs/quickstart) for your project and save service credentials file `service-credentials.json` to [`backend/src/main/webapp/WEB-INF/`](backend/src/main/webapp/WEB-INF/)
4. Replace `projectId` in [`backend/build.gradle`](backend/build.gradle) with your project id
5. Run `./gradlew :backend:appengineDeploy` to deploy the backend
6. Open the project in Android Studio and run the app

To generate signed APK set the following variables in `~/.gradle/gradle.properties`:

```
DICTIO_RELEASE_STORE_FILE=<path to keystore>
DICTIO_RELEASE_KEY_ALIAS=<key alias>
DICTIO_RELEASE_STORE_PASSWORD=<store password>
DICTIO_RELEASE_KEY_PASSWORD=<key password>
```

## License

[GPL](LICENSE) @ Mikhail Pontus
