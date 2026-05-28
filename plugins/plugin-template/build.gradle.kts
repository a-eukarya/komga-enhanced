plugins {
  // Match the Kotlin version Komga is built with.
  kotlin("jvm") version "2.2.21"
}

group = "com.example"
version = "1.0.0"

repositories {
  mavenCentral()
}

kotlin {
  // JDK 21 is required to build Komga, so build the plugin with it too.
  jvmToolchain(21)
  compilerOptions {
    // Komga compiles to bytecode 17 (it just RUNS on JDK 21+). Targeting 17
    // keeps the plugin compatible with that and any newer runtime.
    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
  }
}

dependencies {
  // The Komga plugin SPI is mirrored under
  // src/main/kotlin/org/gotson/komga/infrastructure/plugin/api so this project
  // compiles with no external Komga artifact. Those classes are excluded from
  // the JAR below; Komga supplies them at runtime.
  compileOnly(kotlin("stdlib"))

  // Add YOUR libraries here — these ARE bundled into the plugin JAR and loaded
  // in the plugin's isolated class loader, e.g.:
  // implementation("com.squareup.okhttp3:okhttp:4.12.0")
}

tasks.jar {
  archiveBaseName.set("example-komga-plugin")
  // Never ship the SPI — it must come from Komga's class loader, not yours.
  exclude("org/gotson/komga/infrastructure/plugin/api/**")
}
