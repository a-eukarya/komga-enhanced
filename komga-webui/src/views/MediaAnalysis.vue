<template>
  <v-container fluid class="pa-6">
    <v-data-table
      :headers="headers"
      :items="booksData"
      :options.sync="options"
      :server-items-length="totalBooks"
      :loading="loading"
      sort-by="media.status"
      multi-sort
      class="elevation-1"
      :footer-props="{
        itemsPerPageOptions: [20, 50, 100]
      }"
    >
      <template v-slot:top>
        <v-container>
          <v-row>
            <v-col cols="12" sm="6">
              <v-chip-group v-model="filterStatus" color="primary" mandatory multiple>
                <v-chip filter value="error">{{ $t('common.error') }}</v-chip>
                <v-chip filter value="unsupported">{{ $t('book_card.unsupported') }}</v-chip>
              </v-chip-group>
            </v-col>
            <v-col cols="12" sm="6">
              <v-select v-model="filterLibraries"
                        :items="filterLibrariesOptions"
                        :label="$t('navigation.libraries')"
                        clearable
                        solo
                        multiple
                        chips
                        deletable-chips
              />
            </v-col>
          </v-row>
          <v-row align="center" class="mt-0">
            <v-col cols="auto">
              <v-btn color="warning" :disabled="verifyInProgress" :loading="verifyInProgress" @click="startVerify">
                Verify ZIP integrity
              </v-btn>
            </v-col>
            <v-col v-if="verifyInProgress || verifyTotal > 0" class="text-caption">
              {{ verifyProcessed }} / {{ verifyTotal }} checked · {{ verifyFlagged }} flagged
            </v-col>
            <v-col cols="auto" v-if="!verifyInProgress && verifyFlagged > 0">
              <v-btn color="primary" :disabled="repairInProgress" :loading="repairInProgress" @click="startRepair">
                Repair flagged ({{ verifyFlagged }})
              </v-btn>
            </v-col>
            <v-col cols="auto" v-if="!verifyInProgress && verifyFlagged > 0">
              <v-btn color="secondary" :loading="rescanning" :disabled="rescanning" @click="rescanFlagged">
                Rescan flagged ({{ verifyFlagged }})
              </v-btn>
            </v-col>
            <v-col v-if="repairInProgress || repairTotal > 0" class="text-caption">
              Repair: {{ repairProcessed }} / {{ repairTotal }} · fixed {{ repairFixed }} · partial {{ repairPartial }} · failed {{ repairFailed }}
            </v-col>
          </v-row>
        </v-container>
      </template>

      <template v-slot:item.libraryId="{ item }">
        {{ getLibraryName(item.libraryId) }}
      </template>

      <template v-slot:item.name="{ item }">
        <router-link
          :to="{name: item.oneshot ? 'browse-oneshot' : 'browse-book', params: {bookId: item.id, seriesId: item.seriesId}}">
          {{ item.name }}
        </router-link>
      </template>

      <template v-slot:item.deleted="{ item }">
        <v-chip
          v-if="item.deleted"
          label small color="error">
          {{ $t('common.unavailable') }}
        </v-chip>
      </template>

      <template v-slot:footer.prepend>
        <v-btn icon @click="loadBooks">
          <v-icon>mdi-refresh</v-icon>
        </v-btn>
      </template>
    </v-data-table>
    <v-snackbar v-model="rescanSnack" :timeout="4000" bottom>
      {{ rescanMsg }}
      <template v-slot:action="{ attrs }">
        <v-btn text v-bind="attrs" @click="rescanSnack = false">Close</v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'
import {MediaStatus} from '@/types/enum-books'
import {BookDto} from '@/types/komga-books'
import {convertErrorCodes} from '@/functions/error-codes'
import {
  BookSearch,
  SearchConditionAllOfBook,
  SearchConditionAnyOfBook,
  SearchConditionBook,
  SearchConditionLibraryId,
  SearchConditionMediaStatus,
  SearchOperatorIs,
} from '@/types/komga-search'

export default Vue.extend({
  name: 'MediaAnalysis',
  data: function () {
    return {
      books: [] as BookDto[],
      totalBooks: 0,
      loading: true,
      options: {
        itemsPerPage: this.$store?.state?.persistedState?.dataTablePageSize || 20,
      } as any,
      filterStatus: ['error', 'unsupported'],
      filterLibraries: [] as string[],
      verifyInProgress: false,
      verifyProcessed: 0,
      verifyTotal: 0,
      verifyFlagged: 0,
      verifyPollHandle: 0 as any,
      repairInProgress: false,
      repairProcessed: 0,
      repairTotal: 0,
      repairFixed: 0,
      repairPartial: 0,
      repairFailed: 0,
      rescanning: false,
      rescanMsg: '',
      rescanSnack: false,
    }
  },
  mounted() {
    this.refreshVerifyStatus()
  },
  beforeDestroy() {
    if (this.verifyPollHandle) clearInterval(this.verifyPollHandle)
  },
  watch: {
    options: {
      handler() {
        if (this.options.itemsPerPage) {
          this.$store.commit('setDataTablePageSize', this.options.itemsPerPage)
        }
        this.loadBooks()
      },
      deep: true,
    },
    filterStatus() {
      this.loadBooks()
    },
    filterLibraries() {
      this.loadBooks()
    },
  },
  computed: {
    filterLibrariesOptions(): object[] {
      return this.$store.state.komgaLibraries.libraries.map(x => ({
        text: x.name,
        value: x.id,
      }))
    },
    headers(): object[] {
      return [
        {text: this.$i18n.t('common.library').toString(), value: 'libraryId', sortable: false},
        {text: this.$i18n.t('media_analysis.name').toString(), value: 'name'},
        {text: this.$i18n.t('media_analysis.status').toString(), value: 'media.status'},
        {text: this.$i18n.t('media_analysis.comment').toString(), value: 'media.comment'},
        {text: this.$i18n.t('media_analysis.media_type').toString(), value: 'media.mediaType'},
        {text: this.$i18n.t('media_analysis.url').toString(), value: 'url'},
        {text: this.$i18n.t('media_analysis.size').toString(), value: 'size'},
        {text: '', value: 'deleted', groupable: false, sortable: false},
      ]
    },
    booksData(): BookDto[] {
      return this.books.map((b: BookDto) => ({
        ...b,
        media: {
          ...b.media,
          comment: convertErrorCodes(b.media.comment),
          status: this.$t(`enums.media_status.${b.media.status}`).toString(),
        },
      }))
    },
  },
  methods: {
    getLibraryName(libraryId: string): string {
      return this.$store.getters.getLibraryById(libraryId).name
    },
    async refreshVerifyStatus() {
      try {
        const r = await this.$http.get('/api/v1/media-management/integrity/status')
        this.verifyInProgress = r.data.inProgress
        this.verifyProcessed = r.data.processed
        this.verifyTotal = r.data.total
        this.verifyFlagged = r.data.flagged
        this.repairInProgress = r.data.repairInProgress
        this.repairProcessed = r.data.repairProcessed
        this.repairTotal = r.data.repairTotal
        this.repairFixed = r.data.repairFixed
        this.repairPartial = r.data.repairPartial
        this.repairFailed = r.data.repairFailed
        const stillRunning = this.verifyInProgress || this.repairInProgress
        if (stillRunning && !this.verifyPollHandle) {
          this.verifyPollHandle = setInterval(() => this.refreshVerifyStatus(), 3000)
        } else if (!stillRunning && this.verifyPollHandle) {
          clearInterval(this.verifyPollHandle)
          this.verifyPollHandle = 0
          this.loadBooks()
        }
      } catch {
      }
    },
    async startVerify() {
      try {
        await this.$http.post('/api/v1/media-management/integrity/verify')
        this.verifyInProgress = true
        this.refreshVerifyStatus()
      } catch (e: any) {
        if (e.response?.status === 409) {
          this.refreshVerifyStatus()
        } else {
          this.$eventHub.$emit('error', {message: e.message})
        }
      }
    },
    async startRepair() {
      try {
        await this.$http.post('/api/v1/media-management/integrity/repair')
        this.repairInProgress = true
        this.refreshVerifyStatus()
      } catch (e: any) {
        if (e.response?.status === 409) {
          this.refreshVerifyStatus()
        } else {
          this.$eventHub.$emit('error', {message: e.message})
        }
      }
    },
    async rescanFlagged() {
      this.rescanning = true
      try {
        const r = await this.$http.post('/api/v1/media-management/integrity/rescan')
        this.rescanMsg = `${r.data.queued} re-queued for analyze · ${r.data.stillCorrupt} still corrupt (kept as ERROR)`
        this.rescanSnack = true
        setTimeout(() => this.loadBooks(), 2000)
      } catch (e: any) {
        this.rescanMsg = `Rescan failed: ${e.message}`
        this.rescanSnack = true
      } finally {
        this.rescanning = false
      }
    },
    async loadBooks() {
      this.loading = true

      const {sortBy, sortDesc, page, itemsPerPage} = this.options

      const pageRequest = {
        page: page - 1,
        size: itemsPerPage,
        sort: [],
      } as PageRequest

      for (let i = 0; i < sortBy.length; i++) {
        pageRequest.sort!!.push(`${sortBy[i]},${sortDesc[i] ? 'desc' : 'asc'}`)
      }

      const conditionsStatus = [] as SearchConditionBook[]
      if (this.filterStatus.includes('error')) conditionsStatus.push(new SearchConditionMediaStatus(new SearchOperatorIs(MediaStatus.ERROR)))
      if (this.filterStatus.includes('unsupported')) conditionsStatus.push(new SearchConditionMediaStatus(new SearchOperatorIs(MediaStatus.UNSUPPORTED)))

      const conditionsLibraries = [] as SearchConditionBook[]
      this.filterLibraries.forEach(x => conditionsLibraries.push(new SearchConditionLibraryId(new SearchOperatorIs(x))))

      if (this.filterStatus.length > 0) {
        const booksPage = await this.$komgaBooks.getBooksList({
          condition: new SearchConditionAllOfBook([
            new SearchConditionAnyOfBook(conditionsStatus),
            new SearchConditionAnyOfBook(conditionsLibraries),
          ]),
        } as BookSearch, pageRequest)
        this.totalBooks = booksPage.totalElements
        this.$store.commit('setBooksToCheck', booksPage.totalElements)
        this.books = booksPage.content
      }

      this.loading = false
    },
  },
})
</script>

<style scoped>

</style>
