package org.gotson.komga.infrastructure.plugin.api

/*
 * ─────────────────────────────────────────────────────────────────────────────
 *  DO NOT EDIT — and DO NOT ship this file inside your plugin JAR.
 *
 *  This is a local mirror of Komga's plugin SPI so the template compiles
 *  standalone (no Komga JAR on the classpath needed). The build excludes this
 *  package from the produced JAR (see build.gradle.kts -> tasks.jar.exclude);
 *  at runtime Komga provides these classes from its own class loader.
 *
 *  Keep it identical to the SPI of the Komga version you target.
 * ─────────────────────────────────────────────────────────────────────────────
 */

interface KomgaPlugin {
  val id: String

  val name: String

  val version: String

  val author: String?
    get() = null

  val description: String?
    get() = null

  val configSchema: String?
    get() = null

  val type: KomgaPluginType

  fun initialize(context: PluginContext) {
  }

  fun shutdown() {
  }
}

enum class KomgaPluginType {
  METADATA,
  NOTIFIER,
  ANALYZER,
  PROCESSOR,
}

interface MetadataProviderPlugin : KomgaPlugin {
  override val type: KomgaPluginType
    get() = KomgaPluginType.METADATA

  fun search(query: String): List<PluginSearchResult>

  fun getMetadata(externalId: String): PluginMetadataDetails?
}

interface NotifierPlugin : KomgaPlugin {
  override val type: KomgaPluginType
    get() = KomgaPluginType.NOTIFIER

  fun onNotification(notification: PluginNotification)
}

interface PluginContext {
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

data class PluginNotification(
  val type: String,
  val title: String,
  val message: String,
  val data: Map<String, String> = emptyMap(),
)
