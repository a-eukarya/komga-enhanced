package com.example.myplugin

import org.gotson.komga.infrastructure.plugin.api.MetadataProviderPlugin
import org.gotson.komga.infrastructure.plugin.api.PluginContext
import org.gotson.komga.infrastructure.plugin.api.PluginMetadataDetails
import org.gotson.komga.infrastructure.plugin.api.PluginSearchResult

/**
 * Minimal metadata provider. Rename the class, set a unique [id], and replace
 * the bodies of [search] and [getMetadata] with calls to your own source.
 *
 * Remember to keep src/main/resources/META-INF/services/...KomgaPlugin in sync
 * with the class name.
 */
class ExampleMetadataPlugin : MetadataProviderPlugin {
  override val id = "example-metadata"
  override val name = "Example Metadata Provider"
  override val version = "1.0.0"
  override val author = "Your Name"
  override val description = "Template metadata plugin — replace search()/getMetadata() with real logic."

  private var config: Map<String, String> = emptyMap()

  override fun initialize(context: PluginContext) {
    config = context.config
    context.info("Example metadata plugin initialized")
  }

  override fun search(query: String): List<PluginSearchResult> =
    listOf(
      PluginSearchResult(
        externalId = "example:$query",
        title = "Example result for \"$query\"",
        description = "Replace this with a real search against your source.",
      ),
    )

  override fun getMetadata(externalId: String): PluginMetadataDetails? =
    PluginMetadataDetails(
      title = "Example title ($externalId)",
      summary = "Replace this with real metadata for $externalId.",
      genres = listOf("Example"),
    )
}
