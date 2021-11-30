package com.choryan.quan.wxclean.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import java.io.File;

/**
 * @author: ChoRyan Quan
 * @date: 2021/11/30
 * @description:
 */
public class FileHelper {

    public static void lookupImgSource(Context context, File file) {
        Uri uri;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        intent.setDataAndType(uri, "image/*");

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void lookupVideoSource(Context context, File file) {
        Uri uri;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        intent.setDataAndType(uri, "video/*");

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void lookupAudioSource(Context context, File file) {
        Uri uri;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        intent.setDataAndType(uri, "audio/*");

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
