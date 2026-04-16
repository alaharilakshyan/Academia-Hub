package com.example.academia.data.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUtils {
    fun getFileFromUri(context: Context, uri: Uri): File? {
        val contentResolver = context.contentResolver
        val fileName = "upload_file_${System.currentTimeMillis()}.pdf"
        val tempFile = File(context.cacheDir, fileName)

        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(tempFile)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            return tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}