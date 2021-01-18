package com.example.styletransfer.gl

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Size
import com.example.styletransfer.interfaces.ImageCarrier
import com.example.styletransfer.interfaces.ImageDataReceiver
import java.nio.IntBuffer

/**
 * Created by tapos-datta on 12/18/20.
 */
class GPUImageView : GLSurfaceView, ImageCarrier {

    private var renderer: GlRenderer? = null;
    private var srcWidth: Int = 0
    private var srcHeight: Int = 0

    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        renderer = GlRenderer(this)
        requestRender()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (srcWidth > 0 && srcHeight > 0) {
            var size = measureSize(srcWidth, srcHeight, measuredWidth, measuredHeight)
            srcWidth = 0
            srcHeight = 0
            super.setMeasuredDimension(size.width, size.height)
        } else {
            super.setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun setImageDataReceiver(imageDataReceiver: ImageDataReceiver?) {
        renderer?.setImageDataReceiver(imageDataReceiver)
    }

    override fun requestForCapturingImg(customWidth: Int, customHeight: Int) {
        renderer?.requestForPixelBuffer(customWidth, customHeight)
    }


    override fun setImageSrc(src: Bitmap, isRecycle: Boolean) {
        renderer?.setSourceFrame(src, isRecycle)
        srcWidth = src.width
        srcHeight = src.height
        requestLayout()
    }

    override fun setImageSrc(srcRGB: IntBuffer, width: Int, height: Int) {
        renderer?.setSourceFrame(srcRGB, width, height)
    }

    override fun setOutputImage(src: Bitmap, isRecycle: Boolean) {
        renderer?.setOutputImage(src, isRecycle)
    }

    private fun measureSize(inWidth: Int, inHeight: Int, outWidth: Int, outHeight: Int): Size {
        var h = outHeight
        var w = outWidth
        if (inWidth >= inHeight) {
            h = ((outWidth.toFloat() / inWidth) * inHeight).toInt()
        } else {
            w = ((outHeight.toFloat() / inHeight) * inWidth).toInt()
        }
        return Size(w, h)
    }


}