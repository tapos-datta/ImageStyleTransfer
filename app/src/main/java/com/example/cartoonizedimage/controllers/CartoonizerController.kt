package com.example.cartoonizedimage.controllers

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.cartoonizedimage.models.Cartoonizer
import com.example.cartoonizedimage.interfaces.FragmentChangeListener
import com.example.cartoonizedimage.interfaces.ImageCarrier
import com.example.cartoonizedimage.interfaces.ImageDataReceiver
import com.example.cartoonizedimage.interfaces.LoaderManager
import com.example.cartoonizedimage.utils.ImageDataTransformer
import com.example.cartoonizedimage.utils.FileUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
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