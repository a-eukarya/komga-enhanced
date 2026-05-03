<template>
  <div>
    <v-container fluid>
      <v-row>
        <v-col>
          <h1 class="text-h4 mb-1">
            <v-icon large class="mr-2">mdi-wrench-outline</v-icon>
            Fixes
          </h1>
          <p class="text-subtitle-2 text--secondary mb-4">
            One-time maintenance actions. Fixes are removed once they are no longer needed.
          </p>
        </v-col>
      </v-row>

      <!-- ── FIX: Re-inject ComicInfo.xml (genre/tag restore) ─────────────── -->
      <v-row>
        <v-col cols="12" md="7">
          <v-card outlined>
            <v-card-title class="subtitle-1 font-weight-bold">
              <v-icon left color="warning">mdi-file-document-refresh-outline</v-icon>
              Re-inject ComicInfo.xml
            </v-card-title>

            <v-card-text>
              <p class="body-2 mb-3">
                Re-generates <code>ComicInfo.xml</code> and <code>series.json</code> for every CBZ
                in the selected library from MangaDex metadata. Enable Force to overwrite existing
                ComicInfo.xml — useful to restore <code>&lt;Genre&gt;</code> for files written
                while the genre/tag split was active (0.1.4.3).
              </p>

              <v-select
                v-model="fixComicInfo.libraryId"
                :items="libraries"
                item-text="name"
                item-value="id"
                label="Library"
                outlined
                dense
                hide-details
                class="mb-3"
              />

              <v-checkbox
                v-model="fixComicInfo.force"
                label="Force (re-inject even if ComicInfo already exists)"
                hide-details
                class="mt-0 mb-1"
                dense
              />
            </v-card-text>

            <v-card-actions class="pt-0">
              <v-btn
                color="warning"
                :loading="fixComicInfo.running"
                :disabled="!fixComicInfo.libraryId"
                @click="runComicInfoFix"
              >
                <v-icon left>mdi-play</v-icon>
                Run
              </v-btn>
            </v-card-actions>

            <v-expand-transition>
              <v-card-text v-if="fixComicInfo.result" class="pt-0">
                <v-alert
                  :type="fixComicInfo.result.errors && fixComicInfo.result.errors.length ? 'warning' : 'success'"
                  dense
                  text
                  class="mb-0"
                >
                  Repaired: <strong>{{ fixComicInfo.result.repaired }}</strong> &nbsp;·&nbsp;
                  Skipped: <strong>{{ fixComicInfo.result.skipped }}</strong> &nbsp;·&nbsp;
                  Manga processed: <strong>{{ fixComicInfo.result.mangaProcessed }}</strong>
                  <div v-if="fixComicInfo.result.errors && fixComicInfo.result.errors.length" class="mt-1">
                    <div v-for="(err, i) in fixComicInfo.result.errors" :key="i" class="caption">{{ err }}</div>
                  </div>
                </v-alert>
              </v-card-text>
            </v-expand-transition>
          </v-card>
        </v-col>
      </v-row>
      <!-- ── END FIX ─────────────────────────────────────────────────────── -->

    </v-container>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" timeout="4000" top>
      {{ snackbar.text }}
      <template #action="{ attrs }">
        <v-btn text v-bind="attrs" @click="snackbar.show = false">Close</v-btn>
      </template>
    </v-snackbar>
  </div>
</template>

<script>
export default {
  name: 'SettingsFixes',
  data() {
    return {
      libraries: [],
      fixComicInfo: {
        libraryId: null,
        force: true,
        running: false,
        result: null,
      },
      snackbar: {
        show: false,
        text: '',
        color: 'error',
      },
    }
  },
  async mounted() {
    try {
      this.libraries = await this.$komgaLibraries.getLibraries()
    } catch (e) {
      this.showSnack('Failed to load libraries: ' + (e.message || e), 'error')
    }
  },
  methods: {
    async runComicInfoFix() {
      this.fixComicInfo.running = true
      this.fixComicInfo.result = null
      try {
        const params = this.fixComicInfo.force ? '?force=true' : ''
        await this.$http.post(`/api/v1/downloads/repair-comicinfo/${this.fixComicInfo.libraryId}${params}`)
        await this.pollRepairStatus()
      } catch (e) {
        this.showSnack('Fix failed: ' + (e?.response?.data?.message || e.message || 'Unknown error'), 'error')
        this.fixComicInfo.running = false
      }
    },
    async pollRepairStatus() {
      while (true) {
        await new Promise(r => setTimeout(r, 2000))
        try {
          const r = await this.$http.get('/api/v1/downloads/repair-comicinfo/status')
          const s = r.data
          this.fixComicInfo.result = {
            repaired: s.repaired,
            skipped: s.skipped,
            mangaProcessed: `${s.processed} / ${s.total}`,
            errors: s.errors,
          }
          if (!s.running) {
            this.fixComicInfo.running = false
            return
          }
        } catch (e) {
          this.showSnack('Status poll failed: ' + (e?.response?.data?.message || e.message || 'Unknown error'), 'error')
          this.fixComicInfo.running = false
          return
        }
      }
    },
    showSnack(text, color = 'success') {
      this.snackbar.text = text
      this.snackbar.color = color
      this.snackbar.show = true
    },
  },
}
</script>
