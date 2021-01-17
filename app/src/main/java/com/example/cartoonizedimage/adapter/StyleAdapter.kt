package com.example.cartoonizedimage.adapter

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.cartoonizedimage.R
import com.example.cartoonizedimage.data.StyleData
import com.example.cartoonizedimage.utils.ImageUtils

/**
 * Created by TAPOS DATTA on 11,January,2021
 */

class StyleAdapter(
    private val data: List<StyleData>,
    private val listener: (Int) -> Unit
) : RecyclerView.Adapter<StyleAdapter.ViewHolder>() {

    var context: Context? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imgView = holder.itemView.findViewById<ImageView>(R.id.imageView)
        imgView.run {
            setImageBitmap(
                ImageUtils.getBitmapFromAsset(
                    context,
                    data[position].styleName,
                    75,
                    75,
                    Bitmap.Config.RGB_565
                )
            )
        }
        holder.itemView.setOnClickListener { listener(position) }
    }

    override fun getItemCount(): Int {
        return data.size
    }

}