<template>
  <v-container fluid class="pa-6">
    <v-row>
      <v-col>
        <div class="mb-4">
          <v-chip label color="primary" class="me-2">Base: {{ currentVersion }}</v-chip>
          <v-chip v-if="forkVersion" label color="secondary">Fork: {{ forkVersion }}</v-chip>
        </div>
      </v-col>
    </v-row>

    <v-tabs v-model="tab" class="mb-4">
      <v-tab>Upstream (Komga)</v-tab>
      <v-tab>
        <v-badge
          dot
          :value="$store.getters.isForkLatestVersion() == 0"
          color="warning"
        >
          Fork
        </v-badge>
      </v-tab>
      <v-tab>
        <v-badge
          dot
          :value="$store.getters.isGalleryDlForkUpToDate() == 0"
          color="warning"
        >
          gallery-dl-fork
        </v-badge>
      </v-tab>
    </v-tabs>

    <v-tabs-items v-model="tab">
      <!-- UPSTREAM TAB -->
      <v-tab-item>
        <div v-if="$store.getters.isLatestVersion() == 1">
          <v-alert type="success" text>
            {{ $t('updates.latest_installed')}}
          </v-alert>
        </div>
        <div v-if="$store.getters.isLatestVersion() == 0">
          <v-alert type="warning" text>
            {{ $t('updates.available')}}
          </v-alert>
        </div>

        <div v-for="(release, index) in $store.state.releases" :key="'up-'+index">
          <v-row justify="space-between" align="center">
            <v-col cols="auto">
              <div>
                <a :href="release.url" target="_blank" class="text-h4 font-weight-medium link-underline me-2">{{
                    release.version
                  }}</a>
                <v-chip
                  v-if="release.version.replace(/^v/, '') == currentVersion.replace(/^v/, '')"
                  class="mx-2 mt-n3"
                  small
                  label
                  color="info"
                >Currently installed</v-chip>
                <v-chip
                  v-if="release.version == latestUpstream?.version"
                  class="mx-2 mt-n3"
                  small
                  label
                >Latest</v-chip>
              </div>
              <div class="mt-2 subtitle-1">
                {{ new Intl.DateTimeFormat($i18n.locale, {dateStyle: 'long'}).format(release.releaseDate) }}
              </div>
            </v-col>
          </v-row>

          <v-row>
            <v-col cols="12">
              <div v-html="marked(release.description)"></div>
            </v-col>
          </v-row>
          <v-divider class="my-8" v-if="index != $store.state.releases.length - 1"/>
        </div>
      </v-tab-item>

      <!-- FORK TAB -->
      <v-tab-item>
        <div v-if="$store.getters.isForkLatestVersion() == 1">
          <v-alert type="success" text>
            Fork is up to date
          </v-alert>
        </div>
        <div v-if="$store.getters.isForkLatestVersion() == 0">
          <v-alert type="warning" text>
            Fork update available
          </v-alert>
        </div>
        <div v-if="$store.state.forkReleases.length == 0">
          <v-alert type="info" text>
            No fork releases found
          </v-alert>
        </div>

        <div v-for="(release, index) in $store.state.forkReleases" :key="'fork-'+index">
          <v-row justify="space-between" align="center">
            <v-col cols="auto">
              <div>
                <a :href="release.url" target="_blank" class="text-h4 font-weight-medium link-underline me-2">{{
                    release.version
                  }}</a>
                <v-chip
                  v-if="isCurrentForkRelease(release.version)"
                  class="mx-2 mt-n3"
                  small
                  label
                  color="info"
                >Installiert</v-chip>
                <v-chip
                  v-if="release.version == latestFork?.version"
                  class="mx-2 mt-n3"
                  small
                  label
                >Latest</v-chip>
              </div>
              <div class="mt-2 subtitle-1">
                {{ new Intl.DateTimeFormat($i18n.locale, {dateStyle: 'long'}).format(release.releaseDate) }}
              </div>
            </v-col>
          </v-row>

          <v-row>
            <v-col cols="12">
              <div v-html="marked(release.description)"></div>
            </v-col>
          </v-row>
          <v-divider class="my-8" v-if="index != $store.state.forkReleases.length - 1"/>
        </div>
      </v-tab-item>

      <!-- GALLERY-DL-FORK TAB -->
      <v-tab-item>
        <div v-if="galleryDlForkUpdates === null">
          <v-alert type="info" text>
            Loading gallery-dl-fork update info…
          </v-alert>
        </div>
        <template v-else>
          <div class="mb-4">
            <v-chip label class="me-2">
              Installed SHA:
              {{ galleryDlForkUpdates.installedSha ? galleryDlForkUpdates.installedSha.substring(0, 7) : 'unknown' }}
            </v-chip>
            <v-chip
              v-if="galleryDlForkUpdates.behindCount === 0"
              label
              color="success"
              text-color="white"
            >Up to date</v-chip>
            <v-chip
              v-else-if="galleryDlForkUpdates.behindCount > 0"
              label
              color="warning"
              text-color="white"
            >{{ galleryDlForkUpdates.behindCount }} commit{{ galleryDlForkUpdates.behindCount === 1 ? '' : 's' }} behind</v-chip>
            <v-chip
              v-else
              label
              color="grey"
              text-color="white"
            >Installed SHA unknown</v-chip>
          </div>

          <v-alert
            v-if="galleryDlForkUpdates.behindCount > 0"
            type="warning"
            text
          >
            <div class="mb-2">
              New commits available on
              <a href="https://github.com/08shiro80/gallery-dl-komga" target="_blank">08shiro80/gallery-dl-komga</a>.
              Komga runs as a non-root user, so the in-container python site-packages aren't
              writable from the running process. Run this on the docker host to update
              without a full rebuild:
            </div>
            <pre class="pa-2 update-cmd">{{ updateCommand }}</pre>
            <v-btn small @click="copyUpdateCommand">{{ copied ? 'Copied!' : 'Copy command' }}</v-btn>
          </v-alert>

          <v-list two-line>
            <v-list-item
              v-for="commit in galleryDlForkUpdates.commits"
              :key="commit.sha"
              :href="commit.url"
              target="_blank"
            >
              <v-list-item-content>
                <v-list-item-title>
                  <code>{{ commit.shortSha }}</code> — {{ commit.message }}
                  <v-chip
                    v-if="commit.installed"
                    class="mx-2"
                    x-small
                    label
                    color="info"
                  >Installed</v-chip>
                </v-list-item-title>
                <v-list-item-subtitle>
                  {{ commit.author || 'unknown' }} ·
                  {{ new Intl.DateTimeFormat($i18n.locale, {dateStyle: 'medium', timeStyle: 'short'}).format(new Date(commit.date)) }}
                </v-list-item-subtitle>
              </v-list-item-content>
            </v-list-item>
          </v-list>
        </template>
      </v-tab-item>
    </v-tabs-items>
  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'
import {marked} from 'marked'

export default Vue.extend({
  name: 'UpdatesView',
  data: () => ({
    marked,
    tab: 0,
    copied: false,
  }),
  computed: {
    latestUpstream(): ReleaseDto | undefined {
      return this.$store.state.releases.find((x: ReleaseDto) => x.latest)
    },
    latestFork(): ReleaseDto | undefined {
      return this.$store.state.forkReleases.find((x: ReleaseDto) => x.latest)
    },
    galleryDlForkUpdates(): GalleryDlForkUpdateDto | null {
      return this.$store.state.galleryDlForkUpdates
    },
    updateCommand(): string {
      const sha = this.galleryDlForkUpdates?.commits?.[0]?.sha || 'master'
      return `docker exec -u 0 komga sh -c "pip3 install --break-system-packages --no-cache-dir --force-reinstall https://github.com/08shiro80/gallery-dl-komga/archive/${sha}.tar.gz && echo ${sha} > /opt/gallery-dl-fork-sha"`
    },
    currentVersion(): string {
      const fullVersion = this.$store.state.actuatorInfo?.build?.version || ''
      return fullVersion.split('-fork')[0]
    },
    forkVersion(): string {
      const fullVersion = this.$store.state.actuatorInfo?.build?.version || ''
      const parts = fullVersion.split('-fork-')
      return parts.length > 1 ? parts[1] : ''
    },
    fullVersion(): string {
      return this.$store.state.actuatorInfo?.build?.version || ''
    },
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.$actuator.getInfo()
        .then(x => this.$store.commit('setActuatorInfo', x))
      this.$komgaReleases.getReleases()
        .then(x => this.$store.commit('setReleases', x))
      this.$komgaReleases.getForkReleases()
        .then(x => this.$store.commit('setForkReleases', x))
        .catch(() => {})
      this.$komgaReleases.getGalleryDlForkUpdates()
        .then(x => this.$store.commit('setGalleryDlForkUpdates', x))
        .catch(() => this.$store.commit('setGalleryDlForkUpdates', {installedSha: null, behindCount: -1, commits: []}))
    },
    isCurrentForkRelease(tagName: string): boolean {
      const tag = tagName.replace(/^v/, '')
      const full = this.fullVersion.replace(/^v/, '')
      if (tag === full) return true
      const tagFork = tag.split('-fork-')[1] || ''
      return tagFork !== '' && tagFork === this.forkVersion
    },
    copyUpdateCommand() {
      navigator.clipboard.writeText(this.updateCommand).then(() => {
        this.copied = true
        setTimeout(() => { this.copied = false }, 2000)
      })
    },
  },
})
</script>

<style scoped>
.update-cmd {
  background: rgba(0, 0, 0, 0.06);
  border-radius: 4px;
  font-size: 0.85em;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
