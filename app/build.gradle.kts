plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.quizapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.quizapp"
        minSdk = 26
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
    packagingOptions {
        exclude("org/apache/commons/collections4/EWAHCompressedBitmap.class")
        exclude("META-INF/services/org.apache.commons.collections4.properties")
    }
}

// Log4j 2.18.0+ uses MethodHandle, which requires Android O (API 26).
// Now that minSdk is 26, we can use newer versions, but forcing 2.17.2 
// ensures stability if no specific features from later versions are needed.
configurations.all {
    resolutionStrategy {
        force("org.apache.logging.log4j:log4j-api:2.17.2")
        force("org.apache.logging.log4j:log4j-core:2.17.2")
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // Google Sign In
    implementation(libs.play.services.auth)

    // For parsing .docx files
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
