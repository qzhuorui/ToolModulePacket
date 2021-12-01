package com.choryan.quan.wxclean.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by ChoRyan Quan on 2021/12/1 11:36.
 * E-mail : qzhuorui@gmail.com
 * Function :
 */
abstract class MyBaseAdapter<T : RecyclerView.ViewHolder?> : RecyclerView.Adapter<T>() {

    abstract fun setDataSource(context: Context, data: List<FileBean>)

    abstract fun getDataSource(): ArrayList<FileBean>

    abstract fun cleanDataSource()
}