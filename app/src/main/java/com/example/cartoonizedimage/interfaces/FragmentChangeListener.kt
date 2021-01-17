package com.example.cartoonizedimage.interfaces

import androidx.navigation.NavController

/**
 * Created by tapos-datta on 12/18/20.
 */
interface FragmentChangeListener {

    fun setNavController( navController : NavController)

    fun navigateFragment( id : Int)

    fun navigateUp()
}