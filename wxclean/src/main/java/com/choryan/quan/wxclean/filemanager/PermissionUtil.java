package com.choryan.quan.wxclean.filemanager;

import android.os.Build;


/**
 * Created by Administrator on 2015/9/15.
 */
public class PermissionUtil {

    public static boolean isAndroid5() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

}
