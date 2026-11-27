package com.example.ntm.storage

import android.os.Environment
import java.io.File

fun scanFiles(directory: File) {
    if (directory.exists()) {
        val files = directory.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                scanFiles(file)
            } else {
                // file.
            }
        }
    }
}
