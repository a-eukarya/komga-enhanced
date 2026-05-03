package org.gotson.komga.interfaces.scheduler

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.persistence.HistoricalEventRepository
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId

private val logger = KotlinLogging.logger {}

@Profile("!test")
@Component
class HistoricalEventCleanupController(
  private val historicalEventRepository: HistoricalEventRepository,
) {
  @Scheduled(fixedRate = 86_400_000)
  fun cleanup() {
    val olderThan = LocalDateTime.now(ZoneId.of("Z")).minusDays(30)
    logger.info { "Remove historical events older than $olderThan (UTC)" }
    historicalEventRepository.deleteOlderThan(olderThan)
  }
}
