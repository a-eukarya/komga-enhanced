<template>
  <div>
    <v-container fluid>
      <v-row>
        <v-col>
          <h1 class="text-h4 mb-4">{{ $t('plugin_manager.title') }}</h1>
          <p class="text-subtitle-1">{{ $t('plugin_manager.subtitle') }}</p>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="12">
          <v-card>
            <v-card-title>
              {{ $t('plugin_manager.installed_plugins') }}
              <v-spacer></v-spacer>
              <v-btn color="primary" @click="installDialog = true">
                <v-icon left>mdi-plus</v-icon>
                {{ $t('plugin_manager.install_plugin') }}
              </v-btn>
              <v-btn icon @click="loadPlugins" :loading="loading" class="ml-2">
                <v-icon>mdi-refresh</v-icon>
              </v-btn>
            </v-card-title>

            <v-card-text>
              <v-alert v-if="plugins.length === 0 && !loading" type="info" text>
                {{ $t('plugin_manager.no_plugins') }}
              </v-alert>

              <v-data-table
                v-else
                :headers="headers"
                :items="plugins"
                :loading="loading"
                :items-per-page="itemsPerPage"
                @update:items-per-page="onItemsPerPageChange"
                class="elevation-1"
                :footer-props="{
                  itemsPerPageOptions: [10, 20, 50, 100],
                }"
              >
                <template v-slot:item.enabled="{ item }">
                  <v-switch
                    v-model="item.enabled"
                    @change="togglePlugin(item)"
                    :loading="toggling === item.id"
                    dense
                    hide-details
                  ></v-switch>
                </template>

                <template v-slot:item.pluginType="{ item }">
                  <v-chip small :color="getTypeColor(item.pluginType)">
                    {{ item.pluginType }}
                  </v-chip>
                </template>

                <template v-slot:item.actions="{ item }">
                  <v-tooltip bottom>
                    <template v-slot:activator="{ on }">
                      <v-btn icon small v-on="on" @click="showConfig(item)">
                        <v-icon small>mdi-cog</v-icon>
                      </v-btn>
                    </template>
                    <span>{{ $t('plugin_manager.configure') }}</span>
                  </v-tooltip>

                  <v-tooltip bottom>
                    <template v-slot:activator="{ on }">
                      <v-btn icon small v-on="on" @click="showLogs(item)" color="info">
                        <v-icon small>mdi-text-box</v-icon>
                      </v-btn>
                    </template>
                    <span>{{ $t('plugin_manager.view_logs') }}</span>
                  </v-tooltip>

                  <v-tooltip v-if="item.external" bottom>
                    <template v-slot:activator="{ on }">
                      <v-btn icon small v-on="on" @click="confirmUninstall(item)" color="error">
                        <v-icon small>mdi-delete</v-icon>
                      </v-btn>
                    </template>
                    <span>{{ $t('plugin_manager.uninstall') }}</span>
                  </v-tooltip>
                  <v-tooltip v-else bottom>
                    <template v-slot:activator="{ on }">
                      <span v-on="on">
                        <v-btn icon small disabled>
                          <v-icon small>mdi-lock</v-icon>
                        </v-btn>
                      </span>
                    </template>
                    <span>{{ $t('plugin_manager.builtin_locked') }}</span>
                  </v-tooltip>
                </template>
              </v-data-table>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>

    <!-- Install Dialog -->
    <v-dialog v-model="installDialog" max-width="600" :fullscreen="$vuetify.breakpoint.xsOnly">
      <v-card>
        <v-card-title>{{ $t('plugin_manager.dialog_install_title') }}</v-card-title>
        <v-card-text>
          <v-alert type="info" text class="mb-4">
            {{ $t('plugin_manager.dialog_install_info') }}
          </v-alert>

          <v-file-input
            v-model="pluginFile"
            :label="$t('plugin_manager.field_plugin_file')"
            accept=".jar"
            outlined
            prepend-icon="mdi-file"
          ></v-file-input>

          <v-text-field
            v-model="pluginUrl"
            :label="$t('plugin_manager.field_plugin_url')"
            outlined
            prepend-icon="mdi-link"
          ></v-text-field>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="installDialog = false">{{ $t('download_manager.cancel') }}</v-btn>
          <v-btn color="primary" @click="installPlugin" :loading="installing">
            {{ $t('plugin_manager.install') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Uninstall Confirmation -->
    <v-dialog v-model="uninstallDialog" max-width="500" :fullscreen="$vuetify.breakpoint.xsOnly">
      <v-card>
        <v-card-title class="headline">{{ $t('plugin_manager.dialog_uninstall_title') }}</v-card-title>
        <v-card-text>
          {{ $t('plugin_manager.dialog_uninstall_confirm') }} <strong>{{ selectedPlugin?.name }}</strong>?
          <br>{{ $t('plugin_manager.dialog_uninstall_info') }}
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="uninstallDialog = false">{{ $t('download_manager.cancel') }}</v-btn>
          <v-btn color="error" text @click="uninstallPlugin" :loading="uninstalling">
            {{ $t('plugin_manager.uninstall') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Config Dialog -->
    <v-dialog v-model="configDialog" max-width="800" :fullscreen="$vuetify.breakpoint.xsOnly">
      <v-card>
        <v-card-title>
          {{ $t('plugin_manager.configure') }} {{ selectedPlugin?.name }}
        </v-card-title>
        <v-card-text>
          <v-alert v-if="selectedPlugin?.description" type="info" text class="mb-4">
            {{ selectedPlugin.description }}
          </v-alert>

          <v-form ref="configForm">
            <template v-if="isAutoMetadata">
              <div class="subtitle-2 font-weight-bold mb-1">{{ $t('plugin_manager.am_provider_priority') }}</div>
              <div class="caption text--secondary mb-2">{{ getSchemaField('provider_priority').description }}</div>
              <v-list dense outlined rounded class="mb-2">
                <v-list-item v-if="providerPriorityList.length === 0">
                  <v-list-item-content class="caption text--secondary">{{ $t('plugin_manager.am_no_providers') }}</v-list-item-content>
                </v-list-item>
                <v-list-item v-for="(p, i) in providerPriorityList" :key="p">
                  <v-avatar size="24" color="grey lighten-2" class="me-3 justify-center">
                    <span class="caption font-weight-medium">{{ i + 1 }}</span>
                  </v-avatar>
                  <v-list-item-content>{{ providerLabel(p) }}</v-list-item-content>
                  <v-list-item-action class="flex-row align-center ma-0">
                    <v-btn icon small :disabled="i === 0" @click="moveProvider(i, -1)">
                      <v-icon small>mdi-arrow-up</v-icon>
                    </v-btn>
                    <v-btn icon small :disabled="i === providerPriorityList.length - 1" @click="moveProvider(i, 1)">
                      <v-icon small>mdi-arrow-down</v-icon>
                    </v-btn>
                    <v-btn icon small @click="removeProvider(i)">
                      <v-icon small color="error">mdi-close</v-icon>
                    </v-btn>
                  </v-list-item-action>
                </v-list-item>
              </v-list>
              <v-select
                v-if="availableProvidersToAdd.length"
                v-model="providerToAdd"
                :items="availableProvidersToAdd"
                item-text="name"
                item-value="id"
                :label="$t('plugin_manager.am_add_provider')"
                outlined
                dense
                hide-details
                class="mb-4"
                @change="addProvider"
              ></v-select>

              <v-select
                v-model="excludedLibraryIdsList"
                :items="autoLibraries"
                item-text="name"
                item-value="id"
                :label="getSchemaField('exclude_library_ids').title || 'Excluded libraries'"
                :hint="getSchemaField('exclude_library_ids').description"
                persistent-hint
                multiple
                chips
                small-chips
                deletable-chips
                outlined
                dense
                class="mb-4"
              ></v-select>
            </template>

            <template v-for="key in genericConfigKeys">
              <v-select
                v-if="getSchemaField(key).enum || getSchemaField(key).dynamicEnum"
                :key="key"
                v-model="pluginConfig[key]"
                :items="getSchemaField(key).enum || []"
                :label="getSchemaField(key).title || formatConfigKey(key)"
                :hint="getSchemaField(key).description"
                :clearable="!!getSchemaField(key).dynamicEnum"
                persistent-hint
                outlined
                dense
                class="mb-2"
              ></v-select>
              <v-text-field
                v-else
                :key="key"
                v-model="pluginConfig[key]"
                :label="getSchemaField(key).title || formatConfigKey(key)"
                :hint="getSchemaField(key).description"
                :persistent-hint="!!getSchemaField(key).description"
                :type="getSchemaField(key).format === 'password' || key.includes('password') || key.includes('secret') ? 'password' : 'text'"
                outlined
                dense
                class="mb-2"
              ></v-text-field>
            </template>
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="configDialog = false">{{ $t('download_manager.cancel') }}</v-btn>
          <v-btn color="primary" @click="saveConfig" :loading="savingConfig">
            {{ $t('plugin_manager.save') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Logs Dialog -->
    <v-dialog v-model="logsDialog" max-width="1200" scrollable :fullscreen="$vuetify.breakpoint.xsOnly">
      <v-card>
        <v-card-title>
          {{ selectedPlugin?.name }} {{ $t('plugin_manager.logs_title') }}
          <v-spacer></v-spacer>
          <v-btn icon @click="loadPluginLogs(selectedPlugin)" :loading="loadingLogs">
            <v-icon>mdi-refresh</v-icon>
          </v-btn>
          <v-btn icon @click="clearLogs" color="error" :loading="clearingLogs">
            <v-icon>mdi-delete</v-icon>
          </v-btn>
        </v-card-title>

        <v-card-subtitle>
          <v-chip-group v-model="logLevelFilter" mandatory>
            <v-chip small filter value="">{{ $t('plugin_manager.log_all') }}</v-chip>
            <v-chip small filter value="DEBUG">{{ $t('plugin_manager.log_debug') }}</v-chip>
            <v-chip small filter value="INFO" color="info">{{ $t('plugin_manager.log_info') }}</v-chip>
            <v-chip small filter value="WARN" color="warning">{{ $t('plugin_manager.log_warn') }}</v-chip>
            <v-chip small filter value="ERROR" color="error">{{ $t('plugin_manager.log_error') }}</v-chip>
          </v-chip-group>
        </v-card-subtitle>

        <v-divider></v-divider>

        <v-card-text style="max-height: 600px;">
          <v-alert v-if="pluginLogs.length === 0 && !loadingLogs" type="info" text>
            {{ $t('plugin_manager.no_logs') }}
          </v-alert>

          <v-timeline v-else dense>
            <v-timeline-item
              v-for="log in filteredLogs"
              :key="log.id"
              :color="getLogColor(log.logLevel)"
              small
              fill-dot
            >
              <template v-slot:icon>
                <v-icon small dark>{{ getLogIcon(log.logLevel) }}</v-icon>
              </template>

              <v-card flat>
                <v-card-subtitle class="py-1">
                  <v-chip x-small :color="getLogColor(log.logLevel)">
                    {{ log.logLevel }}
                  </v-chip>
                  <span class="text-caption ml-2">{{ formatDate(log.createdDate) }}</span>
                </v-card-subtitle>
                <v-card-text class="py-2">
                  {{ log.message }}
                  <v-expansion-panels v-if="log.exceptionTrace" flat class="mt-2">
                    <v-expansion-panel>
                      <v-expansion-panel-header class="py-0 px-0">
                        <span class="text-caption error--text">{{ $t('plugin_manager.stack_trace') }}</span>
                      </v-expansion-panel-header>
                      <v-expansion-panel-content>
                        <pre class="text-caption">{{ log.exceptionTrace }}</pre>
                      </v-expansion-panel-content>
                    </v-expansion-panel>
                  </v-expansion-panels>
                </v-card-text>
              </v-card>
            </v-timeline-item>
          </v-timeline>
        </v-card-text>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="logsDialog = false">{{ $t('download_manager.close') }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Snackbar -->
    <v-snackbar v-model="snackbar" :color="snackbarColor" :timeout="3000" bottom>
      {{ snackbarText }}
      <template v-slot:action="{ attrs }">
        <v-btn text v-bind="attrs" @click="snackbar = false">{{ $t('download_manager.close') }}</v-btn>
      </template>
    </v-snackbar>
  </div>
</template>

<script>
export default {
  name: 'PluginManager',
  data() {
    return {
      itemsPerPage: this.$store?.state?.persistedState?.dataTablePageSize || 10,
      plugins: [],
      loading: false,
      toggling: null,
      installing: false,
      uninstalling: false,
      installDialog: false,
      uninstallDialog: false,
      configDialog: false,
      logsDialog: false,
      selectedPlugin: null,
      pluginFile: null,
      pluginUrl: '',
      pluginConfig: {},
      parsedSchema: null,
      savingConfig: false,
      autoProviders: [],
      autoLibraries: [],
      providerPriorityList: [],
      excludedLibraryIdsList: [],
      providerToAdd: null,
      pluginLogs: [],
      loadingLogs: false,
      clearingLogs: false,
      logLevelFilter: '',
      snackbar: false,
      snackbarText: '',
      snackbarColor: 'success',
    }
  },
  computed: {
    headers() {
      return [
        { text: this.$t('plugin_manager.header_name'), value: 'name' },
        { text: this.$t('plugin_manager.header_version'), value: 'version' },
        { text: this.$t('plugin_manager.header_type'), value: 'pluginType' },
        { text: this.$t('plugin_manager.header_author'), value: 'author' },
        { text: this.$t('plugin_manager.header_enabled'), value: 'enabled' },
        { text: this.$t('plugin_manager.header_actions'), value: 'actions', sortable: false },
      ]
    },
    filteredLogs() {
      if (!this.logLevelFilter) return this.pluginLogs
      return this.pluginLogs.filter(log => log.logLevel === this.logLevelFilter)
    },
    configKeys() {
      if (this.parsedSchema?.properties) {
        return Object.keys(this.parsedSchema.properties)
      }
      return Object.keys(this.pluginConfig || {})
    },
    isAutoMetadata() {
      return this.selectedPlugin?.id === 'auto-metadata'
    },
    genericConfigKeys() {
      if (!this.isAutoMetadata) return this.configKeys
      return this.configKeys.filter(k => k !== 'provider_priority' && k !== 'exclude_library_ids')
    },
    availableProvidersToAdd() {
      return this.autoProviders.filter(p => !this.providerPriorityList.includes(p.id))
    },
  },
  mounted() {
    const savedPageSize = this.$store?.state?.persistedState?.dataTablePageSize
    if (savedPageSize) this.itemsPerPage = savedPageSize
    this.loadPlugins()
  },
  methods: {
    onItemsPerPageChange(val) {
      this.itemsPerPage = val
      this.$store.commit('setDataTablePageSize', val)
    },
    async loadPlugins() {
      this.loading = true
      try {
        const response = await this.$http.get('/api/v1/plugins')
        this.plugins = response.data
      } catch (error) {
        this.showError(this.$t('plugin_manager.snack_load_failed') + ': ' + error.message)
      } finally {
        this.loading = false
      }
    },
    async togglePlugin(plugin) {
      this.toggling = plugin.id
      try {
        await this.$http.patch(`/api/v1/plugins/${plugin.id}`, { enabled: plugin.enabled })
        this.showSuccess(plugin.enabled ? this.$t('plugin_manager.snack_enabled') : this.$t('plugin_manager.snack_disabled'))
      } catch (error) {
        plugin.enabled = !plugin.enabled
        this.showError(this.$t('plugin_manager.snack_toggle_failed') + ': ' + error.message)
      } finally {
        this.toggling = null
      }
    },
    async installPlugin() {
      if (!this.pluginFile && !this.pluginUrl) {
        this.showError(this.$t('plugin_manager.snack_install_failed') + ': ' + this.$t('download_manager.cancel'))
        return
      }
      this.installing = true
      try {
        const form = new FormData()
        if (this.pluginFile) form.append('file', this.pluginFile)
        if (this.pluginUrl) form.append('url', this.pluginUrl)
        await this.$http.post('/api/v1/plugins/install', form, {
          headers: {'Content-Type': 'multipart/form-data'},
        })
        this.showSuccess(this.$t('plugin_manager.snack_installed'))
        this.installDialog = false
        this.pluginFile = null
        this.pluginUrl = ''
        await this.loadPlugins()
      } catch (error) {
        const msg = error.response?.data?.message || error.message
        this.showError(this.$t('plugin_manager.snack_install_failed') + ': ' + msg)
      } finally {
        this.installing = false
      }
    },
    confirmUninstall(plugin) {
      this.selectedPlugin = plugin
      this.uninstallDialog = true
    },
    async uninstallPlugin() {
      this.uninstalling = true
      try {
        await this.$http.delete(`/api/v1/plugins/${this.selectedPlugin.id}`)
        this.showSuccess(this.$t('plugin_manager.snack_uninstalled'))
        this.uninstallDialog = false
        await this.loadPlugins()
      } catch (error) {
        this.showError(this.$t('plugin_manager.snack_uninstall_failed') + ': ' + error.message)
      } finally {
        this.uninstalling = false
      }
    },
    async showConfig(plugin) {
      this.selectedPlugin = plugin
      try {
        const response = await this.$http.get(`/api/v1/plugins/${plugin.id}/config`)
        this.pluginConfig = response.data || {}

        this.parsedSchema = null
        if (plugin.configSchema) {
          try {
            this.parsedSchema = JSON.parse(plugin.configSchema)
            if (this.parsedSchema.properties) {
              Object.keys(this.parsedSchema.properties).forEach(key => {
                if (!(key in this.pluginConfig)) {
                  this.pluginConfig[key] = this.parsedSchema.properties[key].default != null
                    ? String(this.parsedSchema.properties[key].default)
                    : ''
                }
              })
              await this.resolveDynamicEnums(this.parsedSchema.properties)
            }
          } catch (e) {
            this.parsedSchema = null
          }
        }

        if (this.isAutoMetadata) await this.loadAutoMatchOptions()

        this.configDialog = true
      } catch (error) {
        this.showError(this.$t('plugin_manager.snack_config_load_failed') + ': ' + error.message)
      }
    },
    async loadAutoMatchOptions() {
      // built-in + external METADATA plugins become selectable providers (short id, no -metadata suffix)
      this.autoProviders = this.plugins
        .filter(p => p.pluginType === 'METADATA' && p.id !== 'auto-metadata')
        .map(p => ({ id: p.id.replace(/-metadata$/, ''), name: p.name }))
      try {
        this.autoLibraries = await this.$komgaLibraries.getLibraries()
      } catch (e) {
        this.autoLibraries = []
      }
      const priorityCsv = this.pluginConfig.provider_priority || 'anilist,mangadex,kitsu'
      this.providerPriorityList = priorityCsv
        .split(',')
        .map(s => s.trim().toLowerCase().replace(/-metadata$/, ''))
        .filter(s => s)
      this.excludedLibraryIdsList = (this.pluginConfig.exclude_library_ids || '')
        .split(',')
        .map(s => s.trim())
        .filter(s => s)
      this.providerToAdd = null
    },
    providerLabel(shortId) {
      const p = this.autoProviders.find(x => x.id === shortId)
      return p ? p.name : shortId
    },
    moveProvider(index, direction) {
      const target = index + direction
      if (target < 0 || target >= this.providerPriorityList.length) return
      const list = this.providerPriorityList.slice()
      const [item] = list.splice(index, 1)
      list.splice(target, 0, item)
      this.providerPriorityList = list
    },
    removeProvider(index) {
      this.providerPriorityList = this.providerPriorityList.filter((_, i) => i !== index)
    },
    addProvider(id) {
      if (id && !this.providerPriorityList.includes(id)) {
        this.providerPriorityList = this.providerPriorityList.concat([id])
      }
      this.$nextTick(() => { this.providerToAdd = null })
    },
    async saveConfig() {
      this.savingConfig = true
      try {
        if (this.isAutoMetadata) {
          this.pluginConfig.provider_priority = this.providerPriorityList.join(',')
          this.pluginConfig.exclude_library_ids = this.excludedLibraryIdsList.join(',')
        }
        await this.$http.post(`/api/v1/plugins/${this.selectedPlugin.id}/config`, this.pluginConfig)
        this.showSuccess(this.$t('plugin_manager.snack_config_saved'))
        this.configDialog = false
      } catch (error) {
        this.showError(this.$t('plugin_manager.snack_config_save_failed') + ': ' + error.message)
      } finally {
        this.savingConfig = false
      }
    },
    async showLogs(plugin) {
      this.selectedPlugin = plugin
      this.logsDialog = true
      await this.loadPluginLogs(plugin)
    },
    async loadPluginLogs(plugin) {
      if (!plugin) return
      this.loadingLogs = true
      try {
        const response = await this.$http.get(`/api/v1/plugins/${plugin.id}/logs`, {
          params: { page: 0, size: 100 },
        })
        this.pluginLogs = response.data.content || []
      } catch (error) {
        this.showError(this.$t('plugin_manager.snack_logs_load_failed') + ': ' + error.message)
      } finally {
        this.loadingLogs = false
      }
    },
    async clearLogs() {
      this.clearingLogs = true
      try {
        await this.$http.delete(`/api/v1/plugins/${this.selectedPlugin.id}/logs`)
        this.pluginLogs = []
        this.showSuccess(this.$t('plugin_manager.snack_logs_cleared'))
      } catch (error) {
        this.showError(this.$t('plugin_manager.snack_logs_clear_failed') + ': ' + error.message)
      } finally {
        this.clearingLogs = false
      }
    },
    async resolveDynamicEnums(properties) {
      for (const key of Object.keys(properties)) {
        const field = properties[key]
        if (field.dynamicEnum === 'libraries') {
          try {
            const response = await this.$http.get('/api/v1/libraries')
            field.enum = response.data.map(lib => lib.name)
          } catch (e) {
            field.enum = []
          }
        }
      }
    },
    getSchemaField(key) {
      if (this.parsedSchema?.properties?.[key]) return this.parsedSchema.properties[key]
      return {}
    },
    formatConfigKey(key) {
      return key.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())
    },
    formatDate(date) {
      return new Date(date).toLocaleString()
    },
    getLogColor(level) {
      const colors = {
        DEBUG: 'grey',
        INFO: 'info',
        WARN: 'warning',
        ERROR: 'error',
      }
      return colors[level] || 'grey'
    },
    getLogIcon(level) {
      const icons = {
        DEBUG: 'mdi-bug',
        INFO: 'mdi-information',
        WARN: 'mdi-alert',
        ERROR: 'mdi-alert-circle',
      }
      return icons[level] || 'mdi-circle'
    },
    getTypeColor(type) {
      const colors = {
        METADATA: 'blue',
        DOWNLOAD: 'green',
        TASK: 'orange',
        PROCESSOR: 'purple',
        NOTIFIER: 'pink',
        ANALYZER: 'cyan',
        SCROBBLER: 'teal',
      }
      return colors[type] || 'grey'
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
  },
}
</script>
