package com.example.cartoonizedimage.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.RecyclerView
import com.example.cartoonizedimage.R
import com.example.cartoonizedimage.adapter.StyleAdapter
import com.example.cartoonizedimage.controllers.StyleTransferController
import com.example.cartoonizedimage.data.StyleData
import com.example.cartoonizedimage.interfaces.*

class StyleTransferFragment : Fragment() {

    private lateinit var backPressCallback: OnBackPressedCallback
    private var styleDataSet: MutableList<StyleData>? = null
    private var recyclerView: RecyclerView? = null
    private lateinit var contentManager: ContentManager
    private lateinit var fragmentChangeListener: FragmentChangeListener
    private lateinit var transferController: StyleTransferController
    private var currentContext: Context? = null
    private lateinit var imageCarrier: ImageCarrier
    private lateinit var loader: LoaderManager
    private var previousId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_style_transfer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.styleList)


        view.findViewById<Button>(R.id.cancel_transfer).setOnClickListener {
            transferController.onBackAction(
                fragmentChangeListener,
                contentManager.getContent()
            )
        }

        view.findViewById<Button>(R.id.save_transfer).setOnClickListener {
            transferController.saveImgAndReturn(
                fragmentChangeListener,
                contentManager.getContentAspectRatio()
            )
        }

        styleDataSet = loadStyleNamesFromAsset()
        recyclerView = recyclerView?.apply {
            this.adapter = StyleAdapter(styleDataSet!!) {
                applyStyleTransfer(it)
            }
        }

        backPressCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                transferController.onBackAction(
                    fragmentChangeListener,
                    contentManager.getContent()
                )
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            backPressCallback
        )

    }

    private fun applyStyleTransfer(position: Int) {
        if (!loader.isLoaderShown() && previousId != position) {
            styleDataSet?.let {
                transferController.applyStyleByRefImage(it[position].styleName)
                previousId = position
            }
        } else {
            Toast.makeText(
                currentContext,
                "Already applied or running to change style.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadStyleNamesFromAsset(): MutableList<StyleData> {
        val assetList = currentContext?.let {
            it.assets?.list("styleRef")
        }
        return MutableList(size = assetList!!.size, init = {
            StyleData("styleRef/" + assetList[it])
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.currentContext = context
        fragmentChangeListener = (context as FragmentManager).getFragmentChangeListener()
        imageCarrier = (context as ImageViewHandler).getImageView() as ImageCarrier
        contentManager = (context as ContentManager)
        loader = (context as LoaderManager)
    }

    override fun onStart() {
        super.onStart()

        transferController = StyleTransferController(currentContext!!, imageCarrier).apply {
            setLoader(loader)
            initSourceImgBuffer()
        }
    }

    override fun onStop() {
        super.onStop()
        transferController.release()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //unregister listener here
        backPressCallback.isEnabled = false
        backPressCallback.remove()
    }
}