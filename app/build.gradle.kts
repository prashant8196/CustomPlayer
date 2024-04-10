plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "com.example.customplayer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.customplayer"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.3.4"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
   viewBinding.enable = true
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.leanback:leanback:1.0.0")
    implementation("com.github.bumptech.glide:glide:4.14.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(project(mapOf("path" to ":ptplayer")))
    implementation("androidx.media3:media3-common:1.2.0")

    //Testing Dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("androidx.multidex:multidex:2.0.1")

        val media3_version = "1.2.1"

        // For media playback using ExoPlayer
        implementation ("androidx.media3:media3-exoplayer:$media3_version")

        // For DASH playback support with ExoPlayer
        implementation ("androidx.media3:media3-exoplayer-dash:$media3_version")
        // For HLS playback support with ExoPlayer
        implementation ("androidx.media3:media3-exoplayer-hls:$media3_version")
        // For RTSP playback support with ExoPlayer
        implementation ("androidx.media3:media3-exoplayer-rtsp:$media3_version")
        // For ad insertion using the Interactive Media Ads SDK with ExoPlayer
        implementation ("androidx.media3:media3-exoplayer-ima:$media3_version")

        // For loading data using the Cronet network stack
        implementation ("androidx.media3:media3-datasource-cronet:$media3_version")
        // For loading data using the OkHttp network stack
        implementation ("androidx.media3:media3-datasource-okhttp:$media3_version")
        // For loading data using librtmp
        implementation ("androidx.media3:media3-datasource-rtmp:$media3_version")

        // For building media playback UIs
        implementation ("androidx.media3:media3-ui:$media3_version")
        // For building media playback UIs for Android TV using the Jetpack Leanback library
        implementation ("androidx.media3:media3-ui-leanback:$media3_version")

        // For exposing and controlling media sessions
        implementation ("androidx.media3:media3-session:$media3_version")

        // For extracting data from media containers
        implementation ("androidx.media3:media3-extractor:$media3_version")

        // For integrating with Cast
        implementation ("androidx.media3:media3-cast:$media3_version")

        // For scheduling background operations using Jetpack Work's WorkManager with ExoPlayer
        implementation ("androidx.media3:media3-exoplayer-workmanager:$media3_version")

        // For transforming media files
        implementation ("androidx.media3:media3-transformer:$media3_version")

        // For applying effects on video frames
        implementation ("androidx.media3:media3-effect:$media3_version")

        // For muxing media files
        implementation ("androidx.media3:media3-muxer:$media3_version")

        // Utilities for testing media components (including ExoPlayer components)
        implementation ("androidx.media3:media3-test-utils:$media3_version")
        // Utilities for testing media components (including ExoPlayer components) via Robolectric
        implementation ("androidx.media3:media3-test-utils-robolectric:$media3_version")

        // Common functionality for media database components
        implementation ("androidx.media3:media3-database:$media3_version")
        // Common functionality for media decoders
        implementation ("androidx.media3:media3-decoder:$media3_version")
        // Common functionality for loading data
        implementation ("androidx.media3:media3-datasource:$media3_version")
        // Common functionality used across multiple media libraries
        implementation ("androidx.media3:media3-common:$media3_version")

}