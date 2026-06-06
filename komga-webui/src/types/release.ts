interface ReleaseDto {
  version: string,
  releaseDate: Date,
  url: string,
  latest: boolean,
  preRelease: boolean,
  description: string,
}

interface GalleryDlForkCommitDto {
  sha: string,
  shortSha: string,
  message: string,
  author: string | null,
  date: string,
  url: string,
  installed: boolean,
}

interface GalleryDlForkUpdateDto {
  installedSha: string | null,
  behindCount: number,
  commits: GalleryDlForkCommitDto[],
}
