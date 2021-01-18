package com.example.styletransfer.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import kotlinx.coroutines.*

/**
 * Created by TAPOS DATTA on 25,December,2020
 */

class ImageDataTransformer {

    companion object {
        val RGBToARGBVal: (Int, Int, Int) -> Int =
            { b, g, r -> (-1 shl 24) or (r shl 16) or (g shl 8) or b }

        val RGBToRGBVal: (Int, Int, Int) -> Int =
            { r, g, b -> (r shl 16) or (g shl 8) or b } //TensorImage Int Array to Pixel Data

        fun transformBitmap(bitmap: Bitmap, scaleX: Float, scaleY: Float, rotationDegrees:Int): Bitmap {
            val matrix = Matrix()
            matrix.postScale(scaleX, scaleY)
            matrix.postRotate(rotationDegrees.toFloat())
            val rotatedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            bitmap.recycle()

            return rotatedBitmap
        }

        fun convertRGBAByteArrayToRGBIntArray(bytes: ByteArray): IntArray {
            return runBlocking {
                val unitSize = (bytes.size / 4);
                val size = 3 * unitSize
                val pixels = IntArray(size) // will be shared to multiple job

                val jobUnitForIntArray = size / 4
                val jobList: Array<Job> = Array(size = 4, init = {
                    val startIndex = (it * jobUnitForIntArray)
                    val endIndex = (it * jobUnitForIntArray) + jobUnitForIntArray - 1
                    val fromIndexOfByte = (it * unitSize)
                    CoroutineScope(Dispatchers.Default).launch {
                        copyPixels(pixels, bytes, startIndex, endIndex, fromIndexOfByte)
                    }
                })

                for (job in jobList) {
                    job.join()
                }
                return@runBlocking pixels
            }
        }

        suspend fun convertRGBToSinglePixelData(rgbArray: IntArray): IntArray {

            val unitSize = rgbArray.size / 3
            val pixels = IntArray(unitSize)
            //split task into 4 job that will perform parallelly
            val jobUnitForPixelArray = unitSize / 4
            val jobUnitForRGBArray = rgbArray.size / 4
            val jobList: Array<Job> = Array(size = 4, init = {
                val startIndex = (it * jobUnitForPixelArray)
                val endIndex = (it * jobUnitForPixelArray) + jobUnitForPixelArray - 1
                val fromIndexOfByte = (it * jobUnitForRGBArray)
                CoroutineScope(Dispatchers.Default).launch {
                    convertRGBToPixelValue(
                        pixels,
                        rgbArray,
                        startIndex,
                        endIndex,
                        fromIndexOfByte,
                        RGBToRGBVal
                    )
                }
            })

            for (job in jobList) {
                job.join()
            }
            return pixels
        }

        suspend fun convertRGBToARGBSinglePixelData(rgbArray: IntArray): IntArray {
            val unitSize = rgbArray.size / 3
            val pixels = IntArray(unitSize)
            //split task into 4 job that will perform parallelly
            val jobUnitForPixelArray = unitSize / 4
            val jobUnitForRGBArray = rgbArray.size / 4
            val jobList: Array<Job> = Array(size = 4, init = {
                val startIndex = (it * jobUnitForPixelArray)
                val endIndex = (it * jobUnitForPixelArray) + jobUnitForPixelArray - 1
                val fromIndexOfByte = (it * jobUnitForRGBArray)
                CoroutineScope(Dispatchers.Default).launch {
                    convertRGBToPixelValue(
                        pixels,
                        rgbArray,
                        startIndex,
                        endIndex,
                        fromIndexOfByte,
                        RGBToARGBVal
                    )
                }
            })

            for (job in jobList) {
                job.join()
            }
            return pixels
        }

        private fun convertRGBToPixelValue(
            pixels: IntArray,
            rgbArray: IntArray,
            startPos: Int,
            endPos: Int,
            indexOfRGB: Int,
            rgbTorgbVal: (Int, Int, Int) -> Int
        ) {
            var j = indexOfRGB
            var index = startPos
            var b = 0
            var g = 0
            var r = 0
            // rgb values exist in array of B-G-R ordering
            while (index <= endPos) {
                b = rgbArray[j++] and 0xff
                g = rgbArray[j++] and 0xff
                r = rgbArray[j++] and 0xff
                pixels[index++] = rgbTorgbVal(r, g, b)
            }
        }


        private fun copyPixels(
            pixels: IntArray,
            bytes: ByteArray,
            startPos: Int,
            endPos: Int,
            fromIndexOfByte: Int
        ) {
            var j = fromIndexOfByte
            var index = startPos
            while (index <= endPos) {
                pixels[index++] = bytes.get(j++).toInt() and 0xff
                pixels[index++] = bytes.get(j++).toInt() and 0xff
                pixels[index++] = bytes.get(j++).toInt() and 0xff
                //ignore the alpha value
                j++
            }
        }
    }

}