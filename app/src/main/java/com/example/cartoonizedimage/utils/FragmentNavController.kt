package com.example.cartoonizedimage.utils

import androidx.navigation.NavController
import com.example.cartoonizedimage.interfaces.FragmentChangeListener

/**
 * Created by tapos-datta on 12/18/20.
 */
class FragmentNavController : FragmentChangeListener {

    lateinit var navigationController: NavController

    override fun setNavController(navController: NavController) {
        this.navigationController = navController
    }

    override fun navigateFragment(id: Int) {
        navigationController.navigate(id)
    }

    override fun navigateUp() {
        navigationController.navigateUp()
    }

}