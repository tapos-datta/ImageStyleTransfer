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

import android.opengl.GLES20.*
import com.example.styletransfer.utils.EGLUtils


/**
 * Created by tapos-datta on 12/18/20.
 *
 * ref: https://github.com/MasayukiSuda/GPUVideo-android/blob/master/gpuv/src/main/java/com/daasuu/gpuv/egl/GlFramebufferObject.java
 *
 */
class GlFrameBufferObject {

    private var width = 0
    private var height = 0
    private var framebufferName = 0
    private var renderBufferName = 0
    private var texName = -1

    fun getWidth(): Int {
        return width
    }

    fun getHeight(): Int {
        return height
    }

    fun getTexName(): Int {
        return texName
    }

    fun setup(width: Int, height: Int) {
        this.setup(width, height, GL_LINEAR, GL_NEAREST);
    }

    fun setup(width: Int, height: Int, mag: Int, min: Int) {
        val args = IntArray(1)
        glGetIntegerv(GL_MAX_TEXTURE_SIZE, args, 0)
        require(!(width > args[0] || height > args[0])) { "GL_MAX_TEXTURE_SIZE " + args[0] }
        glGetIntegerv(GL_MAX_RENDERBUFFER_SIZE, args, 0)
        require(!(width > args[0] || height > args[0])) { "GL_MAX_RENDERBUFFER_SIZE " + args[0] }
        glGetIntegerv(GL_FRAMEBUFFER_BINDING, args, 0)
        val saveFramebuffer = args[0]
        glGetIntegerv(GL_RENDERBUFFER_BINDING, args, 0)
        val saveRenderbuffer = args[0]
        glGetIntegerv(GL_TEXTURE_BINDING_2D, args, 0)
        val saveTexName = args[0]
        release()
        try {
            this.width = width
            this.height = height
            glGenFramebuffers(args.size, args, 0)
            framebufferName = args[0]
            glBindFramebuffer(GL_FRAMEBUFFER, framebufferName)
            glGenRenderbuffers(args.size, args, 0)
            renderBufferName = args[0]
            glBindRenderbuffer(GL_RENDERBUFFER, renderBufferName)
            glRenderbufferStorage(
                GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, width, height
            )
            glFramebufferRenderbuffer(
                GL_FRAMEBUFFER,
                GL_DEPTH_ATTACHMENT,
                GL_RENDERBUFFER,
                renderBufferName
            )
            glGenTextures(args.size, args, 0)
            texName = args[0]
            glBindTexture(GL_TEXTURE_2D, texName)
            EGLUtils.setupSampler(GL_TEXTURE_2D, GL_LINEAR, GL_NEAREST)
            glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGBA,
                width,
                height,
                0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                null
            )
            glFramebufferTexture2D(
                GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D,
                texName,
                0
            )
            val status = glCheckFramebufferStatus(GL_FRAMEBUFFER)
            if (status != GL_FRAMEBUFFER_COMPLETE) {
                throw RuntimeException("Failed to initialize framebuffer object $status")
            }
        } catch (e: RuntimeException) {
            release()
            throw e
        }
        glBindFramebuffer(GL_FRAMEBUFFER, saveFramebuffer)
        glBindRenderbuffer(GL_RENDERBUFFER, saveRenderbuffer)
        glBindTexture(GL_TEXTURE_2D, saveTexName)
    }

    fun release() {
        val args = IntArray(1)
        args[0] = texName
        glDeleteTextures(args.size, args, 0)
        texName = -1
        args[0] = renderBufferName
        glDeleteRenderbuffers(args.size, args, 0)
        renderBufferName = 0
        args[0] = framebufferName
        glDeleteFramebuffers(args.size, args, 0)
        framebufferName = 0
    }

    fun enable() {
        glBindFramebuffer(GL_FRAMEBUFFER, framebufferName)
    }

    fun disable() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

}