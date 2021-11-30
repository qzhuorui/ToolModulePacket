package com.choryan.quan.wxclean

import android.content.Context
import android.os.Bundle
import android.text.format.Formatter
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.choryan.quan.wxclean.adapter.FileBean
import com.choryan.quan.wxclean.adapter.ImgDecoration
import com.choryan.quan.wxclean.adapter.WXFileSourceAdapter
import com.choryan.quan.wxclean.adapter.WXMediaSourceAdapter
import com.choryan.quan.wxclean.filemanager.FileTypeScanManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(R.layout.activity_main), (FileBean, Int) -> Unit {

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
        val imgAdapter = WXMediaSourceAdapter()
        imgAdapter.click = this
        imgAdapter
    }

    private val fileAdapter by lazy {
        val imgAdapter = WXFileSourceAdapter()
        imgAdapter.click = this
        imgAdapter
    }

    private val dataSource = ArrayList<FileBean>()
    private var selectAll = false

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
                val curAdapter = rv.adapter
                selectAll = !selectAll
                withContext(Dispatchers.IO) {
                    when (curAdapter) {
                        is WXFileSourceAdapter -> {
                            curAdapter.getDataSource().map { it.select = selectAll }
                        }
                        is WXMediaSourceAdapter -> {
                            curAdapter.getDataSource().map { it.select = selectAll }
                        }
                        else -> {
                            Log.d("test", "select_All: adapter error")
                        }
                    }
                }
                curAdapter?.notifyDataSetChanged()
            }
        }
    }

    private fun initRecycleView() {
        rv.addItemDecoration(ImgDecoration())
        rv.isNestedScrollingEnabled = true
    }

    private fun packingMediaAdapter() {
        lifecycleScope.launch(Dispatchers.Main) {
            rv.layoutManager = GridLayoutManager(this@MainActivity, 3)
            rv.adapter = mediaAdapter
            mediaAdapter.cleanDataSource()
            mediaAdapter.setDataSource(this@MainActivity, dataSource)
            mediaAdapter.notifyDataSetChanged()
        }
    }

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
        Toast.makeText(this, "delete file", Toast.LENGTH_SHORT).show()
        p1.select = !p1.select
        rv.adapter?.notifyItemChanged(pos)

//        FileTypeScanManager.deleteTest(this, p1)
//        mediaAdapter.notifyDataSetChanged()
    }

}