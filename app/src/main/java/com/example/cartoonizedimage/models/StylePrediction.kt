package com.example.cartoonizedimage.models

import android.content.Context
import android.graphics.Bitmap
import com.example.cartoonizedimage.ml.StylePrediction
import org.tensorflow.lite.support.image.TensorImage
import java.nio.ByteBuffer

/**
 * Created by tapos-datta on 12/8/20.
 */

class StylePrediction(context: Context) : BaseHandle(context) {
    private lateinit var mlModel: StylePrediction
    private var output: StylePrediction.Outputs? = null

    init {
        WIDTH_CONSTRAINT = 256
        HEIGHT_CONSTRAINT = 256
    }

    override fun initModel(config: Int) {
        mlModel = StylePrediction.newInstance(context, getOptions(config))
    }

    override fun loadImageToModel(image: Bitmap) {
        tensor.load(
            if (image.width != WIDTH_CONSTRAINT || image.height != HEIGHT_CONSTRAINT) {
                getResizedBitmap(image, WIDTH_CONSTRAINT!!, HEIGHT_CONSTRAINT!!)
            } else {
                image
            }
        )
    }

    override fun applyModel(): StylePrediction.Outputs? {
        tensor.run {
            output = mlModel.process(this)
        }
        return output
    }

    override fun releaseModel() {
        mlModel.close()
    }

}