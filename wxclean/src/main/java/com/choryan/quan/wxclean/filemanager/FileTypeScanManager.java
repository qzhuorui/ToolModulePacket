package com.choryan.quan.wxclean.filemanager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016/6/23.
 */

public class FileTypeScanManager {

    public static final String FILE_TYPE_DOWNLOAD_NAME = "download";
    private static FileTypeScanManager instance = null;
    private final Object mLock = new Object();

    private boolean mCancel = false;
    private boolean mIsRunning = false;
    private boolean mDownloadHasFolder = false;
    private String mCurrentTaskId = "";

    //image
    private final ArrayList<File> mImageList;

    //audio
    private final ArrayList<File> mAudioList;

    //video
    private final ArrayList<File> mVideoList;

    //document
    private final ArrayList<File> mDocxList;
    private final ArrayList<File> mExcelList;
    private final ArrayList<File> mPptList;
    private final ArrayList<File> mTxtList;
    private final ArrayList<File> mPdfList;

    //apk
    private final ArrayList<File> mApkList;

    //download
    private final ArrayList<File> mDownloadList;

    //zip file
    private final ArrayList<File> mZipList;

    //zip file
    private final ArrayList<File> mRecentList;

    private ImageFileListener mImageFileListener;
    private AudioFileListener mAudioFileListener;
    private VideoFileListener mVideoFileListener;

    public FileTypeScanManager() {
        mImageList = new ArrayList<>();
        mAudioList = new ArrayList<>();
        mVideoList = new ArrayList<>();
        mDocxList = new ArrayList<>();
        mExcelList = new ArrayList<>();
        mPptList = new ArrayList<>();
        mTxtList = new ArrayList<>();
        mPdfList = new ArrayList<>();
        mApkList = new ArrayList<>();
        mDownloadList = new ArrayList<>();
        mZipList = new ArrayList<>();
        mRecentList = new ArrayList<>();
    }

    public static FileTypeScanManager getInstance() {
        if (instance == null) {
            instance = new FileTypeScanManager();
        }
        return instance;
    }


    public ArrayList<File> getImagesList() {
        synchronized (mLock) {
            return new ArrayList<>(mImageList);
        }
    }

    public ArrayList<File> getAudioList() {
        synchronized (mLock) {
            return new ArrayList<>(mAudioList);
        }
    }

    public ArrayList<File> getVideoList() {
        synchronized (mLock) {
            return new ArrayList<>(mVideoList);
        }
    }

    public ArrayList<File> getDocxList() {
        synchronized (mLock) {
            return new ArrayList<>(mDocxList);
        }
    }

    public ArrayList<File> getExcelList() {
        synchronized (mLock) {
            return new ArrayList<>(mExcelList);
        }
    }

    public ArrayList<File> getPptList() {
        synchronized (mLock) {
            return new ArrayList<>(mPptList);
        }
    }

    public ArrayList<File> getTxtList() {
        synchronized (mLock) {
            return new ArrayList<>(mTxtList);
        }
    }

    public ArrayList<File> getPdfList() {
        synchronized (mLock) {
            return new ArrayList<>(mPdfList);
        }
    }

    public int getDocumentFileCount() {
        synchronized (mLock) {
            return mDocxList.size() + mExcelList.size() + mPptList.size() + mTxtList.size() + mPdfList.size();
        }
    }

    public ArrayList<File> getApkFileList() {
        synchronized (mLock) {
            return new ArrayList<>(mApkList);
        }
    }

    public ArrayList<File> getZipList() {
        synchronized (mLock) {
            return new ArrayList<>(mZipList);
        }
    }

    public ArrayList<File> getDownloadList() {
        synchronized (mLock) {
            return new ArrayList<>(mDownloadList);
        }
    }

    public ArrayList<File> getRecentList() {
        synchronized (mLock) {
            return new ArrayList<>(mRecentList);
        }
    }

    private void getRecentFilesInternal() {
        ArrayList<File> tmp = new ArrayList<>();
        tmp.addAll(mImageList);
        tmp.addAll(mAudioList);
        tmp.addAll(mVideoList);
        tmp.addAll(mDocxList);
        tmp.addAll(mExcelList);
        tmp.addAll(mPptList);
        tmp.addAll(mTxtList);
        tmp.addAll(mPdfList);
        tmp.addAll(mApkList);
        tmp.addAll(mDownloadList);
        tmp.addAll(mZipList);
        if (tmp.size() > 0) {
            for (int i = tmp.size() - 1; i >= 0; i--) {
                File file = tmp.get(i);
                if (System.currentTimeMillis() - file.lastModified() >= 7 * 24 * 60 * 60 * 1000) {
                    tmp.remove(i);
                }
            }
        }

        if (tmp.size() > 0) {
            if (tmp.size() >= 80) {
                ArrayList<File> arrayList = new ArrayList<>();
                for (int i = 0; i < 80; i++) {
                    arrayList.add(tmp.get(i));
                }
                mRecentList.addAll(arrayList);
            } else {
                mRecentList.addAll(tmp);
            }
        }
    }

    public String getDownloadFileCountString() {
        return mDownloadList.size() + (mDownloadHasFolder ? "+" : "");
    }

    public void setCancel(boolean result, String taskId) {
        if (!mCurrentTaskId.equals(taskId)) {
            return;
        }
        if (!mIsRunning) {
            return;
        }
        mCancel = result;
    }

    public int getImagesFileCount(Activity activity) {
        int count = 0;
        Uri u = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA};

        Cursor c = null;
        try {
            if (u != null) {
                c = activity.getContentResolver().query(u, projection, null, null, null);
            }

            if ((c != null) && (c.moveToFirst())) {
                do {
                    String tempDir = c.getString(0);
                    File file = new File(tempDir);
                    if (file.exists()) {
                        count++;
                    }
                }
                while (c.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return count;
    }

    public int getAudiosFileCount(Activity activity) {
        int count = 0;

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION
        };
        Cursor cursor = null;

        try {
            cursor = activity.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);
            File file;
            String path;

            if ((cursor != null) && (cursor.moveToFirst())) {
                do {
                    path = cursor.getString(4);
                    file = new File(path);
                    if (file.exists()) {
                        count++;
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }

    public int getVideosFileCount(Activity activity) {
        int count = 0;
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Video.VideoColumns.DATA};
        Cursor c = activity.getContentResolver().query(uri, projection, null, null, null);
        try {
            if (c != null && c.moveToFirst()) {
                String path;
                do {
                    path = c.getString(0);
                    File file = new File(path);
                    if (file.exists()) {
                        count++;
                    }
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return count;
    }

    public int getDocumentsFileCount(Activity activity) {
        int count = 0;
        ContentResolver cr = activity.getContentResolver();
        Uri uri = MediaStore.Files.getContentUri("external");

        String[] projection = {MediaStore.Files.FileColumns.DATA};

        String selection = MediaStore.Files.FileColumns.MIME_TYPE + " =?"
                + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " =?"
                + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ?"
                + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ?"
                + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ?"
                + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ?"
                + " or " + MediaStore.Files.FileColumns.DATA + " like ?"
                + " or " + MediaStore.Files.FileColumns.DATA + " like ?";

        String[] selectionArgs = new String[]{
                MimeTypeMap.getSingleton().getMimeTypeFromExtension("docx"),
                MimeTypeMap.getSingleton().getMimeTypeFromExtension("doc"),
                MimeTypeMap.getSingleton().getMimeTypeFromExtension("csv"),
                MimeTypeMap.getSingleton().getMimeTypeFromExtension("xlsx"),
                MimeTypeMap.getSingleton().getMimeTypeFromExtension("xls"),
                MimeTypeMap.getSingleton().getMimeTypeFromExtension("txt"),
                "%.ppt",
                "%.pdf"
        };

        String sortOrder = null;
        Cursor c = null;
        try {
            c = cr.query(uri, projection, selection, selectionArgs, sortOrder);

            String path;
            if ((c != null) && (c.moveToFirst())) {
                do {
                    path = c.getString(0);
                    File file = new File(path);
                    if (file.exists()) {
                        count++;
                    }
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return count;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public int getApksFileCount(Activity activity) {
        int count = 0;
        ContentResolver cr = activity.getContentResolver();
        Uri uri = MediaStore.Files.getContentUri("external");

        String[] projection = {MediaStore.Files.FileColumns.DATA};

        String selection = "(" + MediaStore.Files.FileColumns.DATA + " like ?)";

        String[] selectionArgs = new String[]{"%.apk"};

        String sortOrder = null;
        Cursor c = null;

        try {
            String path;
            c = cr.query(uri, projection, selection, selectionArgs, sortOrder);

            if ((c != null) && (c.moveToFirst())) {
                do {
                    path = c.getString(0);
                    File file = new File(path);
                    if (file.exists()) {
                        count++;
                    }
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return count;
    }

    public int getZipsFileCount(Activity activity) {
        int count = 0;
        ContentResolver cr = activity.getContentResolver();
        Uri uri = MediaStore.Files.getContentUri("external");

        String[] projection = {MediaStore.Files.FileColumns.DATA};

        String selection = MediaStore.Files.FileColumns.MIME_TYPE + " = ?";

        String[] selectionArgs = new String[]{MimeTypeMap.getSingleton().getMimeTypeFromExtension("zip")};

        String sortOrder = null;
        Cursor c = null;
        try {
            c = cr.query(uri, projection, selection, selectionArgs, sortOrder);

            String path;
            if ((c != null) && (c.moveToFirst())) {
                do {
                    path = c.getString(0);
                    File file = new File(path);
                    if (file.exists()) {
                        count++;
                    }
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return count;
    }

    public void scanDocumentFileType(Context context, String taskId) {
        synchronized (mLock) {
            mIsRunning = true;
            mCancel = false;
            mCurrentTaskId = taskId;

            mDocxList.clear();
            mExcelList.clear();
            mPptList.clear();
            mTxtList.clear();
            mPdfList.clear();

            if (Build.VERSION.SDK_INT >= 11) {
                ContentResolver cr = context.getContentResolver();
                Uri uri = MediaStore.Files.getContentUri("external");

                String[] projection = {MediaStore.Files.FileColumns.DATA};

                String selection = MediaStore.Files.FileColumns.MIME_TYPE + " =?"
                        + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " =?"
                        + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ?"
                        + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ?"
                        + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ?"
                        + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ?"
                        + " or " + MediaStore.Files.FileColumns.DATA + " like ?"
                        + " or " + MediaStore.Files.FileColumns.DATA + " like ?";


                String[] selectionArgs = new String[]{
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension("docx"),
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension("doc"),
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension("csv"),
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension("xlsx"),
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension("xls"),
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension("txt"),
                        "%.ppt",
                        "%.pdf"
                };

                String sortOrder = null;
                Cursor c = null;
                try {
                    c = cr.query(uri, projection, selection, selectionArgs, sortOrder);

                    String path;
                    if ((c != null) && (c.moveToFirst())) {
                        if (mCancel) {
                            mIsRunning = false;
                            return;
                        }
                        do {
                            path = c.getString(0);
                            File file = new File(path);
                            if (mCancel) {
                                return;
                            }
                            if (file.exists()) {
                                updateFileListType(file);
                            }
                        } while (c.moveToNext());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            } else {
                String internal = !TextUtils.isEmpty(EnvironmentUtil.getStoragePath(context, false)) ? EnvironmentUtil.getStoragePath(context, false) : "/";
                getFile(new File(internal));

                String exSdPath = EnvironmentUtil.getStoragePath(context, true);
                if ((Build.VERSION.SDK_INT < 19 || Build.VERSION.SDK_INT >= 21) && !TextUtils.isEmpty(exSdPath)) {
                    getFile(new File(exSdPath));
                }
                mIsRunning = false;
            }
        }
    }

    public void scanApkFileType(Context context, String taskId) {
        synchronized (mLock) {
            mIsRunning = true;
            mCancel = false;
            mCurrentTaskId = taskId;

            mApkList.clear();

            if (Build.VERSION.SDK_INT >= 11) {
                ContentResolver cr = context.getContentResolver();
                Uri uri = MediaStore.Files.getContentUri("external");

                String[] projection = {MediaStore.Files.FileColumns.DATA};

                String selection = "(" + MediaStore.Files.FileColumns.DATA + " like ?)";

                String[] selectionArgs = new String[]{"%.apk"};

                String sortOrder = null;
                Cursor c = null;

                try {
                    c = cr.query(uri, projection, selection, selectionArgs, sortOrder);

                    String path;
                    if ((c != null) && (c.moveToFirst())) {
                        if (mCancel) {
                            mIsRunning = false;
                            return;
                        }
                        do {
                            path = c.getString(0);
                            File file = new File(path);
                            if (mCancel) {
                                return;
                            }
                            if (file.exists()) {
                                updateFileListType(file);
                            }
                        } while (c.moveToNext());

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            } else {
                String internal = !TextUtils.isEmpty(EnvironmentUtil.getStoragePath(context, false)) ? EnvironmentUtil.getStoragePath(context, false) : "/";
                getFile(new File(internal));

                String exSdPath = EnvironmentUtil.getStoragePath(context, true);
                if ((Build.VERSION.SDK_INT < 19 || Build.VERSION.SDK_INT >= 21) && !TextUtils.isEmpty(exSdPath)) {
                    getFile(new File(exSdPath));
                }
                mIsRunning = false;
            }
        }
    }

    public void scanZipFileType(Context context, String taskId) {
        synchronized (mLock) {
            mIsRunning = true;
            mCancel = false;
            mCurrentTaskId = taskId;
            mZipList.clear();

            if (Build.VERSION.SDK_INT >= 11) {
                ContentResolver cr = context.getContentResolver();
                Uri uri = MediaStore.Files.getContentUri("external");

                String[] projection = {MediaStore.Files.FileColumns.DATA};

                String selection = MediaStore.Files.FileColumns.MIME_TYPE + " =?";

                String[] selectionArgs = new String[]{MimeTypeMap.getSingleton().getMimeTypeFromExtension("zip")};

                String sortOrder = null;
                Cursor c = null;

                try {
                    c = cr.query(uri, projection, selection, selectionArgs, sortOrder);

                    String path;
                    if ((c != null) && (c.moveToFirst())) {
                        if (mCancel) {
                            mIsRunning = false;
                            return;
                        }
                        do {
                            path = c.getString(0);
                            File file = new File(path);
                            if (mCancel) {
                                return;
                            }
                            if (file.exists()) {
                                updateFileListType(file);
                            }
                        } while (c.moveToNext());

                    }
                } catch (Exception e) {
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            } else {
                String internal = !TextUtils.isEmpty(EnvironmentUtil.getStoragePath(context, false)) ? EnvironmentUtil.getStoragePath(context, false) : "/";
                getFile(new File(internal));

                String exSdPath = EnvironmentUtil.getStoragePath(context, true);
                if ((Build.VERSION.SDK_INT < 19 || Build.VERSION.SDK_INT >= 21) && !TextUtils.isEmpty(exSdPath)) {
                    getFile(new File(exSdPath));
                }
                mIsRunning = false;
            }
        }
    }

    private void updateFileListType(File file) {
        String name = file.getName();
        if (name.endsWith(".docx") || name.endsWith(".doc")) {
            mDocxList.add(file);
        } else if (name.endsWith(".csv") || name.endsWith(".xlsx") || name.endsWith(".xls")) {
            mExcelList.add(file);
        } else if (name.endsWith(".ppt")) {
            mPptList.add(file);
        } else if (name.endsWith(".txt")) {
            mTxtList.add(file);
        } else if (name.endsWith(".pdf")) {
            mPdfList.add(file);
        } else if (name.endsWith(".apk")) {
            mApkList.add(file);
        } else if (name.endsWith(".zip")) {
            mZipList.add(file);
        }
    }

    public void getFile(File dir) {
        if (mCancel) {
            return;
        }
        File[] listFile = dir.listFiles();
        if (listFile != null && listFile.length > 0) {
            for (int i = 0; i < listFile.length; i++) {
                if (listFile[i].isDirectory()) {
                    if (mCancel) {
                        return;
                    }
                    getFile(listFile[i]);
                } else {
                    File file = listFile[i];
                    if (mCancel) {
                        return;
                    }
                    updateFileListType(file);
                }
            }
        }
    }

    public void getImageFiles(Context context, String taskId) {
        synchronized (mLock) {
            try {
                mIsRunning = true;
                mCancel = false;
                mImageList.clear();
                mCurrentTaskId = taskId;

                if (mCancel) {
                    mIsRunning = false;
                    return;
                }
                Uri u = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                String[] projection = {MediaStore.Images.ImageColumns.DATA};
                Cursor c = null;

                try {
                    if (u != null) {
                        c = context.getContentResolver().query(u, projection, null, null, null);
                    }

                    if ((c != null) && (c.moveToFirst())) {
                        if (mCancel) {
                            mIsRunning = false;
                            return;
                        }
                        do {
                            String path = c.getString(0);
                            String tempDir = path.substring(0, path.lastIndexOf("/"));
                            File file = new File(path);
                            if (file.exists()) {
                                mImageList.add(file);
                                if (mImageFileListener != null) {
                                    mImageFileListener.onImageScan(path, tempDir);
                                }
                            }
                        } while (c.moveToNext());
                    }
                    mIsRunning = false;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mIsRunning = false;
        }
    }

    public void getAudioFiles(Context context, String taskId) {
        synchronized (mLock) {
            mIsRunning = true;
            mCurrentTaskId = taskId;
            mCancel = false;

            if (mCancel) {
                mIsRunning = false;
                return;
            }
            mAudioList.clear();

            String[] projection = {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.DURATION
            };

            Cursor cursor = null;
            File file;
            String path;
            String dir;
            long albumId;
            try {
                cursor = context.getContentResolver().query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        null,
                        null,
                        null);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        if (mCancel) {
                            mIsRunning = false;
                            return;
                        }
                        path = cursor.getString(4);
                        albumId = cursor.getLong(5);
                        file = new File(path);
                        dir = file.getParent();
                        if (file.exists()) {
                            mAudioList.add(file);
                            if (mAudioFileListener != null) {
                                mAudioFileListener.onAudioScan(path, dir, albumId);
                            }
                        }
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        mIsRunning = false;
    }

    public void getVideoFiles(Context context, String taskId) {
        synchronized (mLock) {
            mIsRunning = true;
            mCancel = false;
            mVideoList.clear();
            mCurrentTaskId = taskId;
            if (mCancel) {
                mIsRunning = false;
                return;
            }

            Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            String[] projection = {MediaStore.Video.VideoColumns.DATA};
            Cursor c = context.getContentResolver().query(uri, projection, null, null, null);
            try {
                if (c != null && c.moveToFirst()) {
                    String path;
                    do {
                        if (mCancel) {
                            mIsRunning = false;
                            return;
                        }

                        path = c.getString(0);
                        File file = new File(path);
                        if (file.exists()) {
                            mVideoList.add(file);
                            if (mVideoFileListener != null) {
                                mVideoFileListener.onVideoScan(path);
                            }
                        }
                    } while (c.moveToNext());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (c != null) {
                    c.close();
                }
            }
            mIsRunning = false;
        }
    }

    private void getVideoFilesInternal(Context context) {
        mVideoList.clear();
        if (mCancel) {
            return;
        }

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Video.VideoColumns.DATA,
                MediaStore.Video.VideoColumns.DISPLAY_NAME
        };
        Cursor c = context.getContentResolver().query(uri, projection, null, null, null);
        try {
            if (c != null) {
                String path;
                while (c.moveToNext()) {
                    if (mCancel) {
                        return;
                    }

                    path = c.getString(0);
                    File file = new File(path);
                    if (file.exists()) {
                        mVideoList.add(file);
                        if (mVideoFileListener != null) {
                            mVideoFileListener.onVideoScan(path);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public void getDownloadFiles(Context context) {
        mDownloadHasFolder = false;
        mDownloadList.clear();
        String path = getDownloadPath(context);
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File tmp : files) {
                    if (tmp.isDirectory()) {
                        mDownloadHasFolder = true;
                    }
                    mDownloadList.add(tmp);
                }
            }
        }
    }

    private void getDownloadFilesInternal(Context context) {
        if (mCancel) {
            return;
        }
        mDownloadHasFolder = false;
        mDownloadList.clear();
        String path = getDownloadPath(context);
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File tmp : files) {
                    if (mCancel) {
                        break;
                    }
                    if (tmp.isDirectory()) {
                        mDownloadHasFolder = true;
                    }
                    mDownloadList.add(tmp);
                }
            }
        }
    }

    public static String getDownloadPath(Context context) {
        String internal = EnvironmentUtil.getStoragePath(context, false);
        return !TextUtils.isEmpty(internal) ? (internal + ("/" + FileTypeScanManager.FILE_TYPE_DOWNLOAD_NAME)) : ("/" + FileTypeScanManager.FILE_TYPE_DOWNLOAD_NAME);
    }

    public static void deleteTest(Context mContext, File file) {
        File[] files = file.listFiles();
        if (files != null && files.length != 0)
            for (File childFile : files) {
                if (childFile.isDirectory()) {
                    deleteTest(mContext, childFile);
                } else {
                    childFile.delete();
                }
            }
        file.delete();
    }

    /**********接口***********/

    public void setImageFileListener(ImageFileListener imageFileListener) {
        this.mImageFileListener = imageFileListener;
    }

    public void setAudioFileListener(AudioFileListener audioFileListener) {
        this.mAudioFileListener = audioFileListener;
    }

    public void setVideoFileListener(VideoFileListener videoFileListener) {
        this.mVideoFileListener = videoFileListener;
    }

    public interface ImageFileListener {
        void onImageScan(String path, String dir);
    }

    public interface AudioFileListener {
        void onAudioScan(String path, String dir, long albumId);
    }

    public interface VideoFileListener {
        void onVideoScan(String path);
    }
}
