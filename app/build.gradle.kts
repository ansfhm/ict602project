plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}


dependencies {
    implementation ("com.squareup.retrofit2:retrofit:2.9.0");
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0");
    implementation ("com.squareup.okhttp3:okhttp:4.9.0");
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.0");
    implementation ("com.squareup.picasso:picasso:2.71828");// For image loading
    implementation ("androidx.constraintlayout:constraintlayout:2.0.4");
    implementation ("com.android.volley:volley:1.2.1");
    implementation ("com.google.code.gson:gson:2.10.1");
    implementation ("com.google.android.gms:play-services-maps:18.0.2");
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.volley)
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}