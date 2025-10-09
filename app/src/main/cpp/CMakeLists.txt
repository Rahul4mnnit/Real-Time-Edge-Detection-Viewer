cmake_minimum_required(VERSION 3.10.2)
project("edgeview")

# Your native code file
add_library(native-lib SHARED native-lib.cpp)

# ---- Dynamic OpenCV Paths ----
# ANDROID_ABI is automatically set by Gradle/NDK (e.g., 'armeabi-v7a' or 'arm64-v8a')
set(OpenCV_INCLUDE_DIR "${CMAKE_SOURCE_DIR}/sdk/native/jni/include")

# *** MODIFIED: Use ANDROID_ABI to dynamically select the correct library path ***
set(OpenCV_LIB_DIR "${CMAKE_SOURCE_DIR}/sdk/native/libs/${ANDROID_ABI}")

include_directories(${OpenCV_INCLUDE_DIR})

# Import the OpenCV shared library
add_library(opencv_java4 SHARED IMPORTED)
set_target_properties(opencv_java4 PROPERTIES IMPORTED_LOCATION
        ${OpenCV_LIB_DIR}/libopencv_java4.so)

# Link native-lib with OpenCV and Android log lib
find_library(log-lib log)

# Ensure linking against a SHARD C++ runtime (this is the NDK default)
target_link_libraries(native-lib
        opencv_java4
        ${log-lib}
        )