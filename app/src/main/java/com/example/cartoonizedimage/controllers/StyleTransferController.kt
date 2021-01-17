package com.example.cartoonizedimage.controllers

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.cartoonizedimage.interfaces.FragmentChangeListener
import com.example.cartoonizedimage.models.StylePrediction
import com.example.cartoonizedimage.models.StyleTransfer

import com.example.cartoonizedimage.interfaces.ImageCarrier
import com.example.cartoonizedimage.interfaces.ImageDataReceiver
import com.example.cartoonizedimage.interfaces.LoaderManager
import com.example.cartoonizedimage.utils.FileUtils
import com.example.cartoonizedimage.utils.ImageDataTransformer
import com.example.cartoonizedimage.utils.ImageUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.lang.Exception
import java.nio.IntBuffer

/**
 * Created by TAPOS DATTA on 10,January,2021
 */

class StyleTransferController(
    currentContext: Context,
    imageCarrier: ImageCarrier
) : BaseController(currentContext, imageCarrier) {

    private var transferModel: StyleTransfer = StyleTransfer(currentContext)
    private var prediction: StylePrediction = StylePrediction(currentContext)
    private var srcImgBuffer: IntBuffer? = null

    override fun release() {
        srcImgBuffer = null
    }

    fun initSourceImgBuffer() {
        imageCarrier.setImageDataReceiver(object : ImageDataReceiver {
            override fun getPixelDataFromSurface(imgBuff: IntBuffer) {
                srcImgBuffer = imgBuff.asReadOnlyBuffer()
                imageCarrier.setImageDataReceiver(null) // release listener
            }
        })
        imageCarrier.requestForCapturingImg(
            transferModel.getWidthConstraint(),
            transferModel.getHeightConstraint()
        )
    }

    fun applyStyleByRefImage(styleName: String) {
        handler.post {
            try {
                if (srcImgBuffer != null) {
                    getLoader()?.startLoader()
                    CoroutineScope(Dispatchers.IO).launch {
                        val bitmap = ImageUtils.getBitmapFromAsset(
                            currentContext,
                            styleName,
                            prediction.getWidthConstraint(),
                            prediction.getHeightConstraint(),
                            Bitmap.Config.ARGB_8888
                        )
                        val styleBuffer = bitmap?.run {
                            prediction.run {
                                initModel(this.CONFIG_GPU)
                                loadImageToModel(bitmap)
                                val styleBuffer =
                                    applyModel()?.styleBottleneckAsTensorBuffer!!.buffer
                                releaseModel()
                                styleBuffer
                            }
                        }
                        val tensorImage = transferModel.run {
                            initModel(this.CONFIG_CPU)
                            createTensorFromPixels(srcImgBuffer!!)
                            loadStyleWeights(styleBuffer!!)
                            val tensorImage = applyModel()?.styledImageAsTensorImage
                            releaseModel()
                            tensorImage
                        }

                        val pixels =
                            ImageDataTransformer.convertRGBToSinglePixelData(tensorImage!!.tensorBuffer.intArray)
                        imageCarrier.setImageSrc(
                            IntBuffer.wrap(pixels),
                            tensorImage.width,
                            tensorImage.height
                        )

                        handler.post {
                            getLoader()?.dismissLoader()
                        }
                        if (bitmap != null && !bitmap.isRecycled) {
                            bitmap.recycle()
                        }
                    }
                }
            } catch (e: Exception) {
                //
            }
        }
    }
}