# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp/lib_include)
set(jnilibs "${CMAKE_SOURCE_DIR}/src/main/jniLibs")
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${jnilibs}/${ANDROID_ABI})
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

add_library( # Sets the name of the library.
        CWinobleLib

        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).
        src/main/cpp/Main/WinobleMain.cpp src/main/cpp/Main/PublicDefine.cpp src/main/cpp/Main/PublicTimer.cpp
        src/main/cpp/DataBase/DataBase.cpp src/main/cpp/DataBase/DeviceList.cpp
        src/main/cpp/DataType/ArrayList.cpp src/main/cpp/DataType/DeviceTypeInfo.cpp src/main/cpp/DataType/TypeDeviceInfo.cpp
        src/main/cpp/Json/cJSON.cpp src/main/cpp/lib_include/openssl/base64.c src/main/cpp/lib_include/openssl/md5.c
        src/main/cpp/NetWork/httpLogin.cpp src/main/cpp/NetWork/NetWorkBase.cpp src/main/cpp/NetWork/NetWorkCMD.cpp src/main/cpp/NetWork/NetWorkCMDParsing.cpp
        src/main/cpp/NetWork/RobotDocking.cpp
        src/main/cpp/SerialPort/SerialPortBase.cpp src/main/cpp/SerialPort/SerialDrive.cpp src/main/cpp/SerialPort/SerialProces.cpp src/main/cpp/SerialPort/TypeZclProfile.cpp
        src/main/cpp/SerialPort/cc2538ZNPDownLoad.cpp
        src/main/cpp/RS485Profile/RS485Profile.cpp src/main/cpp/RS485Profile/TypeSmartDoorLockHLS.cpp src/main/cpp/RS485Profile/TypeCentralAirConditionZH.cpp
        src/main/cpp/RS485Profile/TypeElectricCurtainDY.cpp src/main/cpp/RS485Profile/TypeElectricCurtainBS.cpp src/main/cpp/RS485Profile/TypeRGBLL.cpp
        src/main/cpp/RS485Profile/TypeVoiceHW.cpp src/main/cpp/RS485Profile/TypeCentralAirConditionMD.cpp src/main/cpp/RS485Profile/TypeTemperatureControl.cpp
        src/main/cpp/RS485Profile/TypeTemperatureControlLF.cpp
        src/main/cpp/RS485Profile/TypeElectricCurtainSX.cpp
        src/main/cpp/RS485Profile/TypeElectricCurtainAK.cpp
        src/main/cpp/RS485Profile/TypeElectricCurtain485AK.cpp
        src/main/cpp/RS485Profile/TypePresenceSense.cpp
        src/main/cpp/RS485Profile/TypeAirSwitch.cpp
        src/main/cpp/protobuf/api_cmd.pb.cc
        src/main/cpp/protobuf/basic.pb.cc
        src/main/cpp/protobuf/device.pb.cc
        src/main/cpp/protobuf/family.pb.cc
        src/main/cpp/protobuf/gateway.pb.cc
        src/main/cpp/protobuf/login.pb.cc
        src/main/cpp/protobuf/msg_unread.pb.cc
        src/main/cpp/protobuf/room.pb.cc
        src/main/cpp/protobuf/scene.pb.cc
        src/main/cpp/protobuf/push.pb.cc
        src/main/cpp/protobuf/ezdevice.pb.cc
        src/main/cpp/protobuf/hfonts.pb.cc
        src/main/cpp/protobuf/appliance.pb.cc
        src/main/cpp/protobuf/ota.pb.cc
        )
# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

add_library( # Sets the name of the library.
        sqlite3

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        IMPORTED)
add_library( # Sets the name of the library.
        protobuf_lite

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        IMPORTED)
set_target_properties(sqlite3
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libsqlite3.so)
set_target_properties(protobuf_lite
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libprotobuf_lite.so)

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        CWinobleLib
        protobuf_lite
        sqlite3
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})