// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.androidApplication) apply false
  alias(libs.plugins.hilt) apply false
  alias(libs.plugins.kotlin) apply false
  alias(libs.plugins.ksp) apply false

  // dependency for the Google services Gradle plugin
  id("com.google.gms.google-services") version "4.4.2" apply false

  // dependency for the Crashlytics Gradle plugin
  id("com.google.firebase.crashlytics") version "3.0.2" apply false
}