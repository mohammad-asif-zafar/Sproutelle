import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.provider.Property
import org.gradle.api.file.DirectoryProperty
import java.util.Properties

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

val supabaseUrlVal = localProperties.getProperty("supabase.url")
    ?: project.findProperty("supabase.url") as? String
    ?: System.getenv("SUPABASE_URL")
    ?: ""

val supabaseAnonKeyVal = localProperties.getProperty("supabase.anonKey")
    ?: project.findProperty("supabase.anonKey") as? String
    ?: System.getenv("SUPABASE_ANON_KEY")
    ?: ""

if (supabaseUrlVal.isBlank() || supabaseAnonKeyVal.isBlank()) {
    throw GradleException(
        "Supabase configuration is missing! Please configure 'supabase.url' and 'supabase.anonKey' in local.properties (copy from local.properties.example), or via project properties/environment variables."
    )
}

abstract class GenerateSupabaseConfigTask : DefaultTask() {
    @get:Input
    abstract val supabaseUrl: Property<String>

    @get:Input
    abstract val supabaseAnonKey: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    private fun String.toKotlinLiteral(): String {
        return "\"" + this
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("$", "\\$")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t") + "\""
    }

    @TaskAction
    fun generate() {
        val dir = outputDir.get().asFile
        dir.mkdirs()
        val file = File(dir, "SupabaseConfig.kt")
        val urlLiteral = supabaseUrl.get().toKotlinLiteral()
        val keyLiteral = supabaseAnonKey.get().toKotlinLiteral()
        file.writeText("""
            package com.hathway.sproutelle.data.datasource

            object SupabaseConfig {
                // Generated at build time from local.properties / environment
                const val SUPABASE_URL = $urlLiteral
                const val SUPABASE_ANON_KEY = $keyLiteral
            }
        """.trimIndent())
    }
}

val generateSupabaseConfig = tasks.register<GenerateSupabaseConfigTask>("generateSupabaseConfig") {
    supabaseUrl.set(supabaseUrlVal)
    supabaseAnonKey.set(supabaseAnonKeyVal)
    outputDir.set(layout.buildDirectory.dir("generated/supabase/com/hathway/sproutelle/data/datasource"))
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    android {
       namespace = "com.hathway.sproutelle.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }
    
    sourceSets {
        named("commonMain") {
            kotlin.srcDir(generateSupabaseConfig.map { it.outputDir.get().asFile })
        }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.ktor.client.cio)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
