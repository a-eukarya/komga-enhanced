<template>
  <v-dialog v-model="modalVisible" :fullscreen="$vuetify.breakpoint.xsOnly" max-width="600">
    <v-card>
      <v-toolbar dark color="primary" flat>
        <v-toolbar-title>Add Chapter Download</v-toolbar-title>
        <v-spacer/>
        <v-btn icon @click="modalVisible = false"><v-icon>mdi-close</v-icon></v-btn>
      </v-toolbar>

      <v-card-text class="pt-4">
        <p v-if="series" class="text-caption mb-3">
          Series: <strong>{{ series.metadata.title }}</strong>
        </p>

        <v-btn-toggle v-model="mode" mandatory dense class="mb-4" color="primary">
          <v-btn value="single" small>Single Chapter</v-btn>
          <v-btn value="range" small>Series + Range</v-btn>
        </v-btn-toggle>

        <v-text-field
          v-model="form.url"
          :label="mode === 'single' ? 'Chapter URL' : 'Series URL'"
          :placeholder="mode === 'single' ? 'https://mangadex.org/chapter/…' : 'https://mangadex.org/title/…'"
          autofocus
          outlined
          dense
        />

        <v-text-field
          v-if="mode === 'range'"
          v-model="form.chapterRange"
          label="Chapter Range"
          placeholder="e.g. 20-30 or 20,22,25"
          outlined
          dense
          class="mt-4"
        />

        <v-switch
          v-if="mode === 'single'"
          v-model="form.useCustomNaming"
          label="Use custom naming"
          dense
          hide-details
          class="mt-2"
        />

        <template v-if="mode === 'single' && form.useCustomNaming">
          <v-text-field
            v-model="form.filename"
            label="Filename *"
            placeholder="e.g. Chapter 999.cbz"
            :rules="filenameRules"
            outlined
            dense
            class="mt-4"
          />
          <v-row no-gutters class="mt-1">
            <v-col cols="6" class="pr-2">
              <v-text-field
                v-model="form.chapterNumber"
                label="Chapter # *"
                placeholder="132 or 132.5"
                :rules="chapterNumberRules"
                outlined
                dense
              />
            </v-col>
            <v-col cols="6" class="pl-2">
              <v-text-field
                v-model="form.volume"
                label="Volume"
                placeholder="12"
                outlined
                dense
              />
            </v-col>
          </v-row>
          <v-text-field
            v-model="form.chapterTitle"
            label="Chapter Title"
            placeholder="e.g. The Big Battle"
            outlined
            dense
            class="mt-1"
          />
        </template>

        <v-switch
          v-model="form.skipIfChapterExists"
          label="Skip if file exists"
          hint="Compares against existing chapter numbers in this series (ComicInfo / DB), not filenames"
          persistent-hint
          dense
          class="mt-2"
        />
      </v-card-text>

      <v-card-actions>
        <v-spacer/>
        <v-btn text @click="modalVisible = false">Cancel</v-btn>
        <v-btn color="primary" :disabled="!canSubmit" :loading="submitting" @click="submit">Download</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import Vue from 'vue'
import {SeriesDto} from '@/types/komga-series'

export default Vue.extend({
  name: 'AddChapterDownloadDialog',
  props: {
    value: Boolean,
    series: {
      type: Object as () => SeriesDto | undefined,
      default: undefined,
    },
  },
  data: () => ({
    mode: 'single' as 'single' | 'range',
    form: {
      url: '',
      chapterRange: '',
      useCustomNaming: true,
      filename: '',
      chapterNumber: '',
      volume: '',
      chapterTitle: '',
      skipIfChapterExists: true,
    },
    submitting: false,
    filenameRules: [(v: string) => !!v || 'Filename is required'],
    chapterNumberRules: [(v: string) => !!v || 'Chapter number is required'],
  }),
  computed: {
    modalVisible: {
      get(): boolean { return this.value },
      set(val: boolean) { this.$emit('input', val) },
    },
    canSubmit(): boolean {
      if (!this.form.url) return false
      if (this.mode === 'range') return !!this.form.chapterRange
      if (this.form.useCustomNaming) {
        return !!this.form.filename && !!this.form.chapterNumber
      }
      return true
    },
  },
  watch: {
    value(newVal) {
      if (newVal) {
        this.mode = 'single'
        this.form = {
          url: '',
          chapterRange: '',
          useCustomNaming: true,
          filename: '',
          chapterNumber: '',
          volume: '',
          chapterTitle: '',
          skipIfChapterExists: true,
        }
      }
    },
  },
  methods: {
    async submit() {
      if (!this.canSubmit) return
      this.submitting = true
      try {
        const body: Record<string, unknown> = {
          sourceUrl: this.form.url,
          title: this.series?.metadata.title ?? null,
          libraryId: this.series?.libraryId ?? null,
          priority: 5,
          seriesId: this.series?.id ?? null,
          skipIfChapterExists: this.form.skipIfChapterExists,
        }
        if (this.mode === 'range') {
          body.chapterRange = this.form.chapterRange
        } else if (this.form.useCustomNaming) {
          body.customFilename = this.form.filename
          body.customChapterNumber = this.form.chapterNumber
          if (this.form.volume) body.customVolume = this.form.volume
          if (this.form.chapterTitle) body.customChapterTitle = this.form.chapterTitle
        }
        await this.$http.post('/api/v1/downloads', body)
        this.modalVisible = false
      } catch (e) {
        this.$emit('error', e)
      } finally {
        this.submitting = false
      }
    },
  },
})
</script>
