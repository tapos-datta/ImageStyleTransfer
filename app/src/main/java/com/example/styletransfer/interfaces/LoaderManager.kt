package com.example.styletransfer.interfaces

/**
 * Created by TAPOS DATTA on 26,December,2020
 */

interface LoaderManager {

    fun startLoader()

    fun dismissLoader()

    fun isLoaderShown(): Boolean
}