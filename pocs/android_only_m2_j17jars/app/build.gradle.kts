plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.protobuf")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.4"
    }

   /* generateProtoTasks {
        all().forEach { task ->
            // Set the source of the .proto files to the "src/main/proto" directory
            //task.source("src/main/proto")

            // Use standard Java classes (not lite) for Protobuf generation
            task.builtins {
                java {
                   // outputSubDir = "java"  // Specify output directory for Java files
                }  // Generates standard Protobuf Java classes
            }
        }
    }*/

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                // Generate standard (non-lite) Java classes
                create("java")
            }
        }
    }
}

android {
    namespace = "bisq.mobile"
    compileSdk = 35

    defaultConfig {
        applicationId = "bisq.mobile"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }


  /*  sourceSets {
        getByName("main") {
            java.srcDir("src/main/proto")  // Specify the directory for proto files
            java.srcDir("build/generated/source/proto/main/java")  // Specify the Java output directory
        }
    }*/

    sourceSets {
        getByName("main") {
            java.srcDir("src/main/proto")  // Specify the directory for proto files
        }
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    implementation("bisq:common:2.1.1")
    implementation(libs.protobuf.java)
    implementation(libs.protobuf.gradle.plugin)
    implementation("com.google.protobuf:protoc:3.25.4")
}

