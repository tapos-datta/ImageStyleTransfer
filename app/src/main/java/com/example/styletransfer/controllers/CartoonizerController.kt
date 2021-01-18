package com.example.styletransfer.controllers

import android.content.Context
import android.widget.Toast
import com.example.styletransfer.models.Cartoonizer
import com.example.styletransfer.interfaces.ImageCarrier
import com.example.styletransfer.interfaces.ImageDataReceiver
import com.example.styletransfer.utils.ImageDataTransformer
import kotlinx.coroutines.*
import java.nio.IntBuffer


/**
 * Created by tapos-datta on 12/19/20.
 */
class CartoonizerController(
    currentContext: Context,
    imageCarrier: ImageCarrier
) : BaseController(currentContext, imageCarrier) {

    var cartoonizer: Cartoonizer? = null
    var processedId: Long = 0

    init {
        cartoonizer = Cartoonizer(currentContext)
        CoroutineScope(Dispatchers.IO).launch {
            cartoonizer?.run {
                initModel(this.CONFIG_GPU)
            }
        }
    }

    fun applyCartoonizeModel(contentId: Long) {
        if (!(getLoader()!!).isLoaderShown() && processedId != contentId) {
            //start loading dialog
            handler.post {
                getLoader()?.startLoader()
            }

            imageCarrier.setImageDataReceiver(object : ImageDataReceiver {
                override fun getPixelDataFromSurface(imgBuff: IntBuffer) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val tensorImage = cartoonizer?.run {
                            createTensorFromPixels(imgBuff)
                            applyModel()?.cartoonizedImageAsTensorImage!!
                        }
                        val pixels =
                            ImageDataTransformer.convertRGBToSinglePixelData(tensorImage?.tensorBuffer!!.intArray)
                        imageCarrier.setImageSrc(
                            IntBuffer.wrap(pixels),
                            tensorImage.width,
                            tensorImage.height
                        )
                        handler.post {
                            getLoader()?.dismissLoader()
                        }
                        processedId = contentId
                    }
                    imageCarrier.setImageDataReceiver(null) // release listener
                }
            })
            imageCarrier.requestForCapturingImg(
                cartoonizer!!.getWidthConstraint(),
                cartoonizer!!.getHeightConstraint()
            )
        } else {
            Toast.makeText(
                currentContext,
                "Already applied or running a style.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun release() {
        cartoonizer?.releaseModel()
    }

}