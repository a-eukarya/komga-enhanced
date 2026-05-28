<template>
  <div>
    <v-container fluid>
      <v-row>
        <v-col>
          <h1 class="text-h4 mb-4">
            <v-icon large class="mr-2">mdi-download</v-icon>
            Downloads
            <v-chip
              small
              color="success"
              class="ml-2"
            >
              <v-icon x-small left>mdi-broadcast</v-icon>
              Live
            </v-chip>
          </h1>
        </v-col>
      </v-row>

      <!-- Stats Cards -->
      <v-row dense>
        <v-col cols="6" sm="3">
          <v-card>
            <v-card-text class="pa-3">
              <div class="text-h5 text-sm-h4">{{ activeDownloads.length }}</div>
              <div class="text-caption text-sm-subtitle-2">Active</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="6" sm="3">
          <v-card>
            <v-card-text class="pa-3">
              <div class="text-h5 text-sm-h4">{{ pendingDownloads.length }}</div>
              <div class="text-caption text-sm-subtitle-2">Pending</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="6" sm="3">
          <v-card>
            <v-card-text class="pa-3">
              <div class="text-h5 text-sm-h4 success--text">{{ completedDownloads.length }}</div>
              <div class="text-caption text-sm-subtitle-2">Completed</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="6" sm="3">
          <v-card>
            <v-card-text class="pa-3">
              <div class="text-h5 text-sm-h4 error--text">{{ failedDownloads.length }}</div>
              <div class="text-caption text-sm-subtitle-2">Failed</div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Active Downloads -->
      <v-row v-if="activeDownloads.length > 0">
        <v-col cols="12">
          <v-card>
            <v-card-title>
              <v-icon left>mdi-download-circle</v-icon>
              Active Downloads
            </v-card-title>
            <v-card-text>
              <v-list>
                <v-list-item v-for="download in activeDownloads" :key="download.id">
                  <v-list-item-content>
                    <v-list-item-title>{{ download.title || download.sourceUrl }}</v-list-item-title>
                    <v-list-item-subtitle>
                      {{ download.currentChapter }}/{{ download.totalChapters }} chapters
                    </v-list-item-subtitle>
                    <v-progress-linear
                      :value="download.progressPercent"
                      height="25"
                      class="mt-2"
                    >
                      <strong>{{ download.progressPercent }}%</strong>
                    </v-progress-linear>
                  </v-list-item-content>
                  <v-list-item-action>
                    <div>
                      <v-btn icon @click="pauseDownload(download)" title="Pause">
                        <v-icon>mdi-pause</v-icon>
                      </v-btn>
                      <v-btn icon @click="cancelDownload(download)" title="Cancel">
                        <v-icon color="error">mdi-close</v-icon>
                      </v-btn>
                    </div>
                  </v-list-item-action>
                </v-list-item>
              </v-list>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- MangaDex Search -->
      <v-row class="mb-2">
        <v-col cols="12">
          <v-card outlined>
            <v-card-title class="flex-wrap pb-2">
              <v-icon left color="primary">mdi-magnify</v-icon>
              <span class="text-subtitle-1 text-sm-h6">Find Manga on MangaDex</span>
            </v-card-title>
            <v-card-text class="pb-2">
              <v-row align="center" dense>
                <v-col cols="12" sm="5" md="5">
                  <v-text-field
                    v-model="searchQuery"
                    label="Search title..."
                    outlined
                    dense
                    hide-details
                    clearable
                    prepend-inner-icon="mdi-magnify"
                    :loading="searchLoading"
                    @keyup.enter="searchMangaDex"
                  />
                </v-col>
                <v-col cols="12" sm="4" md="4">
                  <v-select
                    v-model="searchLibraryId"
                    :items="libraries"
                    item-text="name"
                    item-value="id"
                    label="Target library"
                    outlined
                    dense
                    hide-details
                  />
                </v-col>
                <v-col cols="12" sm="3" md="3">
                  <v-btn color="primary" depressed block @click="searchMangaDex" :loading="searchLoading" :disabled="!searchQuery && !hasFilters">
                    <v-icon left>mdi-magnify</v-icon>
                    {{ searchQuery ? 'Search' : 'Browse' }}
                  </v-btn>
                </v-col>
              </v-row>

              <v-expansion-panels flat tile v-model="advancedPanel" class="mt-2">
                <v-expansion-panel>
                  <v-expansion-panel-header class="px-0 py-1 text--secondary">
                    <span>
                      <v-icon small left>mdi-filter-variant</v-icon>
                      Advanced filters (random browse when title is empty)
                      <v-chip v-if="hasFilters" x-small color="primary" class="ml-2">{{ activeFilterCount }} active</v-chip>
                    </span>
                  </v-expansion-panel-header>
                  <v-expansion-panel-content class="pa-0">
                    <v-row dense>
                      <v-col cols="12" md="6">
                        <v-autocomplete
                          v-model="filterTags"
                          :items="tagOptions"
                          item-text="name"
                          item-value="id"
                          label="Include tags / genres"
                          multiple chips small-chips deletable-chips
                          outlined dense hide-details
                          :loading="loadingTags"
                          @focus="loadTagsIfNeeded"
                        >
                          <template v-slot:item="{ item }">
                            <v-list-item-content>
                              <v-list-item-title>{{ item.name }}</v-list-item-title>
                              <v-list-item-subtitle class="caption">{{ item.group }}</v-list-item-subtitle>
                            </v-list-item-content>
                          </template>
                        </v-autocomplete>
                      </v-col>
                      <v-col cols="12" md="6">
                        <v-autocomplete
                          v-model="filterExcludedTags"
                          :items="tagOptions"
                          item-text="name"
                          item-value="id"
                          label="Blacklist tags / genres"
                          multiple chips small-chips deletable-chips
                          outlined dense hide-details
                          :loading="loadingTags"
                          @focus="loadTagsIfNeeded"
                          color="error"
                          item-color="error"
                        >
                          <template v-slot:item="{ item }">
                            <v-list-item-content>
                              <v-list-item-title>{{ item.name }}</v-list-item-title>
                              <v-list-item-subtitle class="caption">{{ item.group }}</v-list-item-subtitle>
                            </v-list-item-content>
                          </template>
                        </v-autocomplete>
                      </v-col>
                      <v-col cols="12" md="6">
                        <v-select
                          v-model="filterStatus"
                          :items="['ongoing','completed','hiatus','cancelled']"
                          label="Status" multiple chips small-chips deletable-chips
                          outlined dense hide-details
                        />
                      </v-col>
                      <v-col cols="12" md="6">
                        <v-select
                          v-model="filterRating"
                          :items="['safe','suggestive','erotica','pornographic']"
                          label="Content rating" multiple chips small-chips deletable-chips
                          outlined dense hide-details
                          hint="Empty = all"
                          persistent-hint
                        />
                      </v-col>
                      <v-col cols="12" md="6">
                        <v-select
                          v-model="filterDemographic"
                          :items="['shounen','shoujo','seinen','josei','none']"
                          label="Publication demographic" multiple chips small-chips deletable-chips
                          outlined dense hide-details
                        />
                      </v-col>
                      <v-col cols="12" md="6" class="d-flex align-center">
                        <v-switch
                          v-model="filterAvailableOnly"
                          label="Only titles with downloadable chapters"
                          hint="1 extra MangaDex API call per result (24h cache). Hides external-link / 0-page chapters."
                          persistent-hint
                          dense
                          class="mt-0"
                        />
                      </v-col>
                      <v-col cols="12" md="6" class="d-flex align-center">
                        <v-switch
                          v-model="filterHideFollowed"
                          label="Hide titles already in any follow.txt"
                          hide-details
                          dense
                          class="mt-0"
                        />
                      </v-col>
                      <v-col cols="12" md="6" class="d-flex align-center">
                        <v-select
                          v-model="searchOrder"
                          :items="sortOptions"
                          label="Sort by"
                          dense
                          hide-details
                          outlined
                          class="mt-0 me-2"
                          @change="onSortChange"
                        />
                        <v-select
                          v-model="searchOrderDir"
                          :items="[{text: 'Desc', value: 'desc'}, {text: 'Asc', value: 'asc'}]"
                          label="Direction"
                          dense
                          hide-details
                          outlined
                          style="max-width:120px"
                          @change="onSortChange"
                        />
                      </v-col>
                      <v-col cols="12" class="d-flex align-center pt-2">
                        <v-btn small text @click="saveFilterDefaults" :color="filtersDirty ? 'primary' : ''">
                          <v-icon small left>mdi-content-save</v-icon>
                          {{ filtersDirty ? 'Save as default' : 'Saved as default' }}
                        </v-btn>
                        <v-btn small text @click="clearFilters">
                          <v-icon small left>mdi-close</v-icon>
                          Clear all
                        </v-btn>
                        <v-spacer />
                        <span class="caption text--secondary">Defaults are stored in your account</span>
                      </v-col>
                    </v-row>
                  </v-expansion-panel-content>
                </v-expansion-panel>
              </v-expansion-panels>

              <v-row v-if="searchResults.length > 0" class="mt-2" dense>
                <v-col
                  v-for="manga in searchResults"
                  :key="manga.externalId"
                  cols="4" sm="3" md="2" lg="2"
                  style="min-width:130px;max-width:180px;"
                >
                  <v-card outlined height="100%" class="d-flex flex-column">
                    <div class="grey lighten-3 d-flex align-center justify-center" style="width:100%;padding-top:150%;position:relative;cursor:pointer;" @click="showMangaDetails(manga)" title="Show description">
                      <img
                        v-if="manga.coverUrl"
                        :src="manga.coverUrl"
                        referrerpolicy="no-referrer"
                        alt=""
                        style="position:absolute;top:0;left:0;width:100%;height:100%;object-fit:cover;"
                      />
                      <v-icon v-else color="grey lighten-1" style="position:absolute;">mdi-book-open-page-variant</v-icon>
                    </div>
                    <v-card-text class="pa-1 flex-grow-1">
                      <div class="text-caption font-weight-bold" style="line-height:1.2;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden;cursor:pointer;" @click="showMangaDetails(manga)">{{ manga.title }}</div>
                      <v-chip v-if="manga.status" x-small class="mt-1" :color="statusColor(manga.status)">{{ manga.status }}</v-chip>
                    </v-card-text>
                    <v-card-actions class="pa-1 pt-0 flex-column">
                      <v-btn
                        x-small block depressed
                        :color="searchAction[manga.externalId] === 'downloaded' ? 'success' : searchAction[manga.externalId] === 'error' ? 'error' : 'primary'"
                        :loading="searchBusy[manga.externalId + ':dl']"
                        :disabled="!!searchAction[manga.externalId]"
                        @click="downloadFromSearch(manga)"
                      >
                        <v-icon x-small left>{{ searchAction[manga.externalId] === 'downloaded' ? 'mdi-check' : 'mdi-download' }}</v-icon>
                        {{ searchAction[manga.externalId] === 'downloaded' ? 'Queued' : 'Download' }}
                      </v-btn>
                      <v-btn
                        x-small block depressed outlined
                        class="mt-1 mx-0"
                        :color="isFollowed(manga) ? 'success' : (searchFollow[manga.externalId] === 'error' ? 'error' : '')"
                        :loading="searchBusy[manga.externalId + ':fl']"
                        @click="toggleFollow(manga)"
                      >
                        <v-icon x-small left>{{ isFollowed(manga) ? 'mdi-check' : 'mdi-playlist-plus' }}</v-icon>
                        {{ isFollowed(manga) ? 'Following' : 'Follow' }}
                      </v-btn>
                    </v-card-actions>
                  </v-card>
                </v-col>
              </v-row>

              <v-dialog v-model="detailsDialog" max-width="700" scrollable>
                <v-card v-if="detailsManga">
                  <v-card-title class="text-h6" style="word-break:normal;">{{ detailsManga.title }}</v-card-title>
                  <v-card-text>
                    <v-row dense>
                      <v-col cols="12" sm="4">
                        <img
                          v-if="detailsManga.coverUrl"
                          :src="detailsManga.coverUrl"
                          referrerpolicy="no-referrer"
                          alt=""
                          style="width:100%;border-radius:4px;"
                        />
                      </v-col>
                      <v-col cols="12" sm="8">
                        <div class="mb-2">
                          <v-chip v-if="detailsManga.status" x-small :color="statusColor(detailsManga.status)" class="me-1">{{ detailsManga.status }}</v-chip>
                          <v-chip v-if="detailsManga.year" x-small class="me-1">{{ detailsManga.year }}</v-chip>
                          <span v-if="detailsManga.author" class="text-caption">{{ detailsManga.author }}</span>
                        </div>
                        <div v-if="detailsManga.tags && detailsManga.tags.length" class="mb-2">
                          <v-chip v-for="t in detailsManga.tags" :key="t" x-small outlined class="me-1 mb-1">{{ t }}</v-chip>
                        </div>
                        <div class="body-2" style="white-space:pre-line;">{{ detailsManga.description || 'No description available.' }}</div>
                      </v-col>
                    </v-row>
                  </v-card-text>
                  <v-card-actions>
                    <v-btn
                      text
                      color="primary"
                      :disabled="!!searchAction[detailsManga.externalId]"
                      @click="downloadFromSearch(detailsManga)"
                    >
                      <v-icon left>mdi-download</v-icon>
                      {{ searchAction[detailsManga.externalId] === 'downloaded' ? 'Queued' : 'Download' }}
                    </v-btn>
                    <v-spacer />
                    <v-btn text @click="detailsDialog = false">Close</v-btn>
                  </v-card-actions>
                </v-card>
              </v-dialog>

              <div v-if="searchDone && searchResults.length === 0 && !searchLoading" class="text-center py-2 text--secondary">
                No results found{{ lastSearchSkippedNote }}
              </div>

              <v-alert v-if="followedUuidsLoadError" type="warning" dense text class="mt-2 mb-0">
                Could not read follow.txt from: {{ followedUuidsLoadError }}
              </v-alert>

              <div v-if="searchDone && searchPageCount > 1" class="d-flex align-center justify-center mt-2">
                <v-pagination
                  :value="searchPage"
                  :length="searchPageCount"
                  :total-visible="7"
                  @input="onSearchPageChange"
                  :disabled="searchLoading"
                />
                <span class="caption text--secondary ml-3">{{ searchTotal }} total</span>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Download Queue (All) -->
      <v-row>
        <v-col cols="12">
          <v-card>
            <v-card-title class="flex-wrap">
              <span class="text-subtitle-1 text-sm-h6">Download Queue</span>
              <v-spacer></v-spacer>
              <v-btn color="primary" @click="newDownloadDialog = true">
                <v-icon :left="$vuetify.breakpoint.smAndUp">mdi-plus</v-icon>
                <span class="d-none d-sm-inline">New Download</span>
              </v-btn>
              <v-menu offset-y>
                <template v-slot:activator="{ on, attrs }">
                  <v-btn text v-bind="attrs" v-on="on" class="ml-2">
                    <v-icon :left="$vuetify.breakpoint.smAndUp">mdi-broom</v-icon>
                    <span class="d-none d-sm-inline">Clear</span>
                    <v-icon right>mdi-menu-down</v-icon>
                  </v-btn>
                </template>
                <v-list dense>
                  <v-list-item @click="clearByStatus('completed')" :disabled="completedDownloads.length === 0">
                    <v-list-item-icon><v-icon color="success">mdi-check-circle</v-icon></v-list-item-icon>
                    <v-list-item-content>Clear Completed ({{ completedDownloads.length }})</v-list-item-content>
                  </v-list-item>
                  <v-list-item @click="clearByStatus('failed')" :disabled="failedDownloads.length === 0">
                    <v-list-item-icon><v-icon color="error">mdi-alert-circle</v-icon></v-list-item-icon>
                    <v-list-item-content>Clear Failed ({{ failedDownloads.length }})</v-list-item-content>
                  </v-list-item>
                  <v-list-item @click="clearByStatus('cancelled')" :disabled="cancelledDownloads.length === 0">
                    <v-list-item-icon><v-icon color="warning">mdi-cancel</v-icon></v-list-item-icon>
                    <v-list-item-content>Clear Cancelled ({{ cancelledDownloads.length }})</v-list-item-content>
                  </v-list-item>
                  <v-divider></v-divider>
                  <v-list-item @click="clearByStatus('pending')" :disabled="pendingDownloads.length === 0">
                    <v-list-item-icon><v-icon color="grey">mdi-clock-outline</v-icon></v-list-item-icon>
                    <v-list-item-content>Clear Pending ({{ pendingDownloads.length }})</v-list-item-content>
                  </v-list-item>
                </v-list>
              </v-menu>
              <v-btn icon @click="loadDownloads" :loading="loading" class="ml-2">
                <v-icon>mdi-refresh</v-icon>
              </v-btn>
            </v-card-title>

            <v-card-text>
              <v-tabs v-model="tab">
                <v-tab>All</v-tab>
                <v-tab>Pending</v-tab>
                <v-tab>Downloading</v-tab>
                <v-tab>Completed</v-tab>
                <v-tab>Failed</v-tab>
                <v-tab>
                  <v-icon left>mdi-cog</v-icon>
                  Configuration
                </v-tab>
                <v-tab>
                  <v-icon left>mdi-import</v-icon>
                  Tachiyomi Import
                </v-tab>
              </v-tabs>

              <v-tabs-items v-model="tab" class="mt-4">
                <v-tab-item>
                  <download-table :downloads="allDownloads" @action="handleAction" />
                </v-tab-item>
                <v-tab-item>
                  <download-table :downloads="pendingDownloads" @action="handleAction" />
                </v-tab-item>
                <v-tab-item>
                  <download-table :downloads="activeDownloads" @action="handleAction" />
                </v-tab-item>
                <v-tab-item>
                  <download-table :downloads="completedDownloads" @action="handleAction" />
                </v-tab-item>
                <v-tab-item>
                  <download-table :downloads="failedDownloads" @action="handleAction" />
                </v-tab-item>
                <v-tab-item>
                  <!-- Follow Configuration Tab - Library-based follow.txt -->
                  <v-row>
                    <v-col cols="12" md="4">
                      <v-card outlined>
                        <v-card-title>
                          <v-icon left>mdi-bookshelf</v-icon>
                          Libraries
                        </v-card-title>
                        <v-list>
                          <v-list-item-group v-model="selectedLibraryIndex" color="primary">
                            <v-list-item
                              v-for="(lib, index) in libraries"
                              :key="lib.id"
                              @click="selectLibrary(index)"
                            >
                              <v-list-item-content>
                                <v-list-item-title>{{ lib.name }}</v-list-item-title>
                              </v-list-item-content>
                            </v-list-item>
                          </v-list-item-group>
                        </v-list>
                      </v-card>
                    </v-col>
                    <v-col cols="12" md="8">
                      <v-card outlined v-if="selectedLibrary">
                        <v-card-title>
                          <v-icon left>mdi-file-document-outline</v-icon>
                          follow.txt - {{ selectedLibrary.name }}
                        </v-card-title>
                        <v-card-subtitle>
                          Edit the follow.txt file in this library. One URL per line.
                        </v-card-subtitle>
                        <v-card-text>
                          <v-textarea
                            v-model="followTxtContent"
                            label="Follow URLs"
                            hint="MangaDex URLs, one per line. Lines starting with # are comments."
                            persistent-hint
                            outlined
                            rows="12"
                            placeholder="https://mangadex.org/title/...&#10;# This is a comment"
                            :loading="loadingFollowTxt"
                          ></v-textarea>
                        </v-card-text>
                        <v-card-actions class="flex-wrap">
                          <v-btn
                            text
                            @click="checkNow"
                            :loading="checkingNow"
                          >
                            <v-icon :left="$vuetify.breakpoint.smAndUp">mdi-update</v-icon>
                            <span class="d-none d-sm-inline">Check Now</span>
                          </v-btn>
                          <v-btn
                            v-if="mangaDexPluginEnabled"
                            text
                            @click="syncToMangaDex"
                            :loading="syncingToMangaDex"
                          >
                            <v-icon :left="$vuetify.breakpoint.smAndUp">mdi-cloud-upload</v-icon>
                            <span class="d-none d-sm-inline">Sync to MangaDex</span>
                          </v-btn>
                          <v-spacer></v-spacer>
                          <v-btn text @click="loadFollowTxt">
                            <v-icon :left="$vuetify.breakpoint.smAndUp">mdi-refresh</v-icon>
                            <span class="d-none d-sm-inline">Reload</span>
                          </v-btn>
                          <v-btn
                            color="primary"
                            @click="saveFollowTxt"
                            :loading="savingFollowTxt"
                            :disabled="!followTxtChanged"
                          >
                            <v-icon :left="$vuetify.breakpoint.smAndUp">mdi-content-save</v-icon>
                            <span class="d-none d-sm-inline">Save</span>
                          </v-btn>
                        </v-card-actions>
                      </v-card>
                      <v-card outlined v-else>
                        <v-card-text class="text-center pa-8">
                          <v-icon size="64" color="grey">mdi-arrow-left</v-icon>
                          <p class="mt-4">Select a library to edit its follow.txt</p>
                        </v-card-text>
                      </v-card>

                      <!-- Scheduler Settings -->
                      <v-card outlined class="mt-4">
                        <v-card-title>
                          <v-icon left>mdi-clock-outline</v-icon>
                          Auto-Check Settings
                        </v-card-title>
                        <v-card-text>
                          <v-row>
                            <v-col cols="12" sm="6">
                              <v-switch
                                v-model="schedulerEnabled"
                                label="Enable Auto-Check"
                                hint="Automatically check and download new chapters"
                                persistent-hint
                              ></v-switch>
                            </v-col>
                          </v-row>
                          <v-row>
                            <v-col cols="12">
                              <v-radio-group
                                v-model="scheduleMode"
                                row
                                label="Schedule Mode"
                              >
                                <v-radio label="Interval" value="interval"></v-radio>
                                <v-radio label="Fixed Time" value="fixed_time"></v-radio>
                              </v-radio-group>
                            </v-col>
                          </v-row>
                          <v-row>
                            <v-col cols="12" sm="6" v-if="scheduleMode === 'interval'">
                              <v-text-field
                                v-model.number="schedulerInterval"
                                label="Check Interval (hours)"
                                type="number"
                                outlined
                                dense
                                min="1"
                                hint="How often to check all libraries for new chapters"
                                persistent-hint
                              ></v-text-field>
                            </v-col>
                            <v-col cols="12" sm="6" v-if="scheduleMode === 'fixed_time'">
                              <v-text-field
                                v-model="checkTime"
                                label="Check Time (HH:mm)"
                                placeholder="03:00"
                                outlined
                                dense
                                hint="Daily time to check for new chapters (24h format)"
                                persistent-hint
                              ></v-text-field>
                            </v-col>
                          </v-row>
                        </v-card-text>
                        <v-card-actions>
                          <v-spacer></v-spacer>
                          <v-btn
                            color="primary"
                            @click="saveSchedulerSettings"
                            :loading="savingScheduler"
                          >
                            <v-icon left>mdi-content-save</v-icon>
                            Save Settings
                          </v-btn>
                        </v-card-actions>
                      </v-card>
                    </v-col>
                  </v-row>
                </v-tab-item>
                <v-tab-item>
                  <!-- Tachiyomi Import Tab -->
                  <v-row>
                    <v-col cols="12" md="6">
                      <v-card outlined>
                        <v-card-title>
                          <v-icon left>mdi-import</v-icon>
                          Import from Tachiyomi/Mihon Backup
                        </v-card-title>
                        <v-card-subtitle>
                          Import MangaDex URLs from a Tachiyomi or Mihon backup file into a library's follow.txt
                        </v-card-subtitle>
                        <v-card-text>
                          <v-file-input
                            v-model="tachiyomiFile"
                            label="Backup File"
                            accept=".proto.gz,.tachibk,.json,.json.gz"
                            prepend-icon="mdi-file-upload"
                            outlined
                            show-size
                            hint="Supports .proto.gz, .tachibk, .json, .json.gz formats"
                            persistent-hint
                          ></v-file-input>

                          <v-select
                            v-model="tachiyomiLibraryId"
                            :items="libraries"
                            item-text="name"
                            item-value="id"
                            label="Target Library"
                            outlined
                            prepend-icon="mdi-bookshelf"
                            hint="MangaDex URLs will be added to this library's follow.txt"
                            persistent-hint
                            class="mt-4"
                          />
                        </v-card-text>
                        <v-card-actions>
                          <v-spacer></v-spacer>
                          <v-btn
                            color="primary"
                            @click="importTachiyomi"
                            :loading="importingTachiyomi"
                            :disabled="!tachiyomiFile || !tachiyomiLibraryId"
                          >
                            <v-icon left>mdi-import</v-icon>
                            Import
                          </v-btn>
                        </v-card-actions>
                      </v-card>
                    </v-col>
                    <v-col cols="12" md="6">
                      <!-- Import Result -->
                      <v-card outlined v-if="tachiyomiResult">
                        <v-card-title>
                          <v-icon left :color="tachiyomiResult.success ? 'success' : 'warning'">
                            {{ tachiyomiResult.success ? 'mdi-check-circle' : 'mdi-alert-circle' }}
                          </v-icon>
                          Import Result
                        </v-card-title>
                        <v-card-text>
                          <v-alert :type="tachiyomiResult.success ? 'success' : 'warning'" dense>
                            {{ tachiyomiResult.message }}
                          </v-alert>
                          <v-row class="mt-2">
                            <v-col cols="6" sm="3">
                              <div class="text-h5">{{ tachiyomiResult.totalInBackup }}</div>
                              <div class="text-caption">Total in Backup</div>
                            </v-col>
                            <v-col cols="6" sm="3">
                              <div class="text-h5">{{ tachiyomiResult.mangaDexCount }}</div>
                              <div class="text-caption">MangaDex</div>
                            </v-col>
                            <v-col cols="6" sm="3">
                              <div class="text-h5 success--text">{{ tachiyomiResult.importedCount }}</div>
                              <div class="text-caption">Imported</div>
                            </v-col>
                            <v-col cols="6" sm="3">
                              <div class="text-h5 grey--text">{{ tachiyomiResult.skippedCount }}</div>
                              <div class="text-caption">Skipped</div>
                            </v-col>
                          </v-row>
                          <v-expansion-panels class="mt-4" v-if="tachiyomiResult.imported.length > 0 || tachiyomiResult.errors.length > 0">
                            <v-expansion-panel v-if="tachiyomiResult.imported.length > 0">
                              <v-expansion-panel-header>
                                <v-icon left color="success" small>mdi-check</v-icon>
                                Imported ({{ tachiyomiResult.imported.length }})
                              </v-expansion-panel-header>
                              <v-expansion-panel-content>
                                <v-list dense>
                                  <v-list-item v-for="(title, i) in tachiyomiResult.imported" :key="'imp-'+i">
                                    <v-list-item-content>{{ title }}</v-list-item-content>
                                  </v-list-item>
                                </v-list>
                              </v-expansion-panel-content>
                            </v-expansion-panel>
                            <v-expansion-panel v-if="tachiyomiResult.errors.length > 0">
                              <v-expansion-panel-header>
                                <v-icon left color="error" small>mdi-alert</v-icon>
                                Errors ({{ tachiyomiResult.errors.length }})
                              </v-expansion-panel-header>
                              <v-expansion-panel-content>
                                <v-list dense>
                                  <v-list-item v-for="(err, i) in tachiyomiResult.errors" :key="'err-'+i">
                                    <v-list-item-content class="error--text">{{ err }}</v-list-item-content>
                                  </v-list-item>
                                </v-list>
                              </v-expansion-panel-content>
                            </v-expansion-panel>
                          </v-expansion-panels>
                        </v-card-text>
                      </v-card>
                      <v-card outlined v-else>
                        <v-card-text class="text-center pa-8">
                          <v-icon size="64" color="grey">mdi-file-upload-outline</v-icon>
                          <p class="mt-4">Select a backup file and target library to import</p>
                          <p class="text-caption grey--text">
                            Only MangaDex entries will be imported from the backup.
                            Other sources are not supported.
                          </p>
                        </v-card-text>
                      </v-card>
                    </v-col>
                  </v-row>
                </v-tab-item>
              </v-tabs-items>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>

    <!-- New Download Dialog -->
    <v-dialog v-model="newDownloadDialog" max-width="600" :fullscreen="$vuetify.breakpoint.xsOnly">
      <v-card>
        <v-card-title>Add Download</v-card-title>
        <v-card-text>
          <v-text-field
            v-model="newDownload.sourceUrl"
            label="Source URL"
            placeholder="https://mangadex.org/title/..."
            outlined
            prepend-icon="mdi-link"
          ></v-text-field>

          <v-select
            v-model="newDownload.libraryId"
            :items="libraries"
            item-text="name"
            item-value="id"
            label="Target Library"
            outlined
            prepend-icon="mdi-bookshelf"
            hint="Downloads will go directly into this library folder"
            persistent-hint
          />

          <v-slider
            v-model="newDownload.priority"
            :min="1"
            :max="10"
            label="Priority"
            thumb-label
            prepend-icon="mdi-flag"
            class="mt-4"
          ></v-slider>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="newDownloadDialog = false">Cancel</v-btn>
          <v-btn color="primary" @click="addDownload" :loading="adding">
            Add to Queue
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Snackbar -->
    <v-snackbar v-model="snackbar" :color="snackbarColor" :timeout="3000" bottom>
      {{ snackbarText }}
      <template v-slot:action="{ attrs }">
        <v-btn text v-bind="attrs" @click="snackbar = false">Close</v-btn>
      </template>
    </v-snackbar>
  </div>
</template>

<script>
import DownloadTable from '../components/DownloadTable.vue'
import {
  DOWNLOAD_STARTED,
  DOWNLOAD_PROGRESS,
  DOWNLOAD_COMPLETED,
  DOWNLOAD_FAILED,
} from '@/types/events'

export default {
  name: 'DownloadDashboard',
  components: {
    DownloadTable,
  },
  data() {
    return {
      downloads: [],
      libraries: [],
      loading: false,
      adding: false,
      tab: 0,
      newDownloadDialog: false,
      newDownload: {
        sourceUrl: '',
        libraryId: null,
        priority: 5,
      },
      snackbar: false,
      snackbarText: '',
      snackbarColor: 'success',
      // Library follow.txt
      selectedLibraryIndex: null,
      followTxtContent: '',
      originalFollowTxtContent: '',
      loadingFollowTxt: false,
      savingFollowTxt: false,
      checkingNow: false,
      syncingToMangaDex: false,
      mangaDexPluginEnabled: false,
      // Scheduler settings
      schedulerEnabled: false,
      schedulerInterval: 6,
      scheduleMode: 'interval',
      checkTime: '03:00',
      savingScheduler: false,
      // SSE connection status (using existing SSE infrastructure)
      sseConnected: true,
      // Tachiyomi import
      tachiyomiFile: null,
      tachiyomiLibraryId: null,
      importingTachiyomi: false,
      tachiyomiResult: null,
      // MangaDex search (top of page)
      searchQuery: '',
      searchLibraryId: null,
      searchResults: [],
      searchLoading: false,
      searchDone: false,
      searchBusy: {},
      searchAction: {},
      searchFollow: {},
      // Advanced filters
      advancedPanel: null,
      filterTags: [],
      filterExcludedTags: [],
      filterStatus: [],
      filterRating: [],
      filterDemographic: [],
      filterAvailableOnly: false,
      filterHideFollowed: false,
      tagOptions: [],
      loadingTags: false,
      savedFiltersHash: '',
      FILTER_DEFAULTS_KEY: 'komga.fork.mangadexsearch.defaults',
      lastSearchSkippedAvailable: 0,
      lastSearchSkippedFollow: 0,
      // Pagination
      searchPageSize: 24,
      searchPage: 1,
      searchTotal: 0,
      searchOrder: '',
      searchOrderDir: 'desc',
      followedUuids: [],
      followedUuidsLoadError: '',
      detailsDialog: false,
      detailsManga: null,
    }
  },
  computed: {
    sortOptions() {
      return [
        {text: 'Relevance', value: ''},
        {text: 'Popularity', value: 'followedCount'},
        {text: 'Latest chapter', value: 'latestUploadedChapter'},
        {text: 'Recently added', value: 'createdAt'},
        {text: 'Recently updated', value: 'updatedAt'},
        {text: 'Title', value: 'title'},
        {text: 'Rating', value: 'rating'},
        {text: 'Year', value: 'year'},
      ]
    },
    currentFilterPayload() {
      return {
        t: [...(this.filterTags || [])].sort(),
        x: [...(this.filterExcludedTags || [])].sort(),
        s: [...(this.filterStatus || [])].sort(),
        r: [...(this.filterRating || [])].sort(),
        d: [...(this.filterDemographic || [])].sort(),
        a: !!this.filterAvailableOnly,
        h: !!this.filterHideFollowed,
      }
    },
    currentFilterHash() {
      return JSON.stringify(this.currentFilterPayload)
    },
    filtersDirty() {
      return this.currentFilterHash !== this.savedFiltersHash
    },
    hasFilters() {
      return this.activeFilterCount > 0
    },
    lastSearchSkippedNote() {
      const parts = []
      if (this.lastSearchSkippedFollow > 0) parts.push(`${this.lastSearchSkippedFollow} already in follow.txt`)
      if (this.lastSearchSkippedAvailable > 0) parts.push(`${this.lastSearchSkippedAvailable} without downloadable chapters`)
      return parts.length > 0 ? ` (${parts.join(', ')} hidden)` : ''
    },
    searchPageCount() {
      if (!this.searchTotal || this.searchTotal <= this.searchPageSize) return 1
      return Math.min(Math.ceil(this.searchTotal / this.searchPageSize), 417)
    },
    activeFilterCount() {
      let n = 0
      if (this.filterTags && this.filterTags.length > 0) n++
      if (this.filterExcludedTags && this.filterExcludedTags.length > 0) n++
      if (this.filterStatus && this.filterStatus.length > 0) n++
      if (this.filterRating && this.filterRating.length > 0) n++
      if (this.filterDemographic && this.filterDemographic.length > 0) n++
      if (this.filterAvailableOnly) n++
      if (this.filterHideFollowed) n++
      return n
    },
    allDownloads() {
      return this.downloads
    },
    activeDownloads() {
      return this.downloads.filter(d => d.status === 'DOWNLOADING')
    },
    pendingDownloads() {
      return this.downloads.filter(d => d.status === 'PENDING')
    },
    completedDownloads() {
      return this.downloads.filter(d => d.status === 'COMPLETED')
    },
    failedDownloads() {
      return this.downloads.filter(d => d.status === 'FAILED')
    },
    cancelledDownloads() {
      return this.downloads.filter(d => d.status === 'CANCELLED')
    },
    selectedLibrary() {
      if (this.selectedLibraryIndex === null || this.selectedLibraryIndex === undefined) return null
      return this.libraries[this.selectedLibraryIndex]
    },
    followTxtChanged() {
      return this.followTxtContent !== this.originalFollowTxtContent
    },
  },
  mounted() {
    this.loadDownloads()
    this.loadLibraries()
    this.loadSchedulerSettings()
    this.loadMangaDexPluginStatus()
    this.loadFilterDefaults()
    // Eagerly fetch the tag catalog so chips/labels in restored defaults
    // resolve to names instead of bare UUIDs without waiting for focus.
    this.loadTagsIfNeeded()
    this.setupSseListeners()
  },
  beforeDestroy() {
    this.removeSseListeners()
  },
  methods: {
    async loadDownloads() {
      this.loading = true
      try {
        const response = await this.$http.get('/api/v1/downloads')
        this.downloads = response.data
      } catch (error) {
        this.showError('Failed to load downloads: ' + error.message)
      } finally {
        this.loading = false
      }
    },
    async loadLibraries() {
      try {
        const response = await this.$komgaLibraries.getLibraries()
        this.libraries = response
        if (this.libraries.length > 0) {
          this.selectLibrary(0)
          if (!this.searchLibraryId) this.searchLibraryId = this.libraries[0].id
        }
        this.refreshFollowedUuids()
      } catch (error) {
        // Library loading failed
      }
    },

    // ── MangaDex Search (top of page) ─────────────────────────────────────
    statusColor(status) {
      return {
        ongoing: 'primary', releasing: 'primary',
        completed: 'success', ended: 'success', finished: 'success',
        hiatus: 'warning',
        cancelled: 'error', canceled: 'error',
      }[String(status || '').toLowerCase()] || 'grey'
    },
    async refreshFollowedUuids() {
      const seen = new Set()
      const errors = []
      const fetches = (this.libraries || []).map(lib =>
        this.$http.get(`/api/v1/downloads/follow-txt/${lib.id}`).then(r => {
          const content = (r.data && r.data.content) || ''
          const re = /mangadex\.org\/(?:title|manga)\/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})/gi
          let m
          while ((m = re.exec(content)) !== null) {
            seen.add(m[1].toLowerCase())
          }
        }).catch(e => {
          errors.push(`${lib && lib.name}: ${(e && e.message) || 'unknown'}`)
        }),
      )
      await Promise.all(fetches)
      this.followedUuids = Array.from(seen)
      this.followedUuidsLoadError = errors.length > 0 ? errors.join('; ') : ''
    },
    isFollowed(manga) {
      return this.followedUuids.indexOf(String(manga.externalId || '').toLowerCase()) >= 0
    },
    async toggleFollow(manga) {
      if (!this.searchLibraryId) { this.showError('Select a target library first'); return }
      const k = manga.externalId + ':fl'
      this.$set(this.searchBusy, k, true)
      this.$set(this.searchFollow, manga.externalId, null)
      try {
        const url = `https://mangadex.org/title/${manga.externalId}`
        const uuidLower = String(manga.externalId || '').toLowerCase()
        const wantUnfollow = this.isFollowed(manga)
        if (wantUnfollow) {
          // Find which library contains the URL and remove the line there.
          const lineRe = new RegExp(`^\\s*https?:\\/\\/mangadex\\.org\\/title\\/${uuidLower}(?:[\\/?#].*)?\\s*$`, 'i')
          let removedAnywhere = false
          for (const lib of this.libraries) {
            const current = await this.$http.get(`/api/v1/downloads/follow-txt/${lib.id}`)
            const existing = (current.data && current.data.content) || ''
            const lines = existing.split(/\r?\n/)
            const kept = lines.filter(l => !lineRe.test(l))
            if (kept.length !== lines.length) {
              const next = kept.join('\n').replace(/\n+$/, '') + (kept.some(l => l.trim()) ? '\n' : '')
              await this.$http.put(`/api/v1/downloads/follow-txt/${lib.id}`, { content: next })
              removedAnywhere = true
            }
          }
          if (removedAnywhere) {
            this.followedUuids = this.followedUuids.filter(u => u !== uuidLower)
            this.showSuccess(`Removed from follow.txt: ${manga.title}`)
          } else {
            this.showError(`Could not find ${manga.title} in any follow.txt`)
          }
        } else {
          const current = await this.$http.get(`/api/v1/downloads/follow-txt/${this.searchLibraryId}`)
          const existing = (current.data && current.data.content) || ''
          const next = existing.trimEnd() + (existing.trim() ? '\n' : '') + url + '\n'
          await this.$http.put(`/api/v1/downloads/follow-txt/${this.searchLibraryId}`, { content: next })
          this.followedUuids = this.followedUuids.concat([uuidLower])
          this.showSuccess(`Added to follow.txt: ${manga.title}`)
        }
      } catch (e) {
        this.$set(this.searchFollow, manga.externalId, 'error')
        this.showError('Follow toggle failed: ' + (e?.response?.data?.message || e.message))
      } finally {
        this.$set(this.searchBusy, k, false)
      }
    },
    showMangaDetails(manga) {
      this.detailsManga = manga
      this.detailsDialog = true
    },
    onSortChange() {
      if (this.searchDone || this.searchQuery || this.hasFilters) {
        this.searchPage = 1
        this.searchMangaDex()
      }
    },
    async loadFilterDefaults() {
      let raw = null
      try {
        const settings = await this.$komgaSettings.getClientSettingsUser()
        const entry = settings && settings[this.FILTER_DEFAULTS_KEY]
        if (entry && entry.value) raw = entry.value
      } catch (_) {
        // account settings unavailable — fall through to localStorage migration
      }
      // Migration: pick up a value previously saved in this browser's localStorage
      if (!raw) {
        try {
          raw = localStorage.getItem('komga-fork.mangadex-search-defaults')
        } catch (_) { /* ignore */ }
      }
      if (raw) {
        try {
          const v = JSON.parse(raw) || {}
          this.filterTags = Array.isArray(v.t) ? v.t : []
          this.filterExcludedTags = Array.isArray(v.x) ? v.x : []
          this.filterStatus = Array.isArray(v.s) ? v.s : []
          this.filterRating = Array.isArray(v.r) ? v.r : []
          this.filterDemographic = Array.isArray(v.d) ? v.d : []
          this.filterAvailableOnly = !!v.a
          this.filterHideFollowed = !!v.h
        } catch (_) {
          // ignore corrupt value
        }
      }
      this.savedFiltersHash = this.currentFilterHash
    },
    async saveFilterDefaults() {
      try {
        await this.$komgaSettings.updateClientSettingUser({
          [this.FILTER_DEFAULTS_KEY]: { value: JSON.stringify(this.currentFilterPayload) },
        })
        this.savedFiltersHash = this.currentFilterHash
        // Drop any stale per-browser copy now that it lives in the account
        try {
          localStorage.removeItem('komga-fork.mangadex-search-defaults')
        } catch (_) { /* ignore */ }
        this.showSuccess('Filters saved as default for your account')
      } catch (e) {
        this.showError('Failed to save defaults: ' + e.message)
      }
    },
    clearFilters() {
      this.filterTags = []
      this.filterExcludedTags = []
      this.filterStatus = []
      this.filterRating = []
      this.filterDemographic = []
      this.filterAvailableOnly = false
      this.filterHideFollowed = false
    },
    async fetchPreferredLanguage() {
      // gallery-dl Downloader's default_language is the closest thing to
      // "what language do I want?" Default to 'en' if not configured.
      try {
        const r = await this.$http.get('/api/v1/plugins/gallery-dl-downloader/config')
        return (r.data && r.data.default_language) || 'en'
      } catch (_) {
        return 'en'
      }
    },
    async loadTagsIfNeeded() {
      if (this.tagOptions.length > 0 || this.loadingTags) return
      const cacheKey = 'komga-fork.mangadex-tags-cache'
      const ttlMs = 7 * 24 * 60 * 60 * 1000
      try {
        const raw = localStorage.getItem(cacheKey)
        if (raw) {
          const cached = JSON.parse(raw)
          if (cached && Array.isArray(cached.tags) && (Date.now() - cached.t) < ttlMs) {
            this.tagOptions = cached.tags
            return
          }
        }
      } catch (_) { /* ignore */ }
      this.loadingTags = true
      try {
        const r = await this.$http.get('/api/v1/plugins/mangadex-metadata/tags')
        this.tagOptions = r.data || []
        try {
          localStorage.setItem(cacheKey, JSON.stringify({ t: Date.now(), tags: this.tagOptions }))
        } catch (_) { /* ignore */ }
      } catch (e) {
        this.showError('Failed to load MangaDex tag list: ' + (e?.response?.data?.message || e.message))
      } finally {
        this.loadingTags = false
      }
    },
    async searchMangaDex(resetPage = true) {
      const q = (this.searchQuery || '').trim()
      if (!q && !this.hasFilters) return
      if (resetPage) this.searchPage = 1
      this.searchLoading = true
      this.searchDone = false
      this.searchResults = []
      this.searchBusy = {}
      this.searchAction = {}
      this.searchFollow = {}
      this.lastSearchSkippedAvailable = 0
      this.lastSearchSkippedFollow = 0
      try {
        const followedPromise = this.refreshFollowedUuids()
        const offset = (this.searchPage - 1) * this.searchPageSize
        const resp = await this.$http.post('/api/v1/plugins/mangadex-metadata/search-advanced', {
          query: q || null,
          includedTagIds: this.filterTags,
          excludedTagIds: this.filterExcludedTags,
          status: this.filterStatus,
          contentRating: this.filterRating,
          publicationDemographic: this.filterDemographic,
          hasAvailableChapters: this.filterAvailableOnly || null,
          offset,
          limit: this.searchPageSize,
          order: this.searchOrder || null,
          orderDir: this.searchOrderDir || null,
        })
        const page = resp.data || {}
        let filtered = page.data || []
        this.searchTotal = page.total || 0
        await followedPromise
        if (this.filterHideFollowed && filtered.length > 0) {
          const before = filtered.length
          filtered = filtered.filter(r => !this.isFollowed(r))
          this.lastSearchSkippedFollow = before - filtered.length
        }
        if (this.filterAvailableOnly && filtered.length > 0) {
          const beforeAvail = filtered.length
          try {
            const lang = await this.fetchPreferredLanguage()
            const check = await this.$http.post('/api/v1/plugins/mangadex-metadata/downloadable-check', {
              language: lang,
              ids: filtered.map(r => r.externalId),
            })
            const map = check.data || {}
            filtered = filtered.filter(r => map[r.externalId] === true)
            this.lastSearchSkippedAvailable = beforeAvail - filtered.length
          } catch (e) {
            this.showError('Downloadable-check failed: ' + (e?.response?.data?.message || e.message))
          }
        }
        this.searchResults = filtered
        this.searchDone = true
      } catch (e) {
        const msg = e?.response?.data?.message || e.message
        this.showError('Search failed: ' + msg + ' (is the MangaDex Metadata plugin enabled?)')
      } finally {
        this.searchLoading = false
      }
    },
    onSearchPageChange(page) {
      this.searchPage = page
      this.searchMangaDex(false)
    },
    async downloadFromSearch(manga) {
      if (!this.searchLibraryId) { this.showError('Select a target library first'); return }
      const k = manga.externalId + ':dl'
      this.$set(this.searchBusy, k, true)
      try {
        await this.$http.post('/api/v1/downloads', {
          sourceUrl: `https://mangadex.org/title/${manga.externalId}`,
          libraryId: this.searchLibraryId,
          priority: 5,
        })
        this.$set(this.searchAction, manga.externalId, 'downloaded')
        this.showSuccess(`Queued: ${manga.title}`)
      } catch (e) {
        this.$set(this.searchAction, manga.externalId, 'error')
        this.showError('Failed to queue: ' + (e?.response?.data?.message || e.message))
      } finally {
        this.$set(this.searchBusy, k, false)
      }
    },
    selectLibrary(index) {
      this.selectedLibraryIndex = index
      this.loadFollowTxt()
    },
    async loadFollowTxt() {
      if (!this.selectedLibrary) return
      this.loadingFollowTxt = true
      try {
        const response = await this.$http.get(`/api/v1/downloads/follow-txt/${this.selectedLibrary.id}`)
        this.followTxtContent = response.data.content || ''
        this.originalFollowTxtContent = this.followTxtContent
      } catch (error) {
        // File might not exist yet
        this.followTxtContent = ''
        this.originalFollowTxtContent = ''
      } finally {
        this.loadingFollowTxt = false
      }
    },
    async saveFollowTxt() {
      if (!this.selectedLibrary) return
      this.savingFollowTxt = true
      try {
        await this.$http.put(`/api/v1/downloads/follow-txt/${this.selectedLibrary.id}`, {
          content: this.followTxtContent,
        })
        this.originalFollowTxtContent = this.followTxtContent
        this.showSuccess('follow.txt saved')
      } catch (error) {
        this.showError('Failed to save follow.txt: ' + error.message)
      } finally {
        this.savingFollowTxt = false
      }
    },
    async checkNow() {
      if (!this.selectedLibrary) return
      this.checkingNow = true
      try {
        await this.$http.post(`/api/v1/downloads/follow-txt/${this.selectedLibrary.id}/check-now`)
        this.showSuccess('Scan started — new chapters will appear automatically.')
      } catch (error) {
        this.showError('Failed to trigger check: ' + error.message)
      } finally {
        this.checkingNow = false
      }
    },
    async syncToMangaDex() {
      if (!this.selectedLibrary) return
      this.syncingToMangaDex = true
      try {
        const response = await this.$http.post(`/api/v1/downloads/follow-txt/${this.selectedLibrary.id}/sync-to-mangadex`)
        this.showSuccess(response.data.message || `Synced ${response.data.followed}/${response.data.total} manga to MangaDex`)
      } catch (error) {
        const msg = error.response?.data?.error || error.message
        this.showError('Sync failed: ' + msg)
      } finally {
        this.syncingToMangaDex = false
      }
    },
    async loadMangaDexPluginStatus() {
      try {
        const response = await this.$http.get('/api/v1/plugins/mangadex-subscription')
        this.mangaDexPluginEnabled = response.data.enabled
      } catch (_) {
        this.mangaDexPluginEnabled = false
      }
    },
    async loadSchedulerSettings() {
      try {
        const response = await this.$http.get('/api/v1/downloads/scheduler')
        this.schedulerEnabled = response.data.enabled
        this.schedulerInterval = response.data.intervalHours || 6
        this.scheduleMode = response.data.scheduleMode || 'interval'
        this.checkTime = response.data.checkTime || '03:00'
      } catch (error) {
        // Default values are fine
      }
    },
    async saveSchedulerSettings() {
      this.savingScheduler = true
      try {
        await this.$http.post('/api/v1/downloads/scheduler', {
          enabled: this.schedulerEnabled,
          intervalHours: this.schedulerInterval,
          scheduleMode: this.scheduleMode,
          checkTime: this.scheduleMode === 'fixed_time' ? this.checkTime : null,
        })
        this.showSuccess('Scheduler settings saved')
      } catch (error) {
        this.showError('Failed to save scheduler settings: ' + error.message)
      } finally {
        this.savingScheduler = false
      }
    },
    async addDownload() {
      if (!this.newDownload.libraryId) {
        this.showError('Please select a target library')
        return
      }
      this.adding = true
      try {
        await this.$http.post('/api/v1/downloads', this.newDownload)
        this.showSuccess('Download added to queue')
        this.newDownloadDialog = false
        this.newDownload = { sourceUrl: '', libraryId: null, priority: 5 }
        await this.loadDownloads()
      } catch (error) {
        this.showError('Failed to add download: ' + error.message)
      } finally {
        this.adding = false
      }
    },
    async pauseDownload(download) {
      try {
        await this.$http.post(`/api/v1/downloads/${download.id}/action`, { action: 'pause' })
        this.showSuccess('Download paused')
        await this.loadDownloads()
      } catch (error) {
        this.showError('Failed to pause download: ' + error.message)
      }
    },
    async resumeDownload(download) {
      try {
        await this.$http.post(`/api/v1/downloads/${download.id}/action`, { action: 'resume' })
        this.showSuccess('Download resumed')
        await this.loadDownloads()
      } catch (error) {
        this.showError('Failed to resume download: ' + error.message)
      }
    },
    async cancelDownload(download) {
      try {
        await this.$http.post(`/api/v1/downloads/${download.id}/action`, { action: 'cancel' })
        this.showSuccess('Download cancelled')
        await this.loadDownloads()
      } catch (error) {
        this.showError('Failed to cancel download: ' + error.message)
      }
    },
    async deleteDownload(download) {
      try {
        await this.$http.delete(`/api/v1/downloads/${download.id}`)
        this.showSuccess('Download deleted from queue')
        await this.loadDownloads()
      } catch (error) {
        this.showError('Failed to delete download: ' + error.message)
      }
    },
    handleAction({ download, action }) {
      switch (action) {
        case 'pause':
          this.pauseDownload(download)
          break
        case 'resume':
          this.resumeDownload(download)
          break
        case 'cancel':
          this.cancelDownload(download)
          break
        case 'retry':
          this.resumeDownload(download)
          break
        case 'delete':
          this.deleteDownload(download)
          break
      }
    },
    async clearByStatus(status) {
      try {
        const response = await this.$http.delete(`/api/v1/downloads/clear/${status}`)
        this.showSuccess(response.data.message || `Cleared ${status} downloads`)
        await this.loadDownloads()
      } catch (error) {
        this.showError('Failed to clear downloads: ' + error.message)
      }
    },
    showSuccess(message) {
      this.snackbarText = message
      this.snackbarColor = 'success'
      this.snackbar = true
    },
    showError(message) {
      this.snackbarText = message
      this.snackbarColor = 'error'
      this.snackbar = true
    },
    // SSE event handlers (using Komga's existing SSE infrastructure)
    setupSseListeners() {
      this.$eventHub.$on(DOWNLOAD_STARTED, this.onDownloadStarted)
      this.$eventHub.$on(DOWNLOAD_PROGRESS, this.onDownloadProgress)
      this.$eventHub.$on(DOWNLOAD_COMPLETED, this.onDownloadCompleted)
      this.$eventHub.$on(DOWNLOAD_FAILED, this.onDownloadFailed)
    },
    removeSseListeners() {
      this.$eventHub.$off(DOWNLOAD_STARTED, this.onDownloadStarted)
      this.$eventHub.$off(DOWNLOAD_PROGRESS, this.onDownloadProgress)
      this.$eventHub.$off(DOWNLOAD_COMPLETED, this.onDownloadCompleted)
      this.$eventHub.$off(DOWNLOAD_FAILED, this.onDownloadFailed)
    },
    onDownloadStarted(data) {
      this.showSuccess(`Download started: ${data.title || data.sourceUrl}`)
      this.updateDownloadFromSse(data)
    },
    onDownloadProgress(data) {
      this.updateDownloadFromSse(data)
    },
    onDownloadCompleted(data) {
      this.showSuccess(`Download completed: ${data.title}`)
      this.updateDownloadFromSse(data)
    },
    onDownloadFailed(data) {
      this.showError(`Download failed: ${data.title} - ${data.errorMessage}`)
      this.updateDownloadFromSse(data)
    },
    updateDownloadFromSse(data) {
      if (!data.downloadId) return

      const index = this.downloads.findIndex(d => d.id === data.downloadId)
      if (index !== -1) {
        // Update existing download reactively
        this.$set(this.downloads, index, {
          ...this.downloads[index],
          status: data.status,
          progressPercent: data.progressPercent ?? this.downloads[index].progressPercent,
          currentChapter: data.currentChapter ?? this.downloads[index].currentChapter,
          totalChapters: data.totalChapters ?? this.downloads[index].totalChapters,
          errorMessage: data.errorMessage ?? this.downloads[index].errorMessage,
        })
      } else {
        // New download - reload full list
        this.loadDownloads()
      }
    },
    // Tachiyomi Import
    async importTachiyomi() {
      if (!this.tachiyomiFile || !this.tachiyomiLibraryId) {
        this.showError('Please select a backup file and target library')
        return
      }
      this.importingTachiyomi = true
      this.tachiyomiResult = null
      try {
        const result = await this.$komgaImport.importTachiyomi(this.tachiyomiFile, this.tachiyomiLibraryId)
        this.tachiyomiResult = result
        if (result.importedCount > 0) {
          this.showSuccess(`Imported ${result.importedCount} manga from Tachiyomi backup`)
          // Refresh follow.txt if we're on the config tab viewing the same library
          if (this.selectedLibrary && this.selectedLibrary.id === this.tachiyomiLibraryId) {
            this.loadFollowTxt()
          }
        } else if (result.skippedCount > 0) {
          this.showSuccess('All manga already exist in follow.txt')
        } else {
          this.showError('No MangaDex manga found in backup')
        }
      } catch (error) {
        this.showError(error.message || 'Failed to import Tachiyomi backup')
      } finally {
        this.importingTachiyomi = false
      }
    },
  },
}
</script>
