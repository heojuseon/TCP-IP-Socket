// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false

    //Navigation 을 사용하여 데이터 전달시 추가
    id("androidx.navigation.safeargs.kotlin") version "2.7.7" apply false
}