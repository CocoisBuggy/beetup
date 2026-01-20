plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.ksp)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.roborazzi)
}

android {
  namespace = "com.coco.beetup"
  compileSdk { version = release(36) }

  defaultConfig {
    applicationId = "com.coco.beetup"
    minSdk = 33
    targetSdk = 36

    versionCode = 15
    versionName = "1.3.22"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    debug {
      applicationIdSuffix = ".debug"
      versionNameSuffix = "-debug"
    }
    release {
      isMinifyEnabled = false
      proguardFiles(
          getDefaultProguardFile("proguard-android-optimize.txt"),
          "proguard-rules.pro",
      )
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    isCoreLibraryDesugaringEnabled = true
  }
  kotlinOptions { jvmTarget = "11" }
  buildFeatures { compose = true }

  testOptions { unitTests { isIncludeAndroidResources = true } }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.ui.text.google.fonts)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.extended)

  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.androidx.work.runtime.ktx)
  ksp(libs.androidx.room.compiler)

  // extra
  coreLibraryDesugaring(libs.desugar)
  implementation(libs.datetime.names)
  implementation(libs.vico.compose)
  implementation(libs.vico.compose.m3)

  testImplementation(libs.junit)
  testImplementation(libs.ui.test.junit4)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.rule)
  testImplementation(libs.androidx.espresso.core)
  testImplementation(libs.androidx.junit)
  // Additional test dependencies
  testImplementation("org.mockito:mockito-core:5.14.2")
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
  testImplementation("androidx.arch.core:core-testing:2.2.0")
  testImplementation("androidx.room:room-testing:2.8.4")

  androidTestImplementation(libs.robolectric)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  // Additional Android test dependencies
  androidTestImplementation("androidx.room:room-testing:2.8.4")
  androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
  debugImplementation(libs.androidx.compose.ui.tooling)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
}

ksp { arg("room.schemaLocation", "$projectDir/schemas") }
