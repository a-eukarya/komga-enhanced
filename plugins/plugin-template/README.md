# example-komga-plugin

A ready-to-build template for a Komga (fork) plugin. See the full guide in
[`../../PLUGINS.md`](../../PLUGINS.md).

## Build

```bash
./gradlew build
# -> build/libs/example-komga-plugin-1.0.0.jar
```

Requires JDK 21 (same as building Komga).

## What's inside

- `src/main/kotlin/com/example/myplugin/ExampleMetadataPlugin.kt` — a `METADATA` plugin (edit this).
- `src/main/kotlin/com/example/myplugin/ExampleNotifierPlugin.kt` — a `NOTIFIER` plugin (edit or delete).
- `src/main/resources/META-INF/services/org.gotson.komga.infrastructure.plugin.api.KomgaPlugin`
  — lists the plugin classes for `ServiceLoader`. Keep it in sync with your classes.
- `src/main/kotlin/org/gotson/komga/infrastructure/plugin/api/KomgaPlugin.kt`
  — a **local mirror** of Komga's SPI so this builds standalone. Do not edit; it
  is excluded from the produced JAR.

## Install

- **UI:** Plugin Manager → *Install Plugin* → choose the `.jar`.
- **Filesystem:** copy the `.jar` into `<komga-config-dir>/plugins/` and restart.

## Class-loader isolation

Komga loads each plugin under a `SpiOnlyClassLoader` that whitelists only:

- `org.gotson.komga.infrastructure.plugin.api.*` (the SPI itself)
- `java.*`, `javax.*` (JDK)
- `kotlin.*`, `kotlinx.*` (Kotlin runtime)
- `com.fasterxml.jackson.*` (JSON)

Trying to `import` anything else from Komga (`org.gotson.komga.domain.…`,
Spring, SLF4J, …) fails at runtime with `ClassNotFoundException("Plugin
denied access to '…' — class-loader isolation. …")`. Anything you genuinely
need — OkHttp, SQLite-JDBC, your own libraries — must be `implementation(...)`
in `build.gradle.kts` so it ends up inside your plugin JAR; the plugin's
private `URLClassLoader` serves them after the parent rejects.

This is **isolation, not a sandbox**: `java.io.File`, `java.net.http` and
`java.lang.reflect` are intentionally allowed. See `PLUGINS.md` →
*Security model* in the fork root for the full statement of limits.
