package com.example.styletransfer.controllers

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.styletransfer.interfaces.FragmentChangeListener
import com.example.styletransfer.interfaces.ImageCarrier
import com.example.styletransfer.interfaces.ImageDataReceiver
import com.example.styletransfer.interfaces.LoaderManager
import com.example.styletransfer.utils.FileUtils
import com.example.styletransfer.utils.ImageDataTransformer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.nio.IntBuffer

/**
 * Created by tapos-datta on 1/17/21.
 */
abstract class BaseController(
    protected val currentContext: Context,
    protected val imageCarrier: ImageCarrier
) {
    private var loader: LoaderManager? = null
    protected var handler: Handler = Handler(Looper.getMainLooper())

    fun setLoader(loader: LoaderManager) {
        this.loader = loader
    }

    fun getLoader(): LoaderManager? {
        return loader
    }

    fun onBackAction(
        fragmentChangeListener: FragmentChangeListener,
        mainContent: Bitmap?
    ) {
        if (!(loader!!).isLoaderShown()) {
            imageCarrier.setImageSrc(mainContent!!, false)
            fragmentChangeListener.navigateUp()
        }
    }

    fun saveImgAndReturn(
        fragmentChangeListener: FragmentChangeListener,
        savedAspectRatio: Float
    ) {
        save(fragmentChangeListener,savedAspectRatio,true)
    }

    fun saveImg(
        fragmentChangeListener: FragmentChangeListener,
        savedAspectRatio: Float
    ) {
        save(fragmentChangeListener,savedAspectRatio,false)
    }

    private fun save(
        fragmentChangeListener: FragmentChangeListener,
        savedAspectRatio: Float,
        terminate: Boolean
    ) {
        if (!(loader!!).isLoaderShown()) {
            loader!!.startLoader()
            CoroutineScope(Dispatchers.Main).launch {
                val channel = Channel<Unit>(0) //Rendezvous
                captureImageAndSaveToGallery(savedAspectRatio, channel)
                //suspend function invoke
                channel.send(Unit)

                handler.post {
                    loader!!.dismissLoader()
                    if (terminate) fragmentChangeListener.navigateUp()

                    Toast.makeText(currentContext, "Image Saved Successfully!!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun captureImageAndSaveToGallery(aspectRatio: Float, channel: Channel<Unit>) {
        val size = FileUtils.getOutputContentSize(aspectRatio)

        imageCarrier.setImageDataReceiver(object : ImageDataReceiver {
            override fun getPixelDataFromSurface(imgBuff: IntBuffer) {
                CoroutineScope(Dispatchers.IO).launch {
                    imgBuff.rewind()
                    var pixelArray = IntArray(imgBuff.remaining())
                    imgBuff.get(pixelArray)
                    pixelArray = ImageDataTransformer.convertRGBToARGBSinglePixelData(pixelArray)
                    var outBitmap =
                        Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
                    outBitmap.setPixels(pixelArray, 0, size.width, 0, 0, size.width, size.height)
                    //re-organize bitmap's transform
                    outBitmap = ImageDataTransformer.transformBitmap(outBitmap, -1f, 1f, 180)
                    //Save Image
                    FileUtils.createNewImgFileAndSaved(currentContext, outBitmap, true)
                    //notify channel
                    channel.receive()
                }
                imageCarrier.setImageDataReceiver(null) // release listener
            }
        })
        imageCarrier.requestForCapturingImg(
            size.width,
            size.height
        )
    }

    abstract fun release()
}