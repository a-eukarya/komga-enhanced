package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.HistoricalEvent
import java.time.LocalDateTime

interface HistoricalEventRepository {
  fun insert(event: HistoricalEvent)

  fun deleteOlderThan(dateTime: LocalDateTime)
}
