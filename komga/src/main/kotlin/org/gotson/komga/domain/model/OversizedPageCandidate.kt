package org.gotson.komga.domain.model

data class OversizedPageCandidate(
  val bookId: String,
  val bookName: String,
  val seriesId: String,
  val seriesName: String,
  val seriesTitle: String?,
  val pageNumber: Int,
  val width: Int,
  val height: Int,
  val fileSize: Long,
  val mediaType: String,
)
