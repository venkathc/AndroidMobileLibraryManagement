package com.venkateshgowda.personallibrary.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.InputStream
import java.util.UUID

object CoverStorage {
    private const val MaxBytes = 2L * 1024L * 1024L
    private const val MaxDimension = 1600

    fun copy(context: Context, uri: Uri): String {
        val bitmap = context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
            ?: error("Selected file is not a valid image.")
        return save(context, bitmap)
    }

    fun processCaptured(context: Context, temporaryPath: String): String {
        val temporaryFile = File(temporaryPath)
        try {
            val bitmap = temporaryFile.inputStream().use(BitmapFactory::decodeStream)
                ?: error("Captured file is not a valid image.")
            return save(context, bitmap)
        } finally {
            temporaryFile.delete()
        }
    }

    fun delete(path: String?) {
        if (path != null) File(path).delete()
    }

    private fun save(context: Context, bitmap: Bitmap): String {
        val directory = File(context.filesDir, "book_covers").apply { mkdirs() }
        val target = File(directory, "${UUID.randomUUID()}.jpg")
        val scale = minOf(1f, MaxDimension.toFloat() / maxOf(bitmap.width, bitmap.height))
        val resized = if (scale < 1f) Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true) else bitmap
        try {
            for (quality in 88 downTo 50 step 4) {
                target.outputStream().use { resized.compress(Bitmap.CompressFormat.JPEG, quality, it) }
                if (target.length() <= MaxBytes) return target.absolutePath
            }
            error("Cover image cannot be reduced below 2 MB.")
        } catch (error: Exception) {
            target.delete()
            throw error
        } finally {
            if (resized !== bitmap) resized.recycle()
            bitmap.recycle()
        }
    }
}