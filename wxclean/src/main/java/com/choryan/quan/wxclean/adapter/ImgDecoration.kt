package com.choryan.quan.wxclean.adapter

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.choryan.quan.wxclean.utils.ExtensionCommon.dp

/**
 * @author: ChoRyan Quan
 * @date: 2021/8/9
 * @description:
 */
class ImgDecoration() : RecyclerView.ItemDecoration() {

    private val bottom = 30f.dp.toInt()
    private val left = 20f.dp.toInt()
    private val right = 20f.dp.toInt()


    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = bottom
        outRect.left = left
        outRect.right = right
    }
}