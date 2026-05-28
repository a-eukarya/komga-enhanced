package org.gotson.komga.infrastructure.plugin

import org.gotson.komga.infrastructure.plugin.api.KomgaPlugin
import org.springframework.stereotype.Component
import java.net.URLClassLoader
import java.nio.file.Path
import java.util.ServiceLoader

class PluginLoadException(
  message: String,
  cause: Throwable? = null,
) : RuntimeException(message, cause)

class LoadedPlugin(
  val instance: KomgaPlugin,
  val classLoader: URLClassLoader,
  val jarPath: Path,
)

@Component
class PluginLoader {
  /**
   * Loads a single plugin JAR into an isolated class loader whose parent is
   * Komga's own class loader (so the plugin sees the SPI and Kotlin stdlib).
   * The implementation is discovered via [ServiceLoader].
   */
  fun load(jarPath: Path): LoadedPlugin {
    val url = jarPath.toUri().toURL()
    val classLoader = URLClassLoader(arrayOf(url), javaClass.classLoader)
    try {
      val iterator = ServiceLoader.load(KomgaPlugin::class.java, classLoader).iterator()
      if (!iterator.hasNext()) {
        throw PluginLoadException(
          "No KomgaPlugin declared in ${jarPath.fileName}. " +
            "Add META-INF/services/org.gotson.komga.infrastructure.plugin.api.KomgaPlugin listing your implementation class.",
        )
      }
      val instance = iterator.next()
      return LoadedPlugin(instance, classLoader, jarPath)
    } catch (e: Throwable) {
      classLoader.close()
      throw if (e is PluginLoadException) e else PluginLoadException("Failed to load plugin from ${jarPath.fileName}: ${e.message}", e)
    }
  }
}
