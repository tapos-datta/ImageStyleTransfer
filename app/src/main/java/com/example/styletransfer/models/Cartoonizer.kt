package com.example.styletransfer.models

import android.content.Context
import android.graphics.Bitmap
import com.example.styletransfer.ml.Cartoongan
import org.tensorflow.lite.support.image.TensorImage


/**
 * Created by tapos-datta on 12/8/20.
 */
class Cartoonizer(context: Context) : BaseHandle(context) {

    private lateinit var mlModel: Cartoongan
    private var output: Cartoongan.Outputs? = null

    init {
        WIDTH_CONSTRAINT = 512
        HEIGHT_CONSTRAINT = 512
    }

    override fun initModel(config: Int) {
        mlModel = Cartoongan.newInstance(context, getOptions(config))
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

    override fun applyModel(): Cartoongan.Outputs? {
        tensor.run {
            output = mlModel.process(this)
        }
        return output
    }

    override fun releaseModel() {
        mlModel.close()
    }

}