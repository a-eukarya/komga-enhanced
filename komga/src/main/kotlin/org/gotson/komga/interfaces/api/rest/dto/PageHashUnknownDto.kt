package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.PageHashUnknown

data class PageHashUnknownDto(
  val hash: String,
  val size: Long?,
  val matchCount: Int,
  val seriesTitle: String?,
)

fun PageHashUnknown.toDto() =
  PageHashUnknownDto(
    hash = hash,
    size = size,
    matchCount = matchCount,
    seriesTitle = seriesTitle,
  )
