package com.choryan.quan.wxclean

import android.app.Activity
import android.app.AppOpsManager
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.provider.Settings
import android.text.format.Formatter
import android.util.Log
import com.choryan.quan.wxclean.filemanager.FileTypeScanManager
import java.io.File
import java.util.*

/**
 * @author: ChoRyan Quan
 * @date: 2021/11/29
 * @description:1.注意检查权限；2.android:requestLegacyExternalStorage="true"
 */
object WeChatCleanHelper {

    const val WECHAT_PACKAGE = "com.tencent.mm"

    private val Normal_Type = listOf(
        WeChatFilType.WXACACHE, WeChatFilType.ATTACHMENT, WeChatFilType.DRAFT,
        WeChatFilType.SNS, WeChatFilType.SFS, WeChatFilType.CRASH, WeChatFilType.LOCALLOG
    )

    private val Medium_Type = listOf(
        WeChatFilType.SNS_AD_LANDINGPAGES,
        WeChatFilType.AVATAR,
        WeChatFilType.BACKUPREPORT,
        WeChatFilType.BRANDICON,
        WeChatFilType.BIZIMG
    )

    private val Severe_Type = listOf(
        WeChatFilType.IMAGE2,
        WeChatFilType.VOICE2,
        WeChatFilType.FAVORITE,
        WeChatFilType.WeChat,
        WeChatFilType.RECORD,
        WeChatFilType.DOWNLOAD,
        WeChatFilType.EMOJI,
        WeChatFilType.GAME,
        WeChatFilType.WALLET,
        WeChatFilType.OPENAPI,
        WeChatFilType.VIDEO,
        WeChatFilType.IMAGE,
        WeChatFilType.VOICE,
        WeChatFilType.CARD,
    )

    fun checkWXPermission(activity: Activity, hasPermission: () -> Unit) {
        val agree = checkPermission(activity)
        if (!agree) {
            openPermissionSetting(activity)
        } else {
            hasPermission()
        }
    }

    private fun checkPermission(activity: Activity): Boolean {
        val granted: Boolean
        val appOps = activity.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            activity.packageName
        )
        granted = if (mode == AppOpsManager.MODE_DEFAULT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) ==
                        PackageManager.PERMISSION_GRANTED
            } else {
                mode == AppOpsManager.MODE_ALLOWED
            }
        } else {
            mode == AppOpsManager.MODE_ALLOWED
        }
        return granted
    }

    private fun openPermissionSetting(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }

    /**
     * @description 获取微信存储使用情况，context：curApp
     * @author ChoRyan Quan
     * @time 2021/11/29 5:21 下午
     */
    private fun getWeChatUsageInfo(context: Context, packageName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val storageStatsManager =
                context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

            val storageVolumes: List<StorageVolume> = storageManager.storageVolumes

            for (item in storageVolumes) {
                val uuidStr = item.uuid
                val uuid =
                    if (uuidStr == null) StorageManager.UUID_DEFAULT else UUID.fromString(uuidStr)
                val uid = getUid(context, packageName)

                val storageStats = storageStatsManager.queryStatsForUid(uuid, uid)

                val appBytes = Formatter.formatFileSize(context, storageStats.appBytes)
                val cacheBytes = Formatter.formatFileSize(context, storageStats.cacheBytes)
                val dataBytes = Formatter.formatFileSize(context, storageStats.dataBytes)
            }
        }
    }

    private fun getUid(context: Context, packageName: String): Int {
        val pm = context.packageManager
        try {
            val ai = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            return ai.uid
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return -1
    }

    /**
     * @description 获取WX路径下所有的文件
     * @author ChoRyan Quan
     * @time 2021/11/29 6:18 下午
     */
    fun getAllWXCacheFile(wxContext: Context, fileCallback: (File) -> Unit) {
        val directoryPath =
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                wxContext.externalCacheDir?.absolutePath
            } else {
                wxContext.cacheDir.absolutePath
            }

        directoryPath?.let { _cacheDirPath ->
            val parentPath = _cacheDirPath.substring(0, _cacheDirPath.lastIndexOf("/"))
            val parentFile = File(parentPath)

            if (parentFile.exists()) {
                collectEachFile(parentFile, fileCallback)
            } else {
                Log.d("test", "getAllWXCacheFile: 文件不存在")
            }
        }
    }

    private fun collectEachFile(parentFile: File, fileCallback: (File) -> Unit) {
        val files = parentFile.listFiles() ?: return
        for (file in files) {
            fileCallback(file)
            if (file.isDirectory) {
                collectEachFile(file, fileCallback)
            }
        }
    }

    /*
    packageName/files
    */
    private fun getWXFilePath(wxContext: Context) {
        val directoryPath =
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                wxContext.getExternalFilesDir(WECHAT_PACKAGE)?.absolutePath
            } else {
                wxContext.filesDir.absolutePath + File.separator + WECHAT_PACKAGE
            }
        directoryPath?.let { _path ->
            val newTargetPath = _path.substring(0, _path.lastIndexOf("/"))
            val file = File(newTargetPath)
            if (file.exists()) {
                Log.d("test", "getWXFilePath: ${file.absolutePath}")
                eachFileRecurse(file)
            } else {
                Log.d("test", "getWXFilePath: 文件不存在")
            }
        }
    }

    /*
    packageName/cache
    */
    private fun getWXCachePath(wxContext: Context) {
        val directoryPath =
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                wxContext.externalCacheDir?.absolutePath
            } else {
                wxContext.cacheDir.absolutePath + File.separator + WECHAT_PACKAGE
            }

        directoryPath?.let { _path ->
            val file = File(_path)
            if (file.exists()) {
                Log.d("test", "getWXCachePath: ${file.absolutePath}")
                eachFileRecurse(file)
            } else {
                Log.d("test", "getWXCachePath: 文件不存在")
            }
        }
    }

    /*
    packageName/MicroMsg
    */
    private fun getWXMsgPath(wxContext: Context) {
        val directoryPath =
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                wxContext.externalCacheDir?.absolutePath
            } else {
                wxContext.cacheDir.absolutePath + File.separator + WECHAT_PACKAGE
            }

        directoryPath?.let { _path ->

            val newTargetPath =
                _path.substring(0, _path.lastIndexOf("/")).plus("/").plus("MicroMsg")

            val file = File(newTargetPath)
            if (file.exists()) {
                Log.d("test", "getWXCachePath: ${file.absolutePath}")
                eachFileRecurse(file)
            } else {
                Log.d("test", "getWXCachePath: 文件不存在")
            }
        }
    }

    private fun eachFileRecurse(inputFile: File) {
        val files = inputFile.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                eachFileRecurse(file)
            } else {
                // TODO: 2021/11/29 deal with every file
            }
        }
    }

    /**
     * @description 按级别清理cache
     * @author ChoRyan Quan
     * @time 2021/11/29 6:20 下午
     */
    fun cleanNormalCache(context: Context, file: File) {
        val normalLevel = Normal_Type.map { it.string }.contains(file.name)
        val tmpTag = file.name.contains("tmp", true) || file.name.contains("temp", true)
        if (normalLevel || tmpTag) {
            FileTypeScanManager.deleteTest(context, file)
            Log.d("test", "cleanNormalCache: ${file.absolutePath}")
        }
    }

    fun cleanMediumCache(context: Context, file: File) {
        val mediaLevel = Medium_Type.map { it.string }.contains(file.name)
        if (mediaLevel) {
            FileTypeScanManager.deleteTest(context, file)
            Log.d("test", "cleanMediumCache: ${file.absolutePath}")
        }
    }

    fun cleanSevereCache(context: Context, file: File) {
        val severeLevel = Severe_Type.map { it.string }.contains(file.name)
        val cacheTag = file.name.contains("cache", true)
        val logTag = file.name.contains("log", true)
        if (severeLevel || cacheTag || logTag) {
            FileTypeScanManager.deleteTest(context, file)
            Log.d("test", "cleanSevereCache: ${file.absolutePath}")
        }
    }

    fun isWXImgCache(fileName: String): Boolean {
        return fileName.contains(".png", true) || fileName.contains(".jpg", true)
    }

    fun isWXEmojiCache(fileName: String): Boolean {
        return fileName.endsWith("_cover")
    }

    fun isWXAudioCache(fileName: String): Boolean {
        return fileName.contains(".mp3", true) || fileName.contains(
            ".aac",
            true
        ) || fileName.contains(".amr", true)
    }

    fun isWXVideoCache(fileName: String): Boolean {
        return fileName.contains(".mp4", true) || fileName.contains(".avi", true)
    }

}