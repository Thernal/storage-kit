package io.thernal.storagekit.storage.impl.data.preferences

import okio.FileSystem

/** Okio's local file system. It exists on every target here, but not in common metadata, hence expect. */
internal expect val systemFileSystem: FileSystem
