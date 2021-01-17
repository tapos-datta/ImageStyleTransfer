package com.example.cartoonizedimage.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.model.Model
import java.nio.IntBuffer

/**
 * Created by tapos-datta on 12/8/20.
 */
open abstract class BaseHandle(cntx: Context) : ModelHandler {
    protected val context = cntx
    protected var WIDTH_CONSTRAINT: Int? = null
    protected var HEIGHT_CONSTRAINT: Int? = null
    protected var tensor: TensorImage = TensorImage()

    override val CONFIG_CPU: Int get() = 0
    override val CONFIG_GPU: Int get() = 1

    protected fun getOptions(config: Int): Model.Options {
        return when (config) {
            CONFIG_CPU -> getCPUOption()
            else -> getGPUOption()
        }
    }

    protected fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        val resizedBitmap = Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false
        )
        return resizedBitmap
    }

    fun createTensorFromPixels(buffer: IntBuffer) {
        val shape = intArrayOf(WIDTH_CONSTRAINT!!, HEIGHT_CONSTRAINT!!, 3)
        buffer.rewind()
        val pixelData = IntArray(buffer.remaining())
        buffer.get(pixelData)
        tensor.load(pixelData, shape)
    }

    fun getWidthConstraint(): Int {
        return WIDTH_CONSTRAINT!!
    }

    fun getHeightConstraint(): Int {
        return HEIGHT_CONSTRAINT!!
    }

    private fun getCPUOption(): Model.Options {
        return Model.Options.Builder()
            .setDevice(Model.Device.CPU)
            .setNumThreads(4)
            .build()
    }

    private fun getGPUOption(): Model.Options {
        return Model.Options.Builder()
            .setDevice(Model.Device.GPU)
            .setNumThreads(4)
            .build()
    }
}