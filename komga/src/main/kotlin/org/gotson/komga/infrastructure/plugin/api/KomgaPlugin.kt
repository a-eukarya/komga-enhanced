package org.gotson.komga.infrastructure.plugin.api

/**
 * Service Provider Interface for external Komga plugins.
 *
 * An external plugin is a JAR that:
 *  - depends on the Komga JAR with `compileOnly` (so this SPI is on its classpath),
 *  - implements [KomgaPlugin] (or a sub-interface such as [MetadataProviderPlugin]),
 *  - declares the implementation in
 *    `META-INF/services/org.gotson.komga.infrastructure.plugin.api.KomgaPlugin`
 *    so it is discoverable via [java.util.ServiceLoader].
 *
 * The JAR is dropped into `${komga.config-dir}/plugins/` (or uploaded via the
 * Plugin Manager) and loaded into an isolated class loader at runtime.
 */
interface KomgaPlugin {
  val id: String

  val name: String

  val version: String

  val author: String?
    get() = null

  val description: String?
    get() = null

  /** Optional JSON Schema describing this plugin's configuration fields (rendered in the Plugin Manager). */
  val configSchema: String?
    get() = null

  val type: KomgaPluginType

  /** Called once after the plugin is loaded, before it is used. */
  fun initialize(context: PluginContext) {
  }

  /** Called when the plugin is uninstalled or the server shuts down. */
  fun shutdown() {
  }
}

enum class KomgaPluginType {
  METADATA,
  NOTIFIER,
  ANALYZER,
  PROCESSOR,
}

/** A plugin that can search an online source and return series metadata. */
interface MetadataProviderPlugin : KomgaPlugin {
  override val type: KomgaPluginType
    get() = KomgaPluginType.METADATA

  fun search(query: String): List<PluginSearchResult>

  fun getMetadata(externalId: String): PluginMetadataDetails?
}

/**
 * A plugin that reacts to Komga events (e.g. a download finished or failed).
 * Notifications are delivered only while the plugin is enabled. Implementations
 * should return quickly and must not throw — failures are logged and ignored.
 */
interface NotifierPlugin : KomgaPlugin {
  override val type: KomgaPluginType
    get() = KomgaPluginType.NOTIFIER

  fun onNotification(notification: PluginNotification)
}

data class PluginNotification(
  /** Stable event key, e.g. "DOWNLOAD_COMPLETED" or "DOWNLOAD_FAILED". */
  val type: String,
  val title: String,
  val message: String,
  val data: Map<String, String> = emptyMap(),
)

/** Runtime services handed to a plugin on [KomgaPlugin.initialize]. */
interface PluginContext {
  /** Configuration values stored for this plugin (key -> value). */
  val config: Map<String, String>

  fun info(message: String)

  fun warn(message: String)

  fun error(
    message: String,
    throwable: Throwable? = null,
  )
}

data class PluginSearchResult(
  val externalId: String,
  val title: String,
  val description: String? = null,
  val coverUrl: String? = null,
  val author: String? = null,
  val year: Int? = null,
  val status: String? = null,
  val tags: List<String> = emptyList(),
)

data class PluginMetadataDetails(
  val title: String,
  val titleSort: String? = null,
  val summary: String? = null,
  val publisher: String? = null,
  val ageRating: Int? = null,
  val releaseDate: String? = null,
  val authors: List<PluginAuthor> = emptyList(),
  val tags: List<String> = emptyList(),
  val genres: List<String> = emptyList(),
  val language: String? = null,
  val status: String? = null,
  val coverUrl: String? = null,
  val alternativeTitles: Map<String, String> = emptyMap(),
)

data class PluginAuthor(
  val name: String,
  val role: String,
)
