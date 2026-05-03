package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.LogLevel
import org.gotson.komga.domain.model.PluginLog
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

interface PluginLogRepository {
  fun findByPluginId(
    pluginId: String,
    pageable: Pageable,
  ): Page<PluginLog>

  fun findByPluginIdAndLevel(
    pluginId: String,
    logLevel: LogLevel,
    pageable: Pageable,
  ): Page<PluginLog>

  fun insert(log: PluginLog)

  fun deleteByPluginId(pluginId: String)

  fun deleteOlderThan(dateTime: LocalDateTime)
}
