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
