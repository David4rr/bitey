package com.bitey.app.core.image

import java.io.File

object ImageStorageNaming {
    fun sanitizeName(name: String): String {
        val clean = name.trim().replace(Regex("[/\\\\?%*:|\"<>]"), "_")
        return clean.ifBlank { "Food" }
    }

    fun resolveSavedFile(directory: File, imageName: String, extension: String): File {
        directory.mkdirs()
        val clean = sanitizeName(imageName)
        val ext = extension.trimStart('.').ifBlank { "webp" }
        var candidate = File(directory, "$clean.$ext")
        var index = 1
        while (candidate.exists()) {
            candidate = File(directory, "${clean}_$index.$ext")
            index++
        }
        return candidate
    }

    fun saveAsNamedImage(sourceFile: File, directory: File, imageName: String): File {
        val target = resolveSavedFile(directory, imageName, sourceFile.extension.ifBlank { "webp" })
        if (sourceFile.exists()) {
            if (!sourceFile.renameTo(target)) {
                sourceFile.copyTo(target, overwrite = true)
                try { sourceFile.delete() } catch (_: Exception) {}
            }
        }
        return target
    }
}
