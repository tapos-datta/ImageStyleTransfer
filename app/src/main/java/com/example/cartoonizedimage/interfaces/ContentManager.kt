package com.example.cartoonizedimage.interfaces

import android.graphics.Bitmap

/**
 * Created by TAPOS DATTA on 25,December,2020
 */

interface ContentManager {

    fun getContent(): Bitmap?

    fun getContentId(): Long

    fun getContentAspectRatio(): Float

}