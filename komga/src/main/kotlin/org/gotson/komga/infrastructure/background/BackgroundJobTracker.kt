package org.gotson.komga.infrastructure.background

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class BackgroundJobTracker {
  private val active = ConcurrentHashMap<String, Boolean>()

  fun start(jobName: String) {
    active[jobName] = true
  }

  fun stop(jobName: String) {
    active.remove(jobName)
  }

  fun snapshot(): Map<String, Boolean> = active.toMap()
}
