package com.example.cartoonizedimage.gl

import android.graphics.Bitmap
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY
import android.opengl.Matrix
import com.example.cartoonizedimage.gl.config.DefaultEGLContextFactory
import com.example.cartoonizedimage.gl.config.GlConfigChooser
import com.example.cartoonizedimage.gl.config.GlFrameBufferObject
import com.example.cartoonizedimage.gl.filter.GlDefaultFilter
import com.example.cartoonizedimage.gl.filter.GlPreview
import com.example.cartoonizedimage.interfaces.ImageDataReceiver
import com.example.cartoonizedimage.utils.ImageDataTransformer
import com.example.cartoonizedimage.utils.EGLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


/**
 * Created by tapos-datta on 12/18/20.
 */

class GlRenderer(var imgView: GPUImageView) : GLSurfaceView.Renderer {


    private val EGL_CONTEXT_CLIENT_VERSION = 2

    private val MVPMatrix = FloatArray(16)
    private val ProjMatrix = FloatArray(16)
    private val MMatrix = FloatArray(16)
    private val VMatrix = FloatArray(16)

    private var isBitmapSrc: Boolean = false
    private var isNewShader: Boolean = false
    private var isShapeChanged: Boolean = false
    private var texName = -1
    private var aspectRatio: Float = 1f
    private var srcWidth: Int = 0
    private var srcHeight: Int = 0
    private val normalShader = GlDefaultFilter()
    private val previewShader = GlPreview()
    private lateinit var frameBufferObject: GlFrameBufferObject
    private lateinit var previewFBO: GlFrameBufferObject
    private var runBeforeDraw: Queue<Runnable> = LinkedList<Runnable>()

    private var imageDataReceiver: ImageDataReceiver? = null
    private var requestForCapturingSurface: Boolean = false

    init {
        //set number of bits for RGBA, depth and stencil
        imgView.setEGLConfigChooser(GlConfigChooser(false))
        imgView.setEGLContextFactory(
            DefaultEGLContextFactory(
                EGL_CONTEXT_CLIENT_VERSION
            )
        )
        imgView.setRenderer(this)
        imgView.renderMode = RENDERMODE_WHEN_DIRTY
    }

    override fun onSurfaceCreated(gl: GL10?, glConfig: EGLConfig?) {
        frameBufferObject =
            GlFrameBufferObject()
        previewFBO = GlFrameBufferObject()
        normalShader.setup()
        previewShader.setup()

        //view Matrix define i.e. camera's positons
        Matrix.setLookAtM(
            VMatrix, 0,
            0.0f, 0.0f, 5.0f,
            0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f
        );
    }


    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        frameBufferObject.setup(width, height)
        normalShader.setFrameSize(width, height)

        Matrix.setIdentityM(MMatrix, 0)
        Matrix.scaleM(MMatrix, 0, 1f, -1f, 1f)
        //define projection matrix
        var scaleRatio = width.toFloat() / height
        Matrix.frustumM(ProjMatrix, 0, -scaleRatio, scaleRatio, -1f, 1f, 5f, 7f)
    }

    override fun onDrawFrame(gl: GL10?) {
        synchronized(runBeforeDraw) {
            while (!runBeforeDraw.isEmpty()) {
                runBeforeDraw.poll().run()
            }
        }

        if (isNewShader) {
            isNewShader = false
            previewShader.setup()
            previewShader.setFrameSize(frameBufferObject.getWidth(), frameBufferObject.getHeight())
        }

        //model-view-projection matrix generation
        Matrix.multiplyMM(MVPMatrix, 0, VMatrix, 0, MMatrix, 0)
        Matrix.multiplyMM(MVPMatrix, 0, ProjMatrix, 0, MVPMatrix, 0)

        frameBufferObject.enable();
        glViewport(0, 0, frameBufferObject.getWidth(), frameBufferObject.getHeight())
        //draw src/other contents to frameBufferObject
        onDrawFrame(gl, frameBufferObject)

        frameBufferObject.disable()
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        normalShader.draw(frameBufferObject.getTexName(), null)
    }

    fun setImageDataReceiver(imageDataReceiver: ImageDataReceiver?) {
        this.imageDataReceiver = imageDataReceiver
    }

    fun requestForPixelBuffer(width: Int, height: Int) {
        runBeforeDraw.add(Runnable {
            previewFBO.setup(width, height)
            prepareModelMatrix(1f, -1f)
            requestForCapturingSurface = true
        })
        imgView.requestRender()
    }

    fun setSourceFrame(srcRGB: IntBuffer, width: Int, height: Int) {
        runBeforeDraw.add(Runnable {
            previewFBO.setup(width, height)
            EGLUtils.releaseTexture(texName)
            texName = EGLUtils.loadTextureFromRGBPixelBuffer(srcRGB, width, height)

            isShapeChanged = true
        })
        imgView.requestRender()
    }

    fun setSourceFrame(src: Bitmap, isRecycle: Boolean) {
        runBeforeDraw.add(Runnable {
            srcWidth = src.width
            srcHeight = src.height

            aspectRatio = srcWidth.toFloat() / srcHeight
            EGLUtils.releaseTexture(texName)
            texName = EGLUtils.loadTexture(src, -1, isRecycle)
            prepareModelMatrix(1f, -1f)

            isBitmapSrc = true
            isNewShader = true
            isShapeChanged = false

        })
        imgView.requestRender()
    }

    fun setOutputImage(src: Bitmap, recycle: Boolean) {
        runBeforeDraw.add(Runnable {
            previewFBO.setup(src.width, src.height)
            isShapeChanged = true
            EGLUtils.releaseTexture(texName)
            texName = EGLUtils.loadTexture(src, -1, recycle)
            prepareModelMatrix(1f, -1f)
        })
        imgView.requestRender()
    }

    private fun onDrawFrame(gl: GL10?, fbo: GlFrameBufferObject) {
        if (texName != -1 && !isShapeChanged) {
            previewShader.draw(texName, MVPMatrix, aspectRatio)
        }

        if (texName != -1 && isShapeChanged) {
            normalShader.draw(texName, fbo)
        }

        if (requestForCapturingSurface) {
            requestForCapturingSurface = false
            fbo.disable()
            previewFBO.enable()
            glViewport(0, 0, previewFBO.getWidth(), previewFBO.getHeight())
            normalShader.draw(fbo.getTexName(), previewFBO)
            readPixelsFromSurface(gl, previewFBO)
            glViewport(0, 0, fbo.getWidth(), fbo.getHeight())
        }

    }

    private fun readPixelsFromSurface(gl: GL10?, fbo: GlFrameBufferObject) {
        val pixelBufer = imageDataReceiver?.run {
            val buffer: ByteBuffer =
                ByteBuffer.allocateDirect(fbo.getWidth() * fbo.getHeight() * 4).order(
                    ByteOrder.nativeOrder()
                )
            buffer.clear()
            // read pixels from current Fbo of OpenGL API
            gl?.glReadPixels(
                0,
                0,
                fbo.getWidth(),
                fbo.getHeight(),
                GL10.GL_RGBA,
                GL10.GL_UNSIGNED_BYTE,
                buffer.asIntBuffer()
            )
            buffer
        }
        if (pixelBufer != null) {
            val bytes = ByteArray(fbo.getWidth() * fbo.getHeight() * 4)
            pixelBufer.rewind()
            pixelBufer.get(bytes)
            //store values of RGB at pixel array
            val pixels = ImageDataTransformer.convertRGBAByteArrayToRGBIntArray(bytes)
            imageDataReceiver?.getPixelDataFromSurface(IntBuffer.wrap(pixels))
        }
    }

    private fun prepareModelMatrix(hScaleFactor: Float, vScaleFactor: Float) {
        Matrix.setIdentityM(MMatrix, 0)
        Matrix.scaleM(MMatrix, 0, hScaleFactor, vScaleFactor, 1f)
    }

}