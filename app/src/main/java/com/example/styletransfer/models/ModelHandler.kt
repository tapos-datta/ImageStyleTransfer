package com.example.styletransfer.models

import android.graphics.Bitmap

/**
 * Created by tapos-datta on 12/8/20.
 */
interface ModelHandler {
    val CONFIG_GPU: Int
    val CONFIG_CPU: Int

    fun initModel(config: Int)

    fun loadImageToModel(image: Bitmap)

    fun applyModel(): Any?

    fun releaseModel()
}