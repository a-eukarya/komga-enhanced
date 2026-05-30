# Komga Enhanced

[![Docker Pulls](https://img.shields.io/docker/pulls/08shiro80/komga)](https://hub.docker.com/r/08shiro80/komga)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21+-blue)](https://adoptium.net/)
[![Based on Komga](https://img.shields.io/badge/Based%20on-Komga-blueviolet)](https://github.com/gotson/komga)

**Komga Enhanced** - A powerful manga media server with integrated manga downloading, automatic chapter tracking, and Tachiyomi/Mihon backup import.

> **Built on [Komga](https://github.com/gotson/komga)** - Extends the excellent Komga media server with manga downloading and automation features.

---

## Contents

- [Quick Start](#quick-start)
- [Why This Fork?](#why-this-fork)
- [Screenshots](#screenshots)
- [Key Features](#key-features)
- [Installation](#installation)
- [Configuration](#configuration)
- [API](docs/api-reference.md)
- [Switching Between Komga Versions](#switching-between-official-komga-and-this-fork)
- [Comparison](#comparison-with-original-komga)
- [Documentation](#documentation)
- [Tech Stack](#tech-stack)
- [Contributing](#contributing)
- [Credits](#credits)

---

## Quick Start

| Image | Description |
|-------|-------------|
| `08shiro80/komga:latest` | Stable release |
| `08shiro80/komga-private:latest` | Testing branch — may contain unstable or experimental changes |

**Docker:**

```bash
docker run -d \
  --name komga \
  --network bridge \
  -p 25600:25600 \
  -v /path/to/config:/config \
  -v /path/to/manga:/manga \
  08shiro80/komga:latest
```

**Docker Compose:**

```yaml
services:
  komga:
    image: 08shiro80/komga:latest
    container_name: komga
    network_mode: bridge
    ports:
      - "25600:25600"
    volumes:
      - ./config:/config
      - /path/to/manga:/manga
    restart: unless-stopped
```

```bash
docker compose up -d
```

Open `http://localhost:25600`, create an admin account, and add a library. See [Installation](#installation) for JAR, build-from-source, and gallery-dl options.

---

## Why This Fork?

This fork transforms Komga from a pure media server into a **complete manga management solution**:

| Problem | Solution |
|---------|----------|
| Manually downloading manga | **Automatic downloads** via gallery-dl — supports MangaDex and other manga/image sites |
| Losing track of downloaded chapters | **Chapter URL tracking** prevents duplicates |
| Re-downloading after crashes | **DB + filesystem tracking** - never re-download completed chapters |
| Title changes cause re-downloads | **UUID folder names** - MangaDex UUID as folder name, immune to title changes |
| Folder renames break series | **Series survives folder rename** - detects same series via MangaDex UUID, preserves progress and metadata |
| Unwanted chapters keep re-downloading | **Chapter blacklist** - permanently block chapters from being downloaded |
| Syncing MangaDex subscriptions | **MangaDex Subscription Sync** auto-downloads from your followed manga feed |
| No guest browsing for family | **Guest/Kiosk mode** - read-only browsing without login, per-library access control |
| Migrating from Tachiyomi/Mihon | **Backup import** extracts your MangaDex follows |
| Long vertical webtoon pages | **Page splitting** like TachiyomiSY |
| Missing metadata | **MangaDex, AniList, Kitsu & Metron plugins** for rich metadata |
| Manual metadata-match per series | **Auto Metadata Match** — title-similarity scoring across providers on scan |
| Tracker progress out of sync | **Manga Scrobbler** — pushes read progress to AniList / MyAnimeList / Kitsu / MangaDex on book completion |
| Western comics need their own tracker | **Comic Scrobbler** — pushes issue progress to Metron |
| No server logs in UI | **Web-based log viewer** - real-time auto-refresh, search, color-coded levels |
| One-off DB/CBZ maintenance | **Settings → Fixes** page — GUI-triggered re-inject of ComicInfo.xml across a library |
| Bland default theme | **7 color themes** - AMOLED, Nord, Dracula, Solarized, Green, Red + Default |

---

## Screenshots

### Download Page

![Download Page](https://github.com/user-attachments/assets/80ed9a1f-f82c-4a5b-8b22-57b61740d765)

### Integrated Mangadex Search

![Integrated Mangadex Search](https://github.com/user-attachments/assets/e2355a2f-a3f0-4b11-970b-96601ec2b52d)

> **New Download** triggers a one-time download only — it does not add the URL to your follow list.

### Follow List Configuration

![Follow List Configuration](https://github.com/user-attachments/assets/43463b26-c436-4dc2-8619-351c37e43843)
![Follow List Configuration 2](https://github.com/user-attachments/assets/16de37ef-e2e9-465a-9298-c695ad58243b)
![Save after edit](https://github.com/user-attachments/assets/57cb7b83-9aa2-4469-a720-bcc5eba92ab2)

### Plugin Manager

![Plugins](https://github.com/user-attachments/assets/baebe276-5a05-4726-8cbe-7d7399e68fb4)

### Manual Backups

![Manual Backups](https://github.com/user-attachments/assets/b2f308be-806c-4f01-a6f5-5162b83a568f)

### Live Logs with Debug Toggle

![Live Logs](https://github.com/user-attachments/assets/24f65885-353f-45c6-85a8-a5afc7a2693e)

### Color Themes

![Color Themes](https://github.com/user-attachments/assets/f7e79d95-e66a-4a3c-98b7-0fff97a526c0)

### Fixes Page

![Fixes Page](https://github.com/user-attachments/assets/10f81194-57bd-42c1-a900-825ee988b1d9)

---

## Key Features

### Download System

Download manga from MangaDex and other manga/image sites via [gallery-dl-komga](https://github.com/08shiro80/gallery-dl-komga) (a fork of [gallery-dl](https://github.com/mikf/gallery-dl) with Komga-specific enhancements):

- **Queue-based downloads** with priority support
- **Real-time progress** via Server-Sent Events (SSE)
- **ComicInfo.xml injection** - metadata embedded in every CBZ
- **UUID folder names** - uses MangaDex UUID as folder name, immune to title changes and mislabeled titles
- **Crash recovery** - skips already-downloaded chapters via DB + filesystem checks, auto-resumes interrupted downloads on restart
- **Repair ComicInfo** - retroactively inject missing ComicInfo.xml and ZIP comments into existing MangaDex CBZ files
- **Rate limiting** - respects site-specific API limits
- **Multi-language support** - 36 languages, shared across all plugins (one setting)
- **Automatic publisher detection** - derives publisher from source site (MangaDex, Mangahere, etc.)
- **Custom gallery-dl path** - point to a local gallery-dl-komga checkout for latest extractors

Any manga/image URL supported by gallery-dl works — not just MangaDex. Simply paste the URL in the WebUI to start a download.

### Follow List Automation

Automatically check for new chapters from your favorite manga:

1. Create a `follow.txt` file in your library root
2. Add URLs (one per line) — MangaDex URLs get fast aggregate checking, other sites use gallery-dl
3. Configure check interval (default: 24 hours)
4. Fast parallel checking via MangaDex aggregate API (~200 manga in 2 minutes)
5. New chapters download automatically

```
# Example follow.txt
https://mangadex.org/title/a1c7c817-4e59-43b7-9365-09c5f56e5eb1
https://mangadex.org/title/32d76d19-8a05-4db0-9fc2-e0b0648fe9d0
https://mangahere.cc/manga/one_piece/
https://hdoujin.me/12345
```

### MangaDex Subscription Feed Sync

Automatically sync new chapters from your MangaDex subscription feed — completely independent from the follow.txt system:

1. Create a [MangaDex API Client](https://mangadex.org/settings) (Personal Client)
2. Enable the `mangadex-subscription` plugin in **Settings → Plugins**
3. Enter your `client_id`, `client_secret`, `username`, and `password`
4. The syncer authenticates via OAuth2 and polls your follow feed

**How it works:**
- Authenticates with MangaDex via OAuth2 (password grant through Keycloak)
- Checks `GET /user/follows/manga` for newly followed manga → queues full download
- Checks `GET /user/follows/manga/feed?publishAtSince=...` for new chapters of existing manga
- Deduplicates against DB: checks mangaDexUuid → series → CHAPTER_URL IDs and blacklist before queuing
- Filters by the language configured in the gallery-dl Downloader plugin (`Default Language`)
- Resilient to temporary MangaDex API failures (retries on next scheduled check)

**Configuration** (via Plugin Manager UI):

| Setting | Default | Description |
|---------|---------|-------------|
| `client_id` | — | MangaDex API Client ID |
| `client_secret` | — | MangaDex API Client Secret |
| `username` | — | MangaDex username |
| `password` | — | MangaDex password |
| `sync_interval_minutes` | 30 | How often to check for new chapters |

**No app restart needed** — the syncer automatically restarts when you save config or toggle the plugin.

### Tachiyomi/Mihon Migration

Import your manga library from Tachiyomi or Mihon:

- Supports `.tachibk` (Mihon/forks) and `.proto.gz` (Tachiyomi) formats
- Extracts MangaDex URLs from your backup
- Adds URLs to your library's `follow.txt`
- Duplicate detection prevents re-adding existing URLs

### Tall Page Splitting

Split long vertical webtoon pages into readable segments:

- Configurable maximum height threshold
- Batch processing for entire libraries
- Preserves original files (creates new split versions)
- Similar to TachiyomiSY's "split tall images" feature

### Enhanced Metadata

Rich metadata from multiple sources:

**MangaDex Metadata Plugin:**
- Fetches title, description, authors, artists
- Multi-language title support (10+ languages)
- Genre and tag mapping
- Cover art downloading

**AniList Metadata Plugin:**
- GraphQL-based metadata fetching
- Configurable title preference (English/Romaji/Native)
- Detailed series information

**Kitsu Metadata Plugin** *(ships as a default external plugin — see [Custom Plugins](#custom-plugins-external-jars)):*
- Fetches title, synopsis, authors, genres, age rating
- Alternative titles in multiple languages
- Cover art downloading
- No API key required

**Metron Metadata Plugin** *(disabled by default — comic metadata):*
- Fetches comic series metadata from [metron.cloud](https://metron.cloud) (free account required)
- Title, publisher, description, genres, cover art
- For Western comics where MangaDex/AniList don't apply

### Auto Metadata Match

Komf-style automatic provider matching for new series on scan:

- Walks a configured provider priority (`anilist,mangadex,kitsu` by default)
- Scores candidates by normalized-title Jaccard similarity (default threshold `0.85`)
- Writes a `web_url` plus multi-source `tracker_links` into `series.json`, so `MylarSeriesProvider` produces one WebLink per matched site
- Bulk-match existing series: `POST /api/v1/automatch/libraries/{id}`
- Configured via `Plugin Manager → Auto Metadata Match` with a **GUI** (drag-free ordered provider list + library multi-select) instead of raw CSV; **does not** appear in the per-series *Search Online Databases* dialog (it runs as a background processor, not a manual search provider)
- Works with **external metadata plugins** too — any installed `MetadataProviderPlugin` becomes selectable in the provider priority

### Custom Plugins (External JARs)

Beyond the built-in plugins, the fork can **load external plugin JARs at runtime** — drop them into `<config-dir>/plugins/` or upload via **Plugin Manager → Install Plugin** (file or URL). Built-in plugins stay compiled in and **cannot be uninstalled**; only external plugins are removable.

> **Docker:** the plugins folder lives at `/config/plugins`, which is **already inside the mounted `/config` volume** — no extra volume mount is needed. Drop JARs there (or upload via the UI) and they persist across container restarts. The bundled default plugins (e.g. Kitsu) are auto-installed there on first start.

- **Two plugin types today:** `MetadataProviderPlugin` (search + metadata, shows up in *Search Online Databases* and Auto Metadata Match) and `NotifierPlugin` (receives `DOWNLOAD_COMPLETED` / `DOWNLOAD_FAILED` events — webhook, Discord, …)
- **Class-loader isolation (SPI-only)** — each JAR runs under a `SpiOnlyClassLoader` whose parent whitelists only the SPI (`org.gotson.komga.infrastructure.plugin.api.*`), JDK (`java.*`/`javax.*`), Kotlin runtime (`kotlin.*`/`kotlinx.*`) and Jackson (`com.fasterxml.jackson.*`). A plugin attempting `import org.gotson.komga.domain.…` or Spring gets `ClassNotFoundException("Plugin denied access to '…' — class-loader isolation. …")`. Bundle anything else you need (OkHttp, JDBC driver, …) into your plugin JAR. Load failures never crash the server.
- **Default external plugin:** the **Kitsu** provider is shipped this way (bundled in the app, auto-installed on first start) as the reference implementation
- **Write your own:** copy [`plugins/plugin-template`](plugins/plugin-template), edit one Kotlin file, run `./gradlew build`, install the JAR. Full guide: **[PLUGINS.md](PLUGINS.md)**

> **Security model — class-loader isolation, NOT a sandbox.** The whitelist above blocks compile-time/runtime imports of Komga internals so a plugin can't reach into the host through the SPI's back door. It does **not** restrict filesystem (`java.io.File`), network (`java.net.http`) or reflection (`java.lang.reflect`) — those are intentionally allowed because plugins call external APIs. An installed external JAR runs arbitrary code with the same OS rights as the Komga process. The install endpoint is admin-only — only install plugins you trust. See [PLUGINS.md → Security model](PLUGINS.md#security-model) for the full statement of limits.

### MangaDex Search on Downloads Page

A prominent search card sits at the top of **Downloads**. Pick a target library once from the dropdown, then for each result you get two buttons:

- **Download** — queues a one-time download into the selected library
- **Follow / Following** — a toggle. If the title isn't in any library's `follow.txt`, the button is "Follow" and appends the MangaDex URL to the selected library. If it's already in any library's `follow.txt`, the button is "Following" (success-coloured) and a second click removes the line from whichever library contains it.

**Advanced filters** (collapsible panel) — multi-select for:
- *Include tags / genres* (MangaDex `includedTags[]`)
- *Blacklist tags / genres* (MangaDex `excludedTags[]`) — useful for permanently hiding things you never want to see
- Status (ongoing / completed / hiatus / cancelled)
- Content rating (default = all four — empty selection keeps it that way)
- Publication demographic (shounen / shoujo / seinen / josei / none)
- *Only titles with downloadable chapters* (toggle) — MangaDex' built-in `hasAvailableChapters=true` is too generous (it counts external-link chapters and 0-page placeholders as "available", e.g. Solo Leveling still shows up even though every English chapter just links to webnovel.com). With this toggle on the UI does one extra `/manga/{id}/feed` call per result and only keeps titles where at least one chapter has `externalUrl == null` AND `pages > 0` in your preferred language (read from gallery-dl Downloader `default_language`). Each result is cached server-side for 24h.

  > **Cost:** 1 extra MangaDex API call per result when the toggle is on (cached). 20 results = ~20 extra calls = a few seconds of throttling on first run, then free until cache expires. Toggle off = zero extra cost.

With no title and at least one filter set, the button switches to **Browse** and queries MangaDex sorted by `followedCount desc` — popular-first browse when you can't decide what to read next.

**Pagination** — 24 results per page, classic page selector below the grid. MangaDex caps `offset + limit ≤ 10000`, so the page count tops out at 417 pages with the default page size.

**Tag catalog cached 7 days in your browser** — the tag dropdown is filled from `GET /api/v1/plugins/mangadex-metadata/tags` on first use, then served from `localStorage` for a week (MangaDex updates its tags only a few times a year). No tag fetch on every page load.

**Persistent defaults** — a **Save as default** button writes the current filter combination to your **Komga account** (user client-setting `komga.fork.mangadexsearch.defaults`), so defaults follow you across browsers and devices (a previously saved per-browser `localStorage` value is migrated automatically). On the next visit the panel pre-fills with those values, so you can set a permanent tag blacklist once and forget about it. **Clear all** resets the current selection without touching the saved default.

**Already-followed titles are marked** — on page load the search card pre-fetches every library's `follow.txt`, extracts the MangaDex UUIDs, and uses that set to flip every result's Follow button to "Following" state. No special filter; just an honest two-state toggle.

> **API rate-limit note:** every search and browse call goes through `MangaDexMetadataPlugin` to `api.mangadex.org` and counts against MangaDex's global rate limit (~5 req/sec). Heavy use of the panel — especially Browse — will throttle (HTTP 429) for a few seconds, same as the rest of the fork's MangaDex traffic.

### Tracker Sync (Scrobblers)

Push read progress to external trackers when a book is marked completed:

**Manga Scrobbler** — AniList / MyAnimeList / Kitsu / MangaDex:
- OAuth2 auto-refresh for MAL and Kitsu tokens
- Tracker IDs auto-detected from `SeriesMetadata.links` (anilist.co, myanimelist.net, kitsu.app, mangadex.org), or via manual JSON mappings
- Per-user filtering (`sync_user_id`) and per-library exclusion (`exclude_library_ids`)

**Comic Scrobbler** — Metron:
- Pushes Western comic issue progress to metron.cloud
- Resolves issue IDs from `metron.cloud/issue/…` and `metron.cloud/series/…` links

Both scrobblers persist last-known progress and status per (series, tracker) in a `sync_state` table so duplicate submissions are skipped after restarts.

#### Shared MangaDex credentials

`gallery-dl Downloader` is the single source of truth for MangaDex authentication. Set `mangadex_username` / `mangadex_password` / `mangadex_client_id` / `mangadex_client_secret` once on that plugin and **MangaDex Subscription Sync** + **Manga Scrobbler** read from it automatically (their own equivalent fields stay empty and only act as per-plugin overrides). The Subscription Sync's `default_language` is already read this way today (in `MangaDexSubscriptionSyncer.checkFeed`), and the credential reads follow the same pattern.

Resolution order for each MangaDex field:
1. The consuming plugin's own field (if non-blank)
2. `gallery-dl Downloader` (`mangadex_*`)
3. `MangaDex Subscription Sync` (`client_id`/`client_secret`/`username`/`password`) — second fallback for the scrobbler only

### Chapter Blacklist

Permanently prevent unwanted chapters from being re-downloaded:

- **Blacklist & Delete** via book 3-dot menu — blacklists the chapter URL and deletes the book file
- **Manage Blacklist** via series 3-dot menu — view, remove, and **manually add** blacklisted chapter URLs
- **Manual URL entry** — paste MangaDex chapter URLs directly into the blacklist dialog for edge cases the automatic system can't handle
- Persists even after book deletion (stored in separate database table)
- Respected by both the downloader and chapter checker

#### Automatic Same-Group Duplicate Detection

When a scanlation group uploads the same chapter multiple times on MangaDex (e.g. same chapter number, same group, different UUIDs), the system automatically detects this, keeps the newest upload, and blacklists the older duplicates. No manual intervention needed — the blacklisted count is included in the known count so the ChapterChecker stays in sync.

### Chapter URL Tracking

Never download the same chapter twice:

- Database tracking of all downloaded chapter URLs
- Filesystem duplicate detection via existing CBZ files
- Tracks chapter metadata (volume, language, scanlation group)
- Multi-group support — same chapter from different scanlation groups downloaded separately

> **Important:** `Import chapter URLs` is **enabled by default** and required for the downloader, follow list, and subscription sync to detect already-downloaded chapters. **Disable it** in **Library → Edit → Metadata** for libraries that don't use the download system — otherwise library scans will be significantly slower.

### Guest/Kiosk Mode

Read-only browsing without login — perfect for family or shared setups:

- Toggle in admin **Settings → UI**
- "Als Gast durchsuchen" button on login page
- Per-library guest access — admins select which libraries are visible
- Security: GET-only access to series/books/libraries, admin routes blocked

### Web Log Viewer

Admin-only log viewer in **Settings → Logs**:

- **Live streaming** via SSE — real-time log tailing without polling
- **Pause/Resume** — buffer incoming logs while paused, flush on resume
- **Debug toggle** — switch between INFO and DEBUG log level at runtime (no restart)
- Color-coded log levels (ERROR=red, WARN=orange, DEBUG=grey)
- Client-side search/filter
- Full log file download

### Color Themes

7 predefined theme presets in **Account → UI Settings**:

- Default, AMOLED, Nord, Dracula, Solarized, Green, Red
- Each preset defines both light and dark mode colors
- Persistent selection via browser local storage

### Configurable Folder Naming

Choose how new manga folders are named:

- `uuid` (default) — uses MangaDex UUID like `0c6fe779-...`
- `title` — uses manga title like `Roman Club`
- Set in **Plugin Manager → gallery-dl Downloader** settings
- Only affects new manga — existing folders are never renamed

### Configurable Chapter Naming

Override the gallery-dl `directory` template per-install via the **Plugin Manager → gallery-dl Downloader → Chapter Naming Template** field. Blank keeps each site's default. Common fields: `{chapter}`, `{chapter_minor}`, `{volume}`, `{title}`, `{group}`, `{lang}`. NOTE: `ChapterMatcher` still expects a `c<num>` token to extract chapter numbers from CBZ filenames — keep that part in your template.

### Settings → Fixes

GUI-triggered one-time maintenance actions under **Settings → Fixes**. Cards are versioned and removed once obsolete.

- **Re-inject ComicInfo.xml** — regenerates `ComicInfo.xml` + `series.json` for every CBZ in the selected library from MangaDex metadata. Enable *Force* to overwrite existing ComicInfo (useful when MangaDex metadata changed, or when migrating libraries that ran an older fork build with a different ComicInfo layout). Runs in the background — you can leave the page and return to watch progress.

### Database Hygiene

Two scheduled jobs run daily and prune log/event tables to keep the SQLite file lean:

- `PLUGIN_LOG` — entries older than 7 days are deleted (was unbounded; observed 119k+ rows on installs without retention)
- `HISTORICAL_EVENT` + `HISTORICAL_EVENT_PROPERTIES` — pruned together (the schema has no `ON DELETE CASCADE`, so the DAO deletes properties first then events)

> **Note on file size:** SQLite does **not** shrink `database.sqlite` when rows are deleted — freed pages are reused for future writes but the file stays the same size on disk (it just stops growing). To actually reclaim space, run `VACUUM;` once against the database while Komga is stopped (e.g. `sqlite3 database.sqlite "VACUUM;"`). Retention prevents unbounded growth; VACUUM is the one-time reclaim.

### Auto-Scan After Download

New chapters are automatically scanned after download completes:

- Uses targeted `scanSeriesFolder()` — only processes the affected series folder
- New books are added, analyzed, and chapter URLs imported automatically
- No full library scan needed

---

## Switching Between Official Komga and This Fork

The fork stores its database migrations in a separate history table (`flyway_fork_history`), completely independent from the official Komga migration history (`flyway_schema_history`):

- **Official Komga → Fork:** Works. Fork migrations run automatically on first startup.
- **Fork → Official Komga:** Works. Official Komga only sees its own migration history and starts normally. The fork's extra tables and columns remain in the database but are ignored.

---

## Installation

### Requirements

- Java 21+
- [gallery-dl-komga](https://github.com/08shiro80/gallery-dl-komga) (`pip install https://github.com/08shiro80/gallery-dl-komga/archive/refs/heads/master.tar.gz`)

### Docker

See [Quick Start](#quick-start) for Docker and Docker Compose commands.

### Updating gallery-dl-komga in Docker

gallery-dl-komga is installed via pip inside the Docker image. The fork keeps the upstream version string (`1.32.1`), so a plain `-U` will **not** pull in new commits — pip sees an unchanged version and reuses its cached wheel, and GitHub caches the branch tarball for a few minutes. Force a clean reinstall:

```bash
docker exec -u 0 komga pip3 install --break-system-packages --no-cache-dir --force-reinstall \
  https://github.com/08shiro80/gallery-dl-komga/archive/refs/heads/master.tar.gz
```

To pin an exact commit — immutable, and it bypasses the branch-tarball cache entirely — use its SHA instead of `refs/heads/master`:

```bash
docker exec -u 0 komga pip3 install --break-system-packages --no-cache-dir --force-reinstall \
  https://github.com/08shiro80/gallery-dl-komga/archive/<commit-sha>.tar.gz
```

gallery-dl runs as a subprocess per download, so no Komga restart is needed — the next download uses the updated version.

### JAR

```bash
java -jar komga.jar
```

> Install **[gallery-dl-komga](https://github.com/08shiro80/gallery-dl-komga)** (the fork, see [Requirements](#requirements)) — the download/ComicInfo features rely on the fork's `komga` postprocessor, not upstream PyPI `gallery-dl`. Point the gallery-dl Downloader plugin's `gallery_dl_path` at a local checkout if needed.

### Important: Metadata Completeness

Full metadata in ComicInfo.xml and series.json (title, authors, genres, cover art, publish dates, scanlation group, etc.) is only guaranteed when downloading from **MangaDex** or when using the **MangaDex/AniList metadata plugins**. Other sites supported by gallery-dl will download chapters correctly, but metadata may be incomplete or missing.

### Build from Source

```bash
# Build frontend
cd komga-webui && npm install && npm run build && cd ..

# Build backend with frontend
./gradlew prepareThymeLeaf :komga:bootJar

# Run
java -jar komga/build/libs/komga-*.jar
```

---

## Configuration

### gallery-dl Setup

Create `~/.config/gallery-dl/config.json`:

```json
{
  "extractor": {
    "mangadex": {
      "lang": ["en"],
      "chapter-filter": "lang == 'en'"
    }
  }
}
```

To use a local gallery-dl-komga checkout (e.g. for latest extractors), set `gallery_dl_path` in the plugin config to the directory containing the `gallery_dl` package. This sets `PYTHONPATH` so `python -m gallery_dl` loads from your local source.

### Follow List Check Interval

Configure via application properties:

```yaml
komga:
  download:
    follow-check-interval: 24h
```

---

## Comparison with Original Komga

| Feature | Original | This Fork |
|---------|----------|-----------|
| Media Server | Yes | Yes |
| Manga Downloads | No | Yes |
| Automatic Chapter Tracking | No | Yes |
| MangaDex Subscription Sync | No | Yes |
| Follow List Automation | No | Yes |
| Chapter Blacklist | No | Yes |
| Series survives folder rename | No | Yes |
| Auto-scan after download | No | Yes |
| Configurable folder naming | No | Yes (UUID/title) |
| Guest/Kiosk Mode | No | Yes |
| Web Log Viewer | No | Yes |
| Color Themes | No | 7 presets |
| Tachiyomi Import | No | Yes |
| Page Splitting | No | Yes |
| AniList, Kitsu & Metron Metadata | No | Yes |
| Auto Metadata Match (Komf-style) | No | Yes |
| Tracker Sync (AniList / MAL / Kitsu / MangaDex / Metron) | No | Yes |
| Settings → Fixes (one-off maintenance) | No | Yes |
| Real-time Progress | No | Yes (SSE) |

---

## Documentation

- [Download System Guide](docs/downloads.md)
- [Follow List Setup](docs/follow-lists.md)
- [Tachiyomi Migration](docs/tachiyomi-import.md)
- [Page Splitting](docs/page-splitting.md)
- [Metadata Plugins](docs/metadata-plugins.md)
- [Plugin Development](docs/plugin-development.md)
- [API Reference](docs/api-reference.md)

---

## Tech Stack

- **Backend:** Kotlin, Spring Boot, jOOQ
- **Frontend:** Vue.js 2, Vuetify, TypeScript
- **Database:** H2 (embedded) / SQLite
- **Downloads:** gallery-dl integration
- **Metadata:** MangaDex API, AniList GraphQL, Kitsu REST, Metron REST, auto-detected from source site
- **Scrobbling:** AniList GraphQL, MyAnimeList REST, Kitsu JSON:API, MangaDex auth, Metron REST

---

## Contributing

1. Fork the repository
2. Create a feature branch
3. Follow [Conventional Commits](https://www.conventionalcommits.org/)
4. Submit a pull request

See [CONTRIBUTING.md](CONTRIBUTING.md) for details.

---

## Credits

- [Komga](https://github.com/gotson/komga) by gotson - The excellent base media server
- [gallery-dl](https://github.com/mikf/gallery-dl) by mikf - Download engine (base)
- [gallery-dl-komga](https://github.com/08shiro80/gallery-dl-komga) - Komga-enhanced gallery-dl fork with extended manga metadata and the `komga` postprocessor for in-process ComicInfo.xml injection
- [jackohagan94-afk](https://github.com/jackohagan94-afk) — original author of the Auto-Metadata-Match, Manga Scrobbler, Comic Scrobbler and Metron Metadata Provider plugins (cherry-picked from their v2.0 branch after their upstream PR was withdrawn)
- [beaux](https://github.com/beaux) — inspiration for the MangaDex search card on the Downloads page (re-implemented here against the existing `MangaDexMetadataPlugin` to keep the rate-limiter and content-rating config in play)
- [MangaDex](https://mangadex.org) - Primary manga source and API
- [AniList](https://anilist.co) - Metadata and tracker source
- [Kitsu](https://kitsu.app) - Metadata and tracker source
- [MyAnimeList](https://myanimelist.net) - Tracker source
- [Metron](https://metron.cloud) - Comic metadata and tracker source

---

## License

[MIT License](LICENSE)
