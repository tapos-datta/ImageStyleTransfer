package com.example.cartoonizedimage.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import com.example.cartoonizedimage.R
import com.example.cartoonizedimage.controllers.CartoonizerController
import com.example.cartoonizedimage.interfaces.*

class CartoonizerFragment : Fragment() {

    private lateinit var backPressCallback: OnBackPressedCallback
    private lateinit var contentManager: ContentManager
    private lateinit var fragmentChangeListener: FragmentChangeListener
    private lateinit var cartoonizerController: CartoonizerController
    private var currentContext: Context? = null
    private lateinit var imageCarrier: ImageCarrier
    private lateinit var loader: LoaderManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cartoonizer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.apply_cartoon).setOnClickListener {
            cartoonizerController.applyCartoonizeModel(contentManager.getContentId())
        }

        view.findViewById<Button>(R.id.cancel_cartoon).setOnClickListener {
            cartoonizerController.onBackAction(
                fragmentChangeListener,
                contentManager.getContent()
            )

        }

        view.findViewById<Button>(R.id.save_cartoon).setOnClickListener {
            cartoonizerController.saveImgAndReturn(
                fragmentChangeListener,
                contentManager.getContentAspectRatio()
            )
        }

        backPressCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                cartoonizerController.onBackAction(
                    fragmentChangeListener,
                    contentManager.getContent()
                )
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            backPressCallback
        )
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
        cartoonizerController = CartoonizerController(currentContext!!, imageCarrier)
        cartoonizerController.setLoader(loader)
    }

    override fun onStop() {
        super.onStop()
        cartoonizerController.release()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //unregister listener here
        backPressCallback.isEnabled = false
        backPressCallback.remove()
    }
}