package com.choryan.quan.wxclean

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.format.Formatter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.choryan.quan.wxclean.adapter.*
import com.choryan.quan.wxclean.filemanager.FileTypeScanManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(R.layout.activity_main), (FileBean, Int) -> Unit {

    private val animation by lazy {
        val animation = AnimationDrawable()
        animation.isOneShot = false
        for (i in 0..45) {
            val bitmap = BitmapFactory.decodeStream(assets.open("animation_500_kwlffqm500$i.jpg"))
            animation.addFrame(BitmapDrawable(resources, bitmap), 50)
        }
        animation
    }

    private val taskId by lazy {
        UUID.randomUUID().toString()
    }

    private val WX_CONTEXT by lazy {
        val otherContext =
            createPackageContext(
                WeChatCleanHelper.WECHAT_PACKAGE,
                Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY
            )
        otherContext
    }

    private val mediaAdapter by lazy {
        val imgAdapter = WXMediaSourceAdapterMy()
        imgAdapter.click = this
        imgAdapter
    }

    private val fileAdapter by lazy {
        val imgAdapter = WXFileSourceAdapterMy()
        imgAdapter.click = this
        imgAdapter
    }

    private val dataSource = ArrayList<FileBean>()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initRecycleView()

        btn_pic.setOnClickListener {
            WeChatCleanHelper.checkWXPermission(this) {
                lifecycleScope.launch(Dispatchers.IO) {
                    dataSource.clear()
                    WeChatCleanHelper.getAllWXCacheFile(WX_CONTEXT) {
                        if (WeChatCleanHelper.isWXImgCache(it.name)) {
                            dataSource.add(FileBean(it, "img".hashCode()))
                        }
                    }
                    packingMediaAdapter()
                }
            }
        }
        btn_audio.setOnClickListener {
            WeChatCleanHelper.checkWXPermission(this) {
                lifecycleScope.launch(Dispatchers.IO) {
                    dataSource.clear()
                    WeChatCleanHelper.getAllWXCacheFile(WX_CONTEXT) {
                        if (WeChatCleanHelper.isWXAudioCache(it.name)) {
                            dataSource.add(FileBean(it, "audio".hashCode()))
                        }
                    }
                    packingFileAdapter()
                }
            }
        }
        btn_video.setOnClickListener {
            WeChatCleanHelper.checkWXPermission(this) {
                lifecycleScope.launch(Dispatchers.IO) {
                    dataSource.clear()
                    WeChatCleanHelper.getAllWXCacheFile(WX_CONTEXT) {
                        if (WeChatCleanHelper.isWXVideoCache(it.name)) {
                            dataSource.add(FileBean(it, "video".hashCode()))
                        }
                    }
                    packingMediaAdapter()
                }
            }
        }
        btn_phone_video.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                dataSource.clear()
                FileTypeScanManager.getInstance().getVideoFiles(this@MainActivity, taskId)
                val rawSourceList =
                    FileTypeScanManager.getInstance().videoList.sortedByDescending { it.lastModified() }
                val size = rawSourceList.map { it.length() }.sum()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "size: ${Formatter.formatFileSize(this@MainActivity, size)}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                dataSource.addAll(rawSourceList.map { FileBean(it, "video".hashCode()) })
                packingMediaAdapter()
            }
        }
        btn_select_all.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                val curAdapter = rv.adapter as MyBaseAdapter
                val curSelectStatus = withContext(Dispatchers.IO) {
                    curAdapter.getDataSource().all { it.select }
                }
                changeSelectUI(!curSelectStatus)
                withContext(Dispatchers.IO) {
                    curAdapter.getDataSource().map { it.select = !curSelectStatus }
                }
                curAdapter.notifyDataSetChanged()
            }
        }
        btn_get_select.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                val curAdapter = rv.adapter as MyBaseAdapter
                val selectDataSource = withContext(Dispatchers.IO) {
                    curAdapter.getDataSource().filter { it.select }
                }
            }
        }
        //iv_frame_animation.setImageDrawable(animation)
        //animation.start()
    }

    private fun changeSelectUI(all: Boolean) {
        if (all) {
            btn_select_all.setBackgroundColor(Color.GREEN)
        } else {
            btn_select_all.setBackgroundColor(Color.BLUE)
        }
    }

    private fun initRecycleView() {
        rv.addItemDecoration(ImgDecoration())
        rv.isNestedScrollingEnabled = true
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun packingMediaAdapter() {
        lifecycleScope.launch(Dispatchers.Main) {
            rv.layoutManager = GridLayoutManager(this@MainActivity, 3)
            rv.adapter = mediaAdapter
            mediaAdapter.cleanDataSource()
            mediaAdapter.setDataSource(this@MainActivity, dataSource)
            mediaAdapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun packingFileAdapter() {
        lifecycleScope.launch(Dispatchers.Main) {
            rv.layoutManager = LinearLayoutManager(this@MainActivity)
            rv.adapter = fileAdapter
            fileAdapter.cleanDataSource()
            fileAdapter.setDataSource(this@MainActivity, dataSource)
            fileAdapter.notifyDataSetChanged()
        }
    }

    override fun invoke(p1: FileBean, pos: Int) {
        lifecycleScope.launch(Dispatchers.Main) {
            p1.select = !p1.select
            val adapter = rv.adapter as MyBaseAdapter
            adapter.notifyItemChanged(pos)
            val curSelectStatus = withContext(Dispatchers.IO) {
                adapter.getDataSource().all { it.select }
            }
            changeSelectUI(curSelectStatus)

            //FileTypeScanManager.deleteTest(this, p1)
        }
    }

}