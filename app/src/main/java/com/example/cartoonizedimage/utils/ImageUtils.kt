package com.example.cartoonizedimage.utils

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

/**
 * Created by TAPOS DATTA on 14,January,2021
 */

class ImageUtils {

    companion object {
        fun getCompressedBitmap(path: String, width: Int, height: Int): Bitmap {
            var scaledBitmap: Bitmap? = null

            try {
                var inWidth = 0
                var inHeight = 0
                var input: InputStream? = FileInputStream(path)
                var degree: Int = rotationDegrees(path)

                // decode image size (decode metadata only, not the whole image)
                var options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeStream(input, null, options)
                input?.close()
                input = null

                // save width and height
                inWidth = options.outWidth
                inHeight = options.outHeight

                // decode full image pre-resized
                input = FileInputStream(path)
                options = BitmapFactory.Options()
                options.inPreferredConfig = Bitmap.Config.ARGB_8888

                // calc rought re-size (this is no exact resize)
                options.inSampleSize = Math.max(inWidth / width, inHeight / height)
                // decode full image
                var roughBitmap = BitmapFactory.decodeStream(input, null, options)

                input.close()
                input = null

                if (degree != 0)
                    roughBitmap = rotateBitmap(roughBitmap!!, degree)

                // calc exact destination size
                val m = Matrix()
                val inRect = RectF(
                    0f, 0f,
                    roughBitmap!!.width.toFloat(),
                    roughBitmap.height.toFloat()
                )
                val outRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
                m.setRectToRect(inRect, outRect, Matrix.ScaleToFit.CENTER)
                val values = FloatArray(9)
                m.getValues(values)

                // resize bitmap
                scaledBitmap = Bitmap.createScaledBitmap(
                    roughBitmap,
                    (roughBitmap.width * values[0]).toInt(),
                    (roughBitmap.height * values[4]).toInt(),
                    true
                )
                if(scaledBitmap != roughBitmap) {
                    roughBitmap.recycle()
                }

            } catch (e: IOException) {
                Log.e("Image", e.message, e)
            }
            return scaledBitmap!!
        }

        /**
         * Get rotation degree from image exif
         */
        private fun rotationDegrees(filePath: String): Int {
            val ei = ExifInterface(filePath);
            val orientation =
                ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

            // Return rotation degree based on orientation from exif
            return when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        }

        private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {

            val rotationMatrix = Matrix()
            rotationMatrix.postRotate(rotationDegrees.toFloat())
            val rotatedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, rotationMatrix, true)
            bitmap.recycle()

            return rotatedBitmap
        }

        fun getBitmapFromAsset(
            context: Context,
            filePath: String?,
            width: Int,
            height: Int,
            config: Bitmap.Config
        ): Bitmap? {
            val assetManager: AssetManager = context.resources.assets
            var scaledBitmap: Bitmap? = null
            var input: InputStream
            try {
                input = assetManager.open(filePath!!)
                // decode image size (decode metadata only, not the whole image)
                var options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeStream(input, null, options)
                input?.close()
                // save width and height
                var inWidth = options.outWidth
                var inHeight = options.outHeight
                // decode full image pre-resized
                input = assetManager.open(filePath!!)
                options = BitmapFactory.Options()
                options.inPreferredConfig = config
                // calc rought re-size (this is no exact resize)
                options.inSampleSize = Math.max(inWidth / width, inHeight / height)
                // decode full image
                var roughBitmap = BitmapFactory.decodeStream(input, null, options)
                input.close()
                // calc exact destination size
                val m = Matrix()
                val inRect = RectF(
                    0f, 0f,
                    roughBitmap!!.width.toFloat(),
                    roughBitmap.height.toFloat()
                )
                val outRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
                m.setRectToRect(inRect, outRect, Matrix.ScaleToFit.CENTER)
                val values = FloatArray(9)
                m.getValues(values)
                // resize bitmap
                scaledBitmap = Bitmap.createScaledBitmap(
                    roughBitmap,
                    (roughBitmap.width * values[0]).toInt(),
                    (roughBitmap.height * values[4]).toInt(),
                    true
                )
                if(scaledBitmap != roughBitmap) {
                    roughBitmap.recycle()
                }

            } catch (e: IOException) {
                Log.e("Image", e.message, e)
            }
            return scaledBitmap!!
        }
    }

}