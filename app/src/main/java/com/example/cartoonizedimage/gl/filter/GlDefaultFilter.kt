/*
 * Copyright 2018 Masayuki Suda
 *
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.example.cartoonizedimage.gl.filter

import android.opengl.GLES20
import android.opengl.GLES20.GL_FLOAT
import com.example.cartoonizedimage.gl.config.GlFrameBufferObject
import com.example.cartoonizedimage.utils.EGLUtils


/**
 * Created by tapos-datta on 12/18/20.
 */
open class GlDefaultFilter {

    protected companion object {

        val DEFAULT_UNIFORM_SAMPLER: String = "sTexture"

        val DEFAULT_VERTEX_SHADER: String = """
                attribute highp vec4 aPosition;
                attribute highp vec4 aTextureCoord;
                varying highp vec2 vTextureCoord;
                void main() {
                    gl_Position = aPosition;
                    vTextureCoord = aTextureCoord.xy;
                }
        """.trimIndent()

        val DEFAULT_FRAGMENT_SHADER: String = """
                precision mediump float;
                varying highp vec2 vTextureCoord;
                uniform lowp sampler2D sTexture;
                void main() {
                    gl_FragColor = texture2D(sTexture, vTextureCoord);
                }
        """.trimIndent()

        private val VERTICES_DATA = floatArrayOf( // X, Y, Z, U, V
            -1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
            1.0f, -1.0f, 0.0f, 1.0f, 0.0f
        )

        val FLOAT_SIZE_BYTES = 4
        val VERTICES_DATA_POS_SIZE = 3
        val VERTICES_DATA_UV_SIZE = 2
        val VERTICES_DATA_STRIDE_BYTES =
            (VERTICES_DATA_POS_SIZE + VERTICES_DATA_UV_SIZE) * FLOAT_SIZE_BYTES
        val VERTICES_DATA_POS_OFFSET = 0 * FLOAT_SIZE_BYTES
        val VERTICES_DATA_UV_OFFSET =
            VERTICES_DATA_POS_OFFSET + VERTICES_DATA_POS_SIZE * FLOAT_SIZE_BYTES
    }

    private var vertexShaderSource: String? = null
    private var fragmentShaderSource: String? = null

    private var program = 0
    private var vertexShader = 0
    private var fragmentShader = 0
    private var vertexBufferName = 0
    private val handleMap = HashMap<String, Int>()

    constructor() : this(DEFAULT_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER)

    constructor(vertexShader: String, fragmentShader: String) {
        this.vertexShaderSource = vertexShader
        this.fragmentShaderSource = fragmentShader
    }

    open fun setup() {
        release()
        vertexShader = EGLUtils.loadShader(vertexShaderSource, GLES20.GL_VERTEX_SHADER)
        fragmentShader = EGLUtils.loadShader(fragmentShaderSource, GLES20.GL_FRAGMENT_SHADER)
        program = EGLUtils.createProgram(vertexShader, fragmentShader)
        vertexBufferName = EGLUtils.createBuffer(VERTICES_DATA)
        getHandle("aPosition")
        getHandle("aTextureCoord")
        getHandle("sTexture")
    }

    open fun setFrameSize(width: Int, height: Int) {
        // do nothing
    }

    open fun release() {
        GLES20.glDeleteProgram(program)
        program = 0
        GLES20.glDeleteShader(vertexShader)
        vertexShader = 0
        GLES20.glDeleteShader(fragmentShader)
        fragmentShader = 0
        GLES20.glDeleteBuffers(1, intArrayOf(vertexBufferName), 0)
        vertexBufferName = 0
        handleMap.clear()
    }


    open fun draw(texName: Int, fbo: GlFrameBufferObject?) {
        useProgram()
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferName)
        GLES20.glEnableVertexAttribArray(getHandle("aPosition"))
        GLES20.glVertexAttribPointer(
            getHandle("aPosition"),
            VERTICES_DATA_POS_SIZE,
            GL_FLOAT,
            false,
            VERTICES_DATA_STRIDE_BYTES,
            VERTICES_DATA_POS_OFFSET
        )
        GLES20.glEnableVertexAttribArray(getHandle("aTextureCoord"))
        GLES20.glVertexAttribPointer(
            getHandle("aTextureCoord"),
            VERTICES_DATA_UV_SIZE,
            GL_FLOAT,
            false,
            VERTICES_DATA_STRIDE_BYTES,
            VERTICES_DATA_UV_OFFSET
        )
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texName)
        GLES20.glUniform1i(getHandle("sTexture"), 0)
        onDraw()
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(getHandle("aPosition"))
        GLES20.glDisableVertexAttribArray(getHandle("aTextureCoord"))
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }

    open fun onDraw() {}

    protected fun useProgram() {
        GLES20.glUseProgram(program)
    }

    protected fun getVertexBufferName(): Int {
        return vertexBufferName
    }

    protected fun getHandle(name: String): Int {
        val value = handleMap[name]
        if (value != null) {
            return value
        }
        var location = GLES20.glGetAttribLocation(program, name)
        if (location == -1) {
            location = GLES20.glGetUniformLocation(program, name)
        }
        check(location != -1) { "Could not get attrib or uniform location for $name" }
        handleMap[name] = Integer.valueOf(location)
        return location
    }


}