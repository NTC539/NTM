package com.example.ntm.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract

object PathUtils {
    fun getPathFromTreeUri(context: Context, uri: Uri): String? {
        return try {
            val docId = DocumentsContract.getTreeDocumentId(uri)
            if (docId.startsWith("primary:")) {
                val relativePath = docId.substringAfter(":")
                val primaryStorage = Environment.getExternalStorageDirectory().absolutePath
                if (relativePath.isEmpty()) primaryStorage else "$primaryStorage/$relativePath"
            } else {
                // Для SD-карт и других хранилищ преобразование сложнее, возвращаем null
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
