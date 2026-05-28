# Writing Komga (fork) plugins

This fork can load **external plugin JARs** at runtime, in addition to the
plugins compiled into Komga. You write a small Kotlin project, run
`./gradlew build`, and drop the resulting JAR into Komga.

There are two kinds of plugin today:

| Type | Interface | What it does |
|------|-----------|--------------|
| `METADATA` | `MetadataProviderPlugin` | Search an online source and return series metadata. Shows up in "Search Online Databases". |
| `NOTIFIER` | `NotifierPlugin` | React to events (`DOWNLOAD_COMPLETED`, `DOWNLOAD_FAILED`) — webhook, Discord, log, … |

> `ANALYZER` / `PROCESSOR` interfaces exist in the SPI but have no internal
> call-sites yet. `DOWNLOAD` is intentionally not pluggable (it is tied to gallery-dl).

---

## Quick start (the easy path)

A ready-to-build template lives in [`plugins/plugin-template/`](plugins/plugin-template). It
compiles standalone — you do **not** need the Komga JAR on your classpath.

```bash
cp -r plugins/plugin-template my-plugin
cd my-plugin
# 1. edit the code in src/main/kotlin/com/example/myplugin/
# 2. keep src/main/resources/META-INF/services/...KomgaPlugin in sync
./gradlew build
# -> build/libs/example-komga-plugin-1.0.0.jar
```

Then install it (either way works):

- **UI:** Plugin Manager → *Install Plugin* → pick the `.jar` (or paste a URL).
- **Filesystem:** copy the JAR into `<komga-config-dir>/plugins/` and restart.
  (`<komga-config-dir>` is your `KOMGA_CONFIGDIR`, e.g. `~/.komga` or the mounted
  `/config` in Docker.)

Uninstall from the Plugin Manager — it unloads the class loader and deletes the JAR.

> Plugins run as **admin-only** installs and execute arbitrary code inside the
> Komga JVM. There is no sandbox. Only install plugins you trust.

---

## How loading works

1. On startup (and on install) Komga scans `<config-dir>/plugins/*.jar`.
2. Each JAR gets its **own** `URLClassLoader` whose parent is Komga's loader, so
   it sees the SPI + Kotlin stdlib but stays isolated from other plugins.
3. Komga discovers your implementation via Java's `ServiceLoader`, which reads
   `META-INF/services/org.gotson.komga.infrastructure.plugin.api.KomgaPlugin`.
   **Every plugin class you ship must be listed there**, one fully-qualified
   name per line.
4. The plugin is registered in the `plugin` table (your `enabled` toggle
   survives restarts) and `initialize(context)` is called.

Because the SPI classes come from Komga's parent class loader, your JAR must
**not** contain its own copy of them — the template's `build.gradle.kts`
excludes the `org.gotson.komga.infrastructure.plugin.api` package for that reason.

---

## The SPI

```kotlin
interface KomgaPlugin {
  val id: String              // unique, stable, e.g. "anidb-metadata"
  val name: String
  val version: String
  val author: String?         // optional
  val description: String?    // optional
  val type: KomgaPluginType   // set by the sub-interface you implement

  fun initialize(context: PluginContext) {}
  fun shutdown() {}
}

interface MetadataProviderPlugin : KomgaPlugin {
  fun search(query: String): List<PluginSearchResult>
  fun getMetadata(externalId: String): PluginMetadataDetails?
}

interface NotifierPlugin : KomgaPlugin {
  fun onNotification(notification: PluginNotification)   // type/title/message/data
}

interface PluginContext {
  val config: Map<String, String>        // values stored for this plugin
  fun info(message: String)
  fun warn(message: String)
  fun error(message: String, throwable: Throwable? = null)
}
```

DTOs (`PluginSearchResult`, `PluginMetadataDetails`, `PluginAuthor`,
`PluginNotification`) are plain data classes — see the mirror in the template or
the source at
`komga/src/main/kotlin/org/gotson/komga/infrastructure/plugin/api/KomgaPlugin.kt`.

### Logging & config

Use the `PluginContext` you receive in `initialize` for logging (it writes to the
plugin's log, visible in the Plugin Manager) and to read configuration values.

---

## Bundling dependencies

Anything you add as `implementation(...)` in `build.gradle.kts` is packaged into
your JAR and available at runtime. The Kotlin stdlib is **not** bundled (Komga
provides it) — keep using the same Kotlin version as Komga (`2.2.x`) to avoid
class-version surprises.

If you pull in large libraries, prefer a "fat JAR" (e.g. the Shadow plugin) so
all transitive dependencies end up inside the single JAR you install — but still
exclude the `...plugin.api` package.

---

## Checklist

- [ ] Unique `id`, sensible `name`/`version`.
- [ ] Implement `MetadataProviderPlugin` and/or `NotifierPlugin`.
- [ ] Each class listed in `META-INF/services/org.gotson.komga.infrastructure.plugin.api.KomgaPlugin`.
- [ ] `./gradlew build` produces a JAR under `build/libs/`.
- [ ] Install via the Plugin Manager or `<config-dir>/plugins/`.
