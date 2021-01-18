package com.example.styletransfer.utils

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLES20.*
import android.opengl.GLException
import android.opengl.GLUtils
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer


/**
 * Created by tapos-datta on 12/18/20.
 *
 * ref:
 */

class EGLUtils {

    companion object {

        val NO_TEXTURE: Int = -1;
        val FLOAT_SIZE_BYTES: Int = 4;

        fun loadShader(strSource: String?, iType: Int): Int {
            val compiled = IntArray(1)
            val iShader = GLES20.glCreateShader(iType)
            GLES20.glShaderSource(iShader, strSource)
            GLES20.glCompileShader(iShader)
            GLES20.glGetShaderiv(iShader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                Log.d(
                    "Load Shader Failed", """
                     Compilation
                     ${GLES20.glGetShaderInfoLog(iShader)}
                     """.trimIndent()
                )
                return 0
            }
            return iShader
        }

        @Throws(GLException::class)
        fun createProgram(vertexShader: Int, pixelShader: Int): Int {
            val program = glCreateProgram()
            if (program == 0) {
                throw RuntimeException("Could not create program")
            }
            glAttachShader(program, vertexShader)
            glAttachShader(program, pixelShader)
            glLinkProgram(program)
            val linkStatus = IntArray(1)
            glGetProgramiv(program, GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GL_TRUE) {
                glDeleteProgram(program)
                throw RuntimeException("Could not link program")
            }
            return program
        }

        fun setupSampler(target: Int, mag: Int, min: Int) {
            glTexParameterf(target, GL_TEXTURE_MAG_FILTER, mag.toFloat())
            glTexParameterf(target, GL_TEXTURE_MIN_FILTER, min.toFloat())
            glTexParameteri(target, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
            glTexParameteri(target, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        }

        fun createBuffer(data: FloatArray?): Int {
            return createBuffer(toFloatBuffer(data!!))
        }

        fun createBuffer(data: FloatBuffer?): Int {
            val buffers = IntArray(1)
            glGenBuffers(buffers.size, buffers, 0)
            updateBufferData(buffers[0], data!!)
            return buffers[0]
        }

        fun toFloatBuffer(data: FloatArray): FloatBuffer? {
            val buffer: FloatBuffer = ByteBuffer
                .allocateDirect(data.size * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
            buffer.put(data).position(0)
            return buffer
        }


        fun updateBufferData(bufferName: Int, data: FloatBuffer) {
            glBindBuffer(GL_ARRAY_BUFFER, bufferName)
            glBufferData(
                GL_ARRAY_BUFFER,
                data.capacity() * FLOAT_SIZE_BYTES,
                data,
                GL_DYNAMIC_DRAW
            )
            glBindBuffer(GL_ARRAY_BUFFER, 0)
        }

        fun loadTexture(img: Bitmap, usedTexId: Int, recycle: Boolean): Int {
            val textures = IntArray(1)
            if (usedTexId == NO_TEXTURE) {
                textures[0] = genTexture()
                GLUtils.texImage2D(GL_TEXTURE_2D, 0, img, 0)
            } else {
                glBindTexture(GL_TEXTURE_2D, usedTexId)
                GLUtils.texSubImage2D(GL_TEXTURE_2D, 0, 0, 0, img)
                textures[0] = usedTexId
            }
            if (recycle) {
                img.recycle()
            }
            return textures[0]
        }

        fun loadTextureFromRGBPixelBuffer(pixels: IntBuffer, width: Int, height: Int): Int {
            val textures = IntArray(1)
            textures[0] = genTexture()

            glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGBA,
                width,
                height,
                0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                pixels
            )
            return textures[0]
        }

        fun genTexture(): Int {
            val textures = IntArray(1)
            glGenTextures(1, textures, 0)
            glBindTexture(GL_TEXTURE_2D, textures[0])
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR.toFloat())
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR.toFloat())
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE.toFloat())
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE.toFloat())
            return textures[0]
        }

        fun releaseTexture(texName: Int) {
            if (texName != NO_TEXTURE) {
                val arr = intArrayOf(1)
                arr[0] = texName
                glDeleteTextures(arr.size, arr, 0)
            }
        }

    }


}