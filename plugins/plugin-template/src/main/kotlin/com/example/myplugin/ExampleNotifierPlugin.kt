package com.example.myplugin

import org.gotson.komga.infrastructure.plugin.api.NotifierPlugin
import org.gotson.komga.infrastructure.plugin.api.PluginContext
import org.gotson.komga.infrastructure.plugin.api.PluginNotification

/**
 * Minimal notifier. Receives events such as "DOWNLOAD_COMPLETED" and
 * "DOWNLOAD_FAILED" while the plugin is enabled. Replace [onNotification] with a
 * real action (webhook, Discord, file, e-mail, ...). Keep it fast and do not throw.
 */
class ExampleNotifierPlugin : NotifierPlugin {
  override val id = "example-notifier"
  override val name = "Example Notifier"
  override val version = "1.0.0"
  override val author = "Your Name"
  override val description = "Template notifier — logs download events. Replace with a real action."

  private var context: PluginContext? = null

  override fun initialize(context: PluginContext) {
    this.context = context
  }

  override fun onNotification(notification: PluginNotification) {
    context?.info("[${notification.type}] ${notification.title}: ${notification.message}")
  }
}
