import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  kotlin("jvm")
}

group = "org.gotson"

dependencies {
  // Provided by Komga at runtime (parent class loader) — not bundled.
  compileOnly("com.fasterxml.jackson.core:jackson-databind:2.21.1")
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.JVM_17
  }
}

// keep compileJava aligned with Kotlin's bytecode target (matches komga-tray)
tasks.withType<JavaCompile> {
  sourceCompatibility = "17"
  targetCompatibility = "17"
}

tasks.jar {
  archiveFileName.set("metron-metadata.jar")
  // The SPI is mirrored locally only so this compiles; Komga provides it at runtime.
  exclude("org/gotson/komga/infrastructure/plugin/api/**")
}
