package com.example.cartoonizedimage.interfaces

import android.graphics.Bitmap
import java.nio.ByteBuffer
import java.nio.IntBuffer

/**
 * Created by tapos-datta on 12/19/20.
 */
interface ImageCarrier {

    fun setImageDataReceiver(imageDataReceiver: ImageDataReceiver?)

    fun requestForCapturingImg(customWidth: Int, customHeight: Int)

    fun setImageSrc(src: Bitmap, isRecycle: Boolean)

    fun setOutputImage(src: Bitmap, isRecycle: Boolean)

    fun setImageSrc(srcRGB: IntBuffer, width: Int, height: Int)
}