package com.example.styletransfer.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import android.util.Size
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by TAPOS DATTA on 26,December,2020
 */

class FileUtils {

    companion object {
        private val TAG = "FileUtils"
        var rootDir: File? = null
        const val OUTPUT_DIR = "ImageStyle"
        private val OUTPUT_IMAGE_SIZE_CONSTRAIN = 1920

        fun getRootStorageDirectory(c: Context, directory_name: String?): File? {
            val result: File
            // First, try getting access to the sdcard partition
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                Log.d(TAG, "Using sdcard")
                result = File(Environment.getExternalStorageDirectory(), directory_name)
            } else {
                // Else, use the internal storage directory for this application
                Log.d(TAG, "Using internal storage")
                result = File(c.getApplicationContext().getFilesDir(), directory_name)
            }
            if (!result.exists()) result.mkdir() else if (result.isFile) {
                return null
            }
            Log.d("getRootStorageDirectory", result.absolutePath)
            return result
        }


        fun getStorageDirectory(parent_directory: File?, new_child_directory_name: String?): File? {
            val result = File(parent_directory, new_child_directory_name)
            if (!result.exists()) return if (result.mkdir()) result else {
                Log.e("getStorageDirectory", "Error creating " + result.absolutePath)
                null
            } else if (result.isFile) {
                return null
            }
            Log.d("getStorageDirectory", "directory ready: " + result.absolutePath)
            return result
        }

        private fun createTempFile(root: File?, filename: String?, extension: String): File? {
            var extension = extension
            var output: File? = null
            return try {
                if (filename != null) {
                    if (!extension.contains(".")) extension = ".$extension"
                    output = File(root, filename + extension)
                    output.createNewFile()
                    //output = File.createTempFile(filename, extension, root);
                    //Log.i(TAG, "Created temp file: " + output.getAbsolutePath());
                }
                output
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }

        fun createImageFileInRootAppStorage(c: Context?, filename: String): File? {
            rootDir = getRootStorageDirectory(c!!, OUTPUT_DIR)
            val strArgs = filename.split(".").toTypedArray()
            return createTempFile(
                rootDir,
                strArgs[0],
                strArgs[1]
            )
        }

        fun getCurrentDateAndTime(): String {
            // Create a DateFormatter object for displaying date in specified format.
            // Create a DateFormatter object for displaying date in specified format.
            val formatter = SimpleDateFormat("dd_MM_yyyy_hh_mm_ss")
            // Create a calendar object that will convert the date and time value in milliseconds to date.

            // Create a calendar object that will convert the date and time value in milliseconds to date.
            val calendar: Calendar = Calendar.getInstance()
            calendar.setTimeInMillis(System.currentTimeMillis())
            return formatter.format(calendar.getTime())
        }

        fun getOutputContentSize(inputAspect: Float): Size {
            return if (inputAspect <= 1f) {
                Size(
                    (inputAspect * OUTPUT_IMAGE_SIZE_CONSTRAIN).toInt(),
                    OUTPUT_IMAGE_SIZE_CONSTRAIN
                )
            } else {
                Size(
                    OUTPUT_IMAGE_SIZE_CONSTRAIN,
                    (OUTPUT_IMAGE_SIZE_CONSTRAIN / inputAspect).toInt()
                )
            }
        }

        fun createNewImgFileAndSaved(context: Context, bitmap: Bitmap, isRecycle: Boolean): String {
            val fileName = "StyledImage_" + getCurrentDateAndTime() + ".jpeg"
            val destFile = createImageFileInRootAppStorage(context, fileName)
            val outStream = FileOutputStream(destFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            outStream.flush()

            //close stream and release bitmap
            outStream.close()
            if (isRecycle && !bitmap.isRecycled) {
                bitmap.recycle()
            }
            return fileName
        }
    }

}