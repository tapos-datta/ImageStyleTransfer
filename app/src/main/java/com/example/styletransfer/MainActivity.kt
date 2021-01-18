package com.example.styletransfer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.model.Image
import com.example.styletransfer.gl.GPUImageView
import com.example.styletransfer.interfaces.*
import com.example.styletransfer.utils.FragmentNavController
import com.example.styletransfer.utils.ImageUtils
import com.github.ybq.android.spinkit.SpinKitView


class MainActivity : AppCompatActivity(), FragmentManager, ImagePickerLauncher,
    ImageViewHandler, ContentManager, LoaderManager {

    private var spinKitView: SpinKitView? = null
    private var fragmentChangeListener = FragmentNavController()
    private val REQUEST_PERMISSION_ID: Int = 8888
    private var image: Image? = null
    private var imageView: GPUImageView? = null
    private var originalImage: Bitmap? = null
    private var contentId: Long = 0
    private var default_width = -1
    private var default_height = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()?.hide()
        setContentView(R.layout.activity_main)

        fragmentChangeListener.setNavController(findNavController(R.id.navController))
        imageView = findViewById(R.id.imageView)
        spinKitView = findViewById<SpinKitView>(R.id.spin_kit)?.apply {
            visibility = View.INVISIBLE
            setColor(Color.rgb(110, 50, 237))
        }
    }

    override fun onResume() {
        super.onResume()
        checkForPermission()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_PERMISSION_ID -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this@MainActivity,
                        "permission has been grunted.",
                        Toast.LENGTH_SHORT
                    ).show();

                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "[WARN] permission is not grunted.",
                        Toast.LENGTH_SHORT
                    ).show();
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            image = ImagePicker.getFirstImageOrNull(data)
            updateImageView(image)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun getFragmentChangeListener(): FragmentChangeListener {
        return fragmentChangeListener
    }

    override fun launchImagePicker() {
        ImagePicker.create(this@MainActivity)
            .single()
            .start()
    }

    override fun getContent(): Bitmap? {
        return originalImage
    }

    override fun getContentId(): Long {
        return this.contentId
    }

    override fun getContentAspectRatio(): Float {
        return originalImage?.run {
            (width.toFloat() / height)
        } ?: 1f
    }

    override fun startLoader() {
        spinKitView?.visibility = View.VISIBLE
    }

    override fun isLoaderShown(): Boolean {
        return spinKitView?.isShown ?: false
    }

    override fun dismissLoader() {
        spinKitView?.visibility = View.INVISIBLE
    }

    private fun updateImageView(image: Image?) {
        image?.run {
            originalImage?.recycle()
            if (default_width < 0) {
                imageView?.let {
                    default_width = it.width
                    default_height = it.height
                }
            }
            originalImage = ImageUtils.getCompressedBitmap(
                image.path,
                default_width,
                default_height
            )
            originalImage?.apply {
                resizeImageView(imageView as View, width, height)
                contentId = image.id
                val clone = originalImage!!.copy(config, isMutable)
                (imageView as ImageCarrier).setImageSrc(clone, true)
            }
        }
    }

    private fun resizeImageView(view: View, width: Int, height: Int) {
        val params = view.layoutParams as ConstraintLayout.LayoutParams
        params.width = width
        params.height = height
        params.topMargin = if ((default_height - height) > 0) (default_height - height) / 2 else 0
        view.layoutParams = params
    }

    private fun checkForPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }
        // request camera permission if it has not been grunted.
        // request camera permission if it has not been grunted.
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), REQUEST_PERMISSION_ID
            )
        }
    }

    override fun getImageView(): GLSurfaceView {
        return imageView!!
    }

}

