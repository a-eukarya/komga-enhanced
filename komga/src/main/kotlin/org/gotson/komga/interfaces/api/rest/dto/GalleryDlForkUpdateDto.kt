package org.gotson.komga.interfaces.api.rest.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

data class GalleryDlForkUpdateDto(
  val installedSha: String?,
  val behindCount: Int,
  val commits: List<GalleryDlForkCommitDto>,
)

data class GalleryDlForkCommitDto(
  val sha: String,
  val shortSha: String,
  val message: String,
  val author: String?,
  val date: ZonedDateTime,
  val url: String,
  val installed: Boolean,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GithubCommitDto(
  val sha: String,
  val commit: GithubCommitDetails,
  @JsonProperty("html_url")
  val htmlUrl: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GithubCommitDetails(
  val message: String,
  val author: GithubCommitAuthor?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GithubCommitAuthor(
  val name: String?,
  val date: ZonedDateTime,
)
