package org.gotson.komga.domain.model

class PageHashUnknown(
  hash: String,
  size: Long? = null,
  val matchCount: Int = 0,
  val seriesTitle: String? = null,
) : PageHash(hash, size)
