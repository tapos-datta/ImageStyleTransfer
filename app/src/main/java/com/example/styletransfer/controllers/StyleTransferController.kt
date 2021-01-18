package com.example.styletransfer.controllers

import android.content.Context
import android.graphics.Bitmap
import com.example.styletransfer.models.StylePrediction
import com.example.styletransfer.models.StyleTransfer

import com.example.styletransfer.interfaces.ImageCarrier
import com.example.styletransfer.interfaces.ImageDataReceiver
import com.example.styletransfer.utils.ImageDataTransformer
import com.example.styletransfer.utils.ImageUtils
import kotlinx.coroutines.*
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