package com.example.cartoonizedimage.models

import android.content.Context
import android.graphics.Bitmap
import com.example.cartoonizedimage.ml.StyleTransfer
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer

/**
 * Created by tapos-datta on 12/8/20.
 */
class StyleTransfer(context: Context) : BaseHandle(context) {
    private lateinit var styleBottleneck: TensorBuffer
    private lateinit var mlModel: StyleTransfer
    private var output: StyleTransfer.Outputs? = null

    init {
        WIDTH_CONSTRAINT = 384
        HEIGHT_CONSTRAINT = 384
    }

    override fun initModel(config: Int) {
        mlModel = StyleTransfer.newInstance(context, getOptions(config))
        styleBottleneck = TensorBuffer.createFixedSize(intArrayOf(1, 1, 1, 100), DataType.FLOAT32)
    }

    override fun loadImageToModel(image: Bitmap) {
        tensor = TensorImage.fromBitmap(
            if (image.width != WIDTH_CONSTRAINT || image.height != HEIGHT_CONSTRAINT) {
                getResizedBitmap(image, WIDTH_CONSTRAINT!!, HEIGHT_CONSTRAINT!!)
            } else {
                image
            }
        )
    }

    fun loadStyleWeights(byteBuffer: ByteBuffer) {
        styleBottleneck.loadBuffer(byteBuffer)
    }

    override fun applyModel(): StyleTransfer.Outputs? {
        tensor?.run {
            output = mlModel.process(this, styleBottleneck)
        }
        return output
    }

    override fun releaseModel() {
        mlModel.close()
    }

}