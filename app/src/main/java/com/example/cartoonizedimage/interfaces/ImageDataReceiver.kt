package com.example.cartoonizedimage.interfaces


import java.nio.IntBuffer

/**
 * Created by tapos-datta on 12/19/20.
 */
interface ImageDataReceiver {

    fun getPixelDataFromSurface(imgBuff : IntBuffer)
}