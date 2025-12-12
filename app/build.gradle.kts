plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.habittracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.habittracker"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Material Design
    implementation("com.google.android.material:material:1.9.0")
// RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.1")
// CardView
    implementation("androidx.cardview:cardview:1.0.0")
// ConstraintLayout
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
// Gson for JSON serialization (SharedPreferences)
    implementation("com.google.code.gson:gson:2.10.1")
// Core KTX
    implementation("androidx.core:core-ktx:1.10.1")
// Fragment KTX
    implementation("androidx.fragment:fragment-ktx:1.6.1")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}