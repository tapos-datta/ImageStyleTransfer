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

package com.example.cartoonizedimage.gl.config

import android.opengl.EGL14.EGL_BLUE_SIZE
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGL10.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLDisplay


/**
 * Created by tapos-datta on 12/20/20.
 */
open class DefaultConfigChooser : GLSurfaceView.EGLConfigChooser {

    private var configSpec: IntArray? = null
    private var redSize = 0
    private var greenSize = 0
    private var blueSize = 0
    private var alphaSize = 0
    private var depthSize = 0
    private var stencilSize = 0
    private val EGL_OPENGL_ES2_BIT = 4

    constructor(version: Int) : this(true, version);

    constructor(withDepthBuffer: Boolean, version: Int) : this(
        8,
        8,
        8,
        0,
        if (withDepthBuffer) 16 else 0,
        0,
        version
    )

    constructor(
        redSize: Int,
        greenSize: Int,
        blueSize: Int,
        alphaSize: Int,
        depthSize: Int,
        stencilSize: Int,
        version: Int
    ) {
        configSpec = filterConfigSpec(
            intArrayOf(
                EGL_RED_SIZE, redSize,
                EGL_GREEN_SIZE, greenSize,
                EGL_BLUE_SIZE, blueSize,
                EGL_ALPHA_SIZE, alphaSize,
                EGL_DEPTH_SIZE, depthSize,
                EGL_STENCIL_SIZE, stencilSize,
                EGL_NONE
            ), version
        )!!
        this.redSize = redSize
        this.greenSize = greenSize
        this.blueSize = blueSize
        this.alphaSize = alphaSize
        this.depthSize = depthSize
        this.stencilSize = stencilSize
    }


    private fun filterConfigSpec(configSpec: IntArray, version: Int): IntArray? {
        if (version != 2) {
            return configSpec
        }
        val len = configSpec.size
        val newConfigSpec = IntArray(len + 2)
        System.arraycopy(configSpec, 0, newConfigSpec, 0, len - 1)
        newConfigSpec[len - 1] = EGL_RENDERABLE_TYPE
        newConfigSpec[len] = EGL_OPENGL_ES2_BIT
        newConfigSpec[len + 1] = EGL_NONE
        return newConfigSpec
    }

    override fun chooseConfig(egl: EGL10, display: EGLDisplay): EGLConfig {
        // 要求されている仕様から使用可能な構成の数を抽出します。
        // 要求されている仕様から使用可能な構成の数を抽出します。
        val num_config = IntArray(1)
        require(
            egl!!.eglChooseConfig(
                display,
                configSpec,
                null,
                0,
                num_config
            )
        ) { "eglChooseConfig failed" }
        val config_size = num_config[0]
        require(config_size > 0) { "No configs match configSpec" }

        // 実際の構成を抽出します。

        // 実際の構成を抽出します。
        val configs =
            arrayOfNulls<EGLConfig>(config_size)
        require(
            egl!!.eglChooseConfig(
                display,
                configSpec!!,
                configs,
                config_size,
                num_config
            )
        ) { "eglChooseConfig#2 failed" }
        return chooseConfig(egl, display!!, configs)
            ?: throw IllegalArgumentException("No config chosen")
    }

    private fun chooseConfig(
        egl: EGL10,
        display: EGLDisplay,
        configs: Array<EGLConfig?>
    ): EGLConfig? {
        for (config in configs) {
            if(config!=null) {
                val d: Int = findConfigAttrib(egl, display, config, EGL_DEPTH_SIZE, 0)
                val s: Int = findConfigAttrib(egl, display, config, EGL_STENCIL_SIZE, 0)
                if (d >= depthSize && s >= stencilSize) {
                    val r: Int = findConfigAttrib(egl, display, config, EGL_RED_SIZE, 0)
                    val g: Int = findConfigAttrib(egl, display, config, EGL_GREEN_SIZE, 0)
                    val b: Int = findConfigAttrib(egl, display, config, EGL_BLUE_SIZE, 0)
                    val a: Int = findConfigAttrib(egl, display, config, EGL_ALPHA_SIZE, 0)
                    if (r == redSize && g == greenSize && b == blueSize && a == alphaSize) {
                        return config
                    }
                }
            }
        }
        return null
    }

    private fun findConfigAttrib(
        egl: EGL10,
        display: EGLDisplay,
        config: EGLConfig,
        attribute: Int,
        defaultValue: Int
    ): Int {
        val value = IntArray(1)
        return if (egl.eglGetConfigAttrib(display, config, attribute, value)) {
            value[0]
        } else defaultValue
    }


}