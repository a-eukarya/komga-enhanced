package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.HistoricalEvent
import org.gotson.komga.domain.persistence.HistoricalEventRepository
import org.gotson.komga.jooq.main.Tables
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class HistoricalEventDao(
  private val dslRW: DSLContext,
) : HistoricalEventRepository {
  private val e = Tables.HISTORICAL_EVENT
  private val ep = Tables.HISTORICAL_EVENT_PROPERTIES

  @Transactional
  override fun insert(event: HistoricalEvent) {
    dslRW
      .insertInto(e)
      .set(e.ID, event.id)
      .set(e.TYPE, event.type)
      .set(e.BOOK_ID, event.bookId)
      .set(e.SERIES_ID, event.seriesId)
      .set(e.TIMESTAMP, event.timestamp)
      .execute()

    if (event.properties.isNotEmpty()) {
      dslRW
        .batch(
          dslRW
            .insertInto(ep, ep.ID, ep.KEY, ep.VALUE)
            .values(null as String?, null, null),
        ).also { step ->
          event.properties.forEach { (key, value) ->
            step.bind(event.id, key, value)
          }
        }.execute()
    }
  }

  @Transactional
  override fun deleteOlderThan(dateTime: LocalDateTime) {
    dslRW
      .deleteFrom(ep)
      .where(ep.ID.`in`(dslRW.select(e.ID).from(e).where(e.TIMESTAMP.lt(dateTime))))
      .execute()
    dslRW
      .deleteFrom(e)
      .where(e.TIMESTAMP.lt(dateTime))
      .execute()
  }
}
