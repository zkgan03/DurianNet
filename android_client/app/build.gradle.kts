plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    id("androidx.navigation.safeargs.kotlin")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")

    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.duriannet"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.duriannet"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true //kj added
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    androidResources {
        noCompress += "tflite"
    }
    buildFeatures {
        viewBinding = true
    }
    /*sourceSets {
        getByName("main") {
            assets {
                srcDirs("src\\main\\assets", "src\\main\\assets")
            }
        }
    }*/
    sourceSets {
        getByName("main") {
            assets.srcDirs("src/main/assets")
        }
    }
}

dependencies {

    // Logging Interceptor for OkHttp
    implementation ("com.squareup.okhttp3:logging-interceptor:4.11.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Navigation library
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    //SignalR
    val signalrVersion = "8.0.6"
    implementation("com.microsoft.signalr:signalr:${signalrVersion}")
    implementation("com.microsoft.signalr.messagepack:signalr-messagepack:${signalrVersion}")
    // for logging of signalr
    implementation("org.slf4j:slf4j-jdk14:1.7.25")

    // CameraX
    val cameraxVersion = "1.3.4"
    implementation("androidx.camera:camera-camera2:${cameraxVersion}")
    implementation("androidx.camera:camera-lifecycle:${cameraxVersion}")
    implementation("androidx.camera:camera-view:${cameraxVersion}")

    //Gson
    implementation("com.google.code.gson:gson:2.11.0")

    // tensorflow lite
    val tfliteVersion = "0.4.4"
    implementation("org.tensorflow:tensorflow-lite-support:$tfliteVersion")
    implementation("org.tensorflow:tensorflow-lite-task-vision:$tfliteVersion") // Task API
    implementation("org.tensorflow:tensorflow-lite-gpu-delegate-plugin:$tfliteVersion")
    val tfliteGpuVersion = "2.16.1"
    implementation("org.tensorflow:tensorflow-lite-gpu-api:$tfliteGpuVersion")
    implementation("org.tensorflow:tensorflow-lite-gpu:$tfliteGpuVersion")

    // Glide for image loading
    val glideVer = "4.16.0"
    implementation("com.github.bumptech.glide:glide:$glideVer")
    annotationProcessor("com.github.bumptech.glide:compiler:$glideVer")

    // Palette for extracting colors from images
    implementation("androidx.palette:palette-ktx:1.0.0")

    // https://mvnrepository.com/artifact/org.opencv/opencv
//    implementation("org.opencv:opencv:4.10.0")

    //google maps
    val mapsKtxVer = "5.1.1"
    implementation("com.google.android.gms:play-services-maps:19.0.0") // Maps SDK for Android
    implementation("com.google.android.gms:play-services-location:21.3.0") //
    implementation("com.google.maps.android:maps-ktx:$mapsKtxVer")
    implementation("com.google.maps.android:android-maps-utils:3.8.2")
    implementation("com.google.maps.android:maps-utils-ktx:$mapsKtxVer")

    //Retrofit
    val retrofitVersion = "2.11.0"
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")

    // dagger hilt
    val hiltVersion = "2.52"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    kapt("com.google.dagger:hilt-android-compiler:$hiltVersion")
    implementation("androidx.hilt:hilt-navigation-fragment:1.2.0")
}

kapt {
    correctErrorTypes = true
}