package com.choryan.quan.wxclean.adapter

import android.content.Context
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.choryan.quan.wxclean.R

/**
 * @author: ChoRyan Quan
 * @date: 2021/11/29
 * @description:
 */
class WXMediaSourceAdapterMy : MyBaseAdapter<WXMediaSourceAdapterMy.ViewHolder>() {

    private var dataSource: ArrayList<FileBean> = ArrayList()
    private var context: Context? = null

    var click: ((FileBean, Int) -> Unit)? = null

    override fun setDataSource(context: Context, data: List<FileBean>) {
        this.context = context
        dataSource.addAll(data)
    }

    override fun getDataSource(): ArrayList<FileBean> {
        return dataSource
    }

    override fun cleanDataSource() {
        dataSource.clear()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img = itemView.findViewById<ImageView>(R.id.iv_item_icon)
        val size = itemView.findViewById<TextView>(R.id.tv_item_size)
        val status = itemView.findViewById<ImageView>(R.id.iv_item_status)
        val parent = itemView.findViewById<ConstraintLayout>(R.id.item_parent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context!!).inflate(
            R.layout.adapter_item_wx_media_source, parent, false
        ) as View
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val curFileBean = dataSource[position]
        context?.let { _context ->
            val curFile = curFileBean.file
            if (curFile.exists() && curFile.length() > 0) {
                Glide.with(_context).load(curFile).fitCenter().into(holder.img)
                holder.size.text = Formatter.formatFileSize(_context, curFile.length())
                holder.parent.setOnClickListener {
                    click?.invoke(curFileBean, position)
                }
                holder.status.visibility = if (curFileBean.select) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }


}