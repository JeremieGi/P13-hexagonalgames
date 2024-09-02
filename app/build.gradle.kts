plugins {
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.kotlin)
  alias(libs.plugins.ksp)
  alias(libs.plugins.hilt)

  // Google services Gradle plugin
  id("com.google.gms.google-services")

  // Crashlytics Gradle plugin
  id("com.google.firebase.crashlytics")
}

android {
  namespace = "com.openclassrooms.hexagonal.games"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.openclassrooms.hexagonal.games"
    minSdk = 26 // Min Android 8 pour gérer les channels des notifications
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
  composeOptions {
    kotlinCompilerExtensionVersion = "1.5.11"
  }
  kotlinOptions {
    jvmTarget = "1.8"
  }
  buildFeatures {
    compose = true
  }
}

dependencies {
  //kotlin
  implementation(platform(libs.kotlin.bom))

  //DI
  implementation(libs.hilt)
  ksp(libs.hilt.compiler)
  implementation(libs.hilt.navigation.compose)

  //compose
  implementation(platform(libs.compose.bom))
  implementation(libs.compose.ui)
  implementation(libs.compose.ui.graphics)
  implementation(libs.compose.ui.tooling.preview)
  implementation(libs.material)
  implementation(libs.compose.material3)
  implementation(libs.lifecycle.runtime.compose)
  debugImplementation(libs.compose.ui.tooling)
  debugImplementation(libs.compose.ui.test.manifest)

  implementation(libs.activity.compose)
  implementation(libs.navigation.compose)
  
  implementation(libs.kotlinx.coroutines.android)
  
  implementation(libs.coil.compose)
  implementation(libs.accompanist.permissions)

  testImplementation(libs.junit)
  androidTestImplementation(libs.ext.junit)
  androidTestImplementation(libs.espresso.core)
  testImplementation (libs.kotlinx.coroutines.test)
  testImplementation("io.mockk:mockk:1.13.3")

  // Coil = affichage d'URL dans un champ Image (comme Glide)
  implementation(libs.coil.compose)

  // Firebase

  // Import the Firebase BoM
  // garantit que toutes les bibliothèques Firebase utilisées dans un projet sont compatibles entre elles.
  implementation(platform(libs.firebase.bom))

  // When using the BoM, don't specify versions in Firebase dependencies
  implementation(libs.firebase.analytics)

  // Authentification
  implementation(libs.firebase.ui.auth)

  // Firestore = base de données NoSQL
  implementation(libs.firebase.firestore)

  // Firebase Storage (Stockage des images)
  implementation(libs.firebase.storage)

  // Firebase -> système de notification
  implementation(libs.firebase.messaging)

  // Add the dependencies for the Crashlytics and Analytics libraries
  implementation(libs.firebase.crashlytics)
  implementation(libs.google.firebase.analytics)

}