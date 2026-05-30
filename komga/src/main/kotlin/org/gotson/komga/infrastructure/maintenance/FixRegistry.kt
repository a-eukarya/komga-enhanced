package org.gotson.komga.infrastructure.maintenance

import org.gotson.komga.domain.persistence.PluginRepository
import org.springframework.stereotype.Component

@Component
class FixRegistry(
  private val pluginRepository: PluginRepository,
) {
  data class Fix(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val endpoint: String,
    val method: String,
    val params: List<Param>,
  )

  data class Param(
    val key: String,
    val label: String,
    val type: String,
    val default: Any?,
    val min: Int?,
    val max: Int?,
    val hint: String?,
  )

  private data class Registration(
    val fix: Fix,
    val isEnabled: () -> Boolean,
  )

  private val registrations: List<Registration> =
    listOf(
      Registration(
        fix =
          Fix(
            id = "mangadex-force-resync",
            title = "MangaDex Subscription — Force Resync",
            description = "Rewinds last_check_time and re-runs the followed-manga feed check now. Use when a manga's chapters were silently skipped (e.g. an old COMPLETED queue row blocked them) or after a long restart gap. isChapterKnown still blocks duplicates.",
            icon = "mdi-wrench-outline",
            endpoint = "/api/v1/downloads/mangadex-subscription/force-resync",
            method = "POST",
            params =
              listOf(
                Param(
                  key = "lookbackDays",
                  label = "Lookback window (days)",
                  type = "number",
                  default = 7,
                  min = 1,
                  max = 30,
                  hint = "How far back to scan the followed-manga feed",
                ),
              ),
          ),
        isEnabled = { pluginRepository.findByIdOrNull("mangadex-subscription")?.enabled == true },
      ),
    )

  fun findAll(): List<Fix> =
    registrations
      .filter { it.isEnabled() }
      .map { it.fix }
}
