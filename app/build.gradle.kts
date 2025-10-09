plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.alone.edgeview"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.alone.edgeview"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
                arguments +="-DANDROID_STL=c++_shared"
            }
        }

//
//        externalNativeBuild {
//            cmake {
//                cppFlags "-std=c++14"
//                abiFilters 'arm64-v8a', 'armeabi-v7a', 'x86', 'x86_64'
//                arguments "-DANDROID_STL=c++_shared"
//            }
//        }
        // Target both 32-bit and 64-bit ARM
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
        resources {
            pickFirsts += "**/libc++_shared.so"
        }
    }

buildFeatures {
viewBinding = true
}
}

dependencies {
implementation("androidx.core:core-ktx:1.9.0")
implementation("androidx.appcompat:appcompat:1.7.1")
implementation("com.google.android.material:material:1.13.0")
implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("org.java-websocket:Java-WebSocket:1.5.4")
    implementation("org.nanohttpd:nanohttpd:2.3.1")
    implementation("org.nanohttpd:nanohttpd-websocket:2.3.1")
testImplementation("junit:junit:4.13.2")
androidTestImplementation("androidx.test.ext:junit:1.3.0")
androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}