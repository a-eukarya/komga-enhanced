-- Tables created by the initial plugin_system migration that no DAO or code path ever used.
-- USER_BLACKLIST here is a never-wired user content filter (tags/genres) — NOT the active
-- chapter blacklist, which lives in BLACKLISTED_CHAPTER and is untouched.
DROP TABLE IF EXISTS UPDATE_CHECK;
DROP TABLE IF EXISTS USER_BLACKLIST;
DROP TABLE IF EXISTS PLUGIN_PERMISSION;
