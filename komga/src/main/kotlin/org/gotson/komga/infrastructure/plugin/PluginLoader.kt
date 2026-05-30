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

private val PLUGIN_ALLOWED_PACKAGE_PREFIXES =
  listOf(
    "org.gotson.komga.infrastructure.plugin.api.",
    "com.fasterxml.jackson.",
    "java.",
    "javax.",
    "kotlin.",
    "kotlinx.",
  )

private class SpiOnlyClassLoader(
  parent: ClassLoader,
) : ClassLoader(parent) {
  override fun loadClass(
    name: String,
    resolve: Boolean,
  ): Class<*> {
    if (PLUGIN_ALLOWED_PACKAGE_PREFIXES.none { name.startsWith(it) }) {
      throw ClassNotFoundException(
        "Plugin denied access to '$name' — class-loader isolation. Allowed: SPI ($PLUGIN_ALLOWED_PACKAGE_PREFIXES). Bundle your own libraries into the plugin JAR.",
      )
    }
    return super.loadClass(name, resolve)
  }
}

@Component
class PluginLoader {
  fun load(jarPath: Path): LoadedPlugin {
    val url = jarPath.toUri().toURL()
    val classLoader = URLClassLoader(arrayOf(url), SpiOnlyClassLoader(javaClass.classLoader))
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
