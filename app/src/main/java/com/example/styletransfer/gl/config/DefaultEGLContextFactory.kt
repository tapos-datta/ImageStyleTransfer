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

package com.example.styletransfer.gl.config

import android.opengl.GLSurfaceView
import android.util.Log
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGL10.EGL_NONE
import javax.microedition.khronos.egl.EGL10.EGL_NO_CONTEXT
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay


/**
 * Created by tapos-datta on 12/18/20.
 */

class DefaultEGLContextFactory(private val EGLContextClientVersion: Int) : GLSurfaceView.EGLContextFactory{

    private val TAG: String = "DEFAULT_EGL_CONTEXT"

    private val EGL_CONTEXT_CLIENT_VERSION = 0x3098

    override fun createContext(egl: EGL10?, display: EGLDisplay?, config: EGLConfig?): EGLContext {
        val attrib_list: IntArray?
        attrib_list = if (EGLContextClientVersion !== 0) {
            intArrayOf(EGL_CONTEXT_CLIENT_VERSION, EGLContextClientVersion, EGL_NONE)
        } else {
            null
        }
        return egl!!.eglCreateContext(display, config, EGL_NO_CONTEXT, attrib_list)
    }

    override fun destroyContext(egl: EGL10?, display: EGLDisplay?, context: EGLContext?) {
        if (!(egl!!.eglDestroyContext(display, context))) {
            Log.e(TAG, "display:" + display + " context: " + context);
            throw RuntimeException("eglDestroyContext" + egl.eglGetError());
        }
    }

}