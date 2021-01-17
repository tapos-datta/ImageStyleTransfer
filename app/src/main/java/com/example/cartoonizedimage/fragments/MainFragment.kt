package com.example.cartoonizedimage.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.cartoonizedimage.R
import com.example.cartoonizedimage.interfaces.ContentManager
import com.example.cartoonizedimage.interfaces.FragmentChangeListener
import com.example.cartoonizedimage.interfaces.FragmentManager
import com.example.cartoonizedimage.interfaces.ImagePickerLauncher

class MainFragment : Fragment() {

    private lateinit var contentManager: ContentManager
    private lateinit var fragmentChangeListener: FragmentChangeListener
    private var imageFilePath: String? = null
    private lateinit var currentContext: Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.currentContext = context
        fragmentChangeListener = (context as FragmentManager).getFragmentChangeListener()
        contentManager = (context as ContentManager)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.addImage).setOnClickListener {
            (context as ImagePickerLauncher).launchImagePicker()
        }

        view.findViewById<Button>(R.id.cartoon_action).setOnClickListener {
            if (isValidAction(it)) {
                enableEditMode(
                    R.id.cartoonizerFragment,
                    fragmentChangeListener
                )
            }
        }

        view.findViewById<Button>(R.id.style_action).setOnClickListener {
            if (isValidAction(it)) {
                enableEditMode(
                    R.id.styleTransferFragment,
                    fragmentChangeListener
                )
            }
        }
    }

    private fun isValidAction(view: View): Boolean {
        return if (contentManager.getContent() == null) {
            Toast.makeText(view.context, "Pick An Image Before Proceeding.", Toast.LENGTH_SHORT)
                .show()
            false
        } else {
            true
        }
    }


    private fun enableEditMode(
        fragmentId: Int,
        fragmentChangeListener: FragmentChangeListener
    ) {
        fragmentChangeListener.navigateFragment(fragmentId)
    }
}