package com.choryan.quan.wxclean

/**
 * @author: ChoRyan Quan
 * @date: 2021/11/29
 * @description:
 */
enum class WeChatFilType(val string: String) {
    /**
     * normal
     */
    WXACACHE("wxacache"),
    ATTACHMENT("attachment"),//通过微信发送的文件 附件
    DRAFT("draft"),//草稿
    SNS("sns"),//朋友圈缓存
    SFS("sfs"),//微信的临时处理结果的缓存文件
    CRASH("crash"),
    LOCALLOG("locallog"),//本地日志文件夹


    /**
     * medium
     */
    SNS_AD_LANDINGPAGES("sns_ad_landingpages"),//ad
    AVATAR("avatar"),//头像
    BACKUPREPORT("backupReport"),//备份报告
    BRANDICON("brandicon"),//公众号图标
    BIZIMG("bizimg"),//大缓存文件夹（占用空间极大）

    /**
     * severe
     */
    IMAGE2("image2"),//聊天照片
    VOICE2("voice2"),//聊天语音
    FAVORITE("favorite"),//收藏
    WeChat("WeChat"),//朋友圈下载的图片
    RECORD("record"),//录音文件夹
    DOWNLOAD("download"),
    EMOJI("emoji"),//emoji
    GAME("game"),
    WALLET("wallet"),
    OPENAPI("openapi"),
    VIDEO("video"),//小视频
    IMAGE("image"),
    VOICE("voice"),
    CARD("card"),//share contact

}