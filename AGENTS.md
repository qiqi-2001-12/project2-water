# AGENTS.md

## Architecture Overview

Green Building is an Android app for smart home/green building control with two modules:
- `app`: Main Android application using MVP pattern, EventBus, GreenDAO, Retrofit, RxJava
- `gateway`: Android library with JNI/native code for device communication via serial/RS485

App communicates with cloud via MQTT (Paho) and local devices via gateway library's serial port protocols.

Key data flows:
- UI (fragments in HomeActivity) -> Presenters -> MQTT/HTTP for remote commands
- MQTT topics: HD, HE, HF, HS, HX for different message types (see `mqtt/` directory)
- Local control: App -> Gateway JNI -> Serial port -> Devices (air conditioners, curtains, locks, etc.)

## Critical Workflows

- **Build Release APK**: `./gradlew assembleRelease` (signing config commented out in `app/build.gradle`, enable if needed)
- **Build Debug APK**: `./gradlew assembleDebug` (LOG_DEBUG=true via BuildConfig)
- **Run Tests**: `./gradlew test` (minimal tests present)
- **Native Build**: Automatic via CMake in `gateway/`, outputs to `jniLibs/`
- **Install to Device**: `adb install app/release/app-release.apk` (see README)

## Project-Specific Conventions

- **Logging**: Use `com.orhanobut:logger` with `Logger.d()`; disk logging in release (HyApplication.java)
- **Event Handling**: EventBus 3.0 for UI updates (e.g., TempSwitchUpdateEvent)
- **View Binding**: ButterKnife 10.2.0 with `@BindView` and `@OnClick`
- **Database**: GreenDAO 3.2.2 for local storage (models in `model/`)
- **Networking**: Retrofit 2.0-beta4 with RxJava 1.x/2.x, Gson converter
- **MQTT**: Paho client/service for pub/sub (MyMqttService.java)
- **Serial Communication**: Android-SerialPort library + custom JNI in gateway
- **Error Handling**: Bugly crash reporting, custom OtherExceptionsHandler
- **Build Config**: LOG_DEBUG flag controls logging adapter (AndroidLogAdapter vs DiskLogAdapter)

## Integration Points

- **External APIs**: MQTT broker (topics in `mqtt/`), HTTP endpoints via Retrofit
- **Device Protocols**: RS485 profiles in `gateway/src/main/cpp/RS485Profile/` (e.g., TypeCentralAirConditionZH.cpp)
- **Native Libs**: Protobuf, SQLite, OpenSSL in gateway JNI
- **Dependencies**: See `app/build.gradle` and `gateway/build.gradle`; force support-v4:27.1.0

## Key Files

- `HyApplication.java`: App initialization, global state
- `HomeActivity.java`: Main UI with vertical tab layout and fragments
- `SpDataProcessor.java`: Serial port data processing
- `MyMqttService.java`: MQTT connection and message handling
- `gateway/CMakeLists.txt`: Native build configuration
- `app/src/main/cpp/Android.mk`: App's NDK build (serial port)

Reference these for understanding component interactions and adding features.</content>
<parameter name="filePath">D:\project2\AGENTS.md
