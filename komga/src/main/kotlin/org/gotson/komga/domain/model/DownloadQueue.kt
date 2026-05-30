package org.gotson.komga.domain.model

import java.time.LocalDateTime

data class DownloadQueue(
  val id: String,
  val sourceUrl: String,
  val sourceType: SourceType,
  val title: String?,
  val author: String?,
  val status: DownloadStatus = DownloadStatus.PENDING,
  val progressPercent: Int = 0,
  val currentChapter: Int?,
  val totalChapters: Int?,
  val libraryId: String?,
  val destinationPath: String?,
  val errorMessage: String?,
  val pluginId: String?,
  val metadataJson: String?, // JSON string with additional metadata
  val createdBy: String,
  val startedDate: LocalDateTime?,
  val completedDate: LocalDateTime?,
  val priority: Int = 5, // 1=highest, 10=lowest
  val retryCount: Int = 0,
  val maxRetries: Int = 3,
  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = LocalDateTime.now(),
) : Auditable {
  fun isComplete() = status == DownloadStatus.COMPLETED

  fun isFailed() = status == DownloadStatus.FAILED

  fun isActive() = status == DownloadStatus.DOWNLOADING

  fun canRetry() = retryCount < maxRetries && (status == DownloadStatus.FAILED || status == DownloadStatus.PENDING)
}

enum class SourceType {
  MANGA_SITE, // manga websites (handled by manga-py)
  DIRECT_URL, // direct file URL
  TORRENT, // torrent file
  RSS_FEED, // RSS feed
  API_SOURCE, // custom API source
}

enum class DownloadStatus {
  PENDING,
  DOWNLOADING,
  COMPLETED,
  FAILED,
  PAUSED,
  CANCELLED,
}

// DTOs for download requests
data class DownloadRequest(
  val sourceUrl: String,
  val libraryId: String?,
  val title: String?,
  val author: String?,
  val startChapter: Int? = null,
  val endChapter: Int? = null,
  val priority: Int = 5,
)

data class DownloadProgress(
  val queueId: String,
  val status: DownloadStatus,
  val progressPercent: Int,
  val currentItem: String?,
  val totalItems: Int?,
  val downloadedBytes: Long,
  val totalBytes: Long?,
  val speed: String?, // formatted string like "2.5 MB/s"
  val estimatedTimeRemaining: String?, // formatted string like "5 minutes"
  val errorMessage: String?,
)
