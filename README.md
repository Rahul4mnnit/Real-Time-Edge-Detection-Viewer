🧪 Real-Time Edge Detection Viewer

Android + OpenCV (C++) + OpenGL ES + Web (TypeScript)
Technical Assessment – RnD Intern

🚀 Overview

This project demonstrates a real-time camera processing pipeline using:

Android Camera API (Java/Kotlin)

OpenCV (C++) via JNI for image processing

OpenGL ES for rendering processed frames

TypeScript-based web viewer to display sample output

🧩 Features Implemented

Android App:

Live camera feed using TextureView / SurfaceTexture

JNI bridge for Java ↔ C++ communication

OpenCV C++ for edge detection or grayscale conversion

OpenGL ES 2.0 rendering of processed frames

Toggle between raw and processed view

FPS counter for performance check


screenshots: There are two output available as a screenshot for seeing the final output


Setup Instructions :
Android Setup
Prerequisites

Android Studio (latest)

NDK (Native Development Kit) installed via SDK Manager

CMake and LLDB components installed

OpenCV Android SDK


Architecture Overview :
🔸 Android Flow
Camera (Camera2 API)
↓
TextureView Frame
↓
JNI Bridge (Java → C++)
↓
OpenCV Processing (C++)
↓
OpenGL ES Renderer → Screen Output
🔹 Native (C++ / OpenCV)

Handles frame conversion, Canny edge detection, and pixel buffer output.

Sends processed texture buffer to OpenGL renderer.


Web (TypeScript):
Static Image / Mock Data
↓
TypeScript DOM Update
↓
HTML Canvas Rendering


