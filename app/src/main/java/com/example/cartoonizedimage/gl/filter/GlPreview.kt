package com.example.cartoonizedimage.gl.filter

import android.opengl.GLES20
import android.opengl.GLES20.*


/**
 * Created by tapos-datta on 12/19/20.
 */
class GlPreview: GlDefaultFilter {

    private companion object{
        val VERTEX_SHADER = """
                uniform mat4 uMVPMatrix;
                uniform float uCRatio;
                attribute vec4 aPosition;
                attribute vec4 aTextureCoord;
                varying highp vec2 vTextureCoord;
                void main() {
                    vec4 scaledPos = aPosition;
                    scaledPos.x = scaledPos.x * uCRatio;
                    gl_Position = uMVPMatrix * scaledPos;
                    vTextureCoord =  aTextureCoord.xy;
                }
        """.trimIndent()

        val FRAGMENT_SHADER: String = """
                precision mediump float;
                varying highp vec2 vTextureCoord;
                uniform lowp sampler2D sTexture;
                void main() {
                    gl_FragColor = texture2D(sTexture, vTextureCoord);
                }
        """.trimIndent()
    }

    constructor() : this(VERTEX_SHADER, FRAGMENT_SHADER)

    constructor(vertexShader:String, fragmentShader: String) : super(vertexShader,fragmentShader)


    override fun setup() {
        super.setup()
        getHandle("uMVPMatrix");
        getHandle("uCRatio");
        getHandle("aPosition");
        getHandle("aTextureCoord");
    }

    fun draw(
        texName: Int,
        mvpMatrix: FloatArray?,
        aspectRatio: Float
    ) {
        useProgram()
        GLES20.glUniformMatrix4fv(getHandle("uMVPMatrix"), 1, false, mvpMatrix, 0)
        GLES20.glUniform1f(getHandle("uCRatio"), aspectRatio)
        GLES20.glBindBuffer(GL_ARRAY_BUFFER, getVertexBufferName())
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

        GLES20.glActiveTexture(GL_TEXTURE0)
        GLES20.glBindTexture(GL_TEXTURE_2D, texName)
        GLES20.glUniform1i(getHandle(DEFAULT_UNIFORM_SAMPLER), 0)
        GLES20.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(getHandle("aPosition"))
        GLES20.glDisableVertexAttribArray(getHandle("aTextureCoord"))
        GLES20.glBindBuffer(GL_ARRAY_BUFFER, 0)
        GLES20.glBindTexture(GL_TEXTURE_2D, 0)
    }


}