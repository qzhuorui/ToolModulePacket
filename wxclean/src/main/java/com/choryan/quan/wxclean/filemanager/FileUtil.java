/*
 * Copyright (C) 2007-2008 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.choryan.quan.wxclean.filemanager;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.storage.StorageManager;
import android.provider.BaseColumns;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Video;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Peli
 * @version 2009-07-03
 */
public class FileUtil {
    /**
     * TAG for log messages.
     */
    public static final String TAG = "FileUtil";

    /**
     * Whether the URI is a local one.
     *
     * @param uri
     * @return
     */
    public static boolean isLocal(String uri) {
        if (uri != null && !uri.startsWith("http://")) {
            return true;
        }
        return false;
    }

    /**
     * Gets the extension of a file name, like ".png" or ".jpg".
     *
     * @param uri
     * @return Extension including the dot("."); "" if there is no extension;
     * null if uri was null.
     */
    public static String getExtension(String uri) {
        if (uri == null) {
            return null;
        }

        int dot = uri.lastIndexOf(".");
        if (dot >= 0) {
            return uri.substring(dot);
        } else {
            // No extension.
            return "";
        }
    }

    /**
     * Returns true if uri is a media uri.
     *
     * @param uri
     * @return
     */
    public static boolean isMediaUri(String uri) {
        if (uri.startsWith(Audio.Media.INTERNAL_CONTENT_URI.toString())
                || uri.startsWith(Audio.Media.EXTERNAL_CONTENT_URI.toString())
                || uri.startsWith(Video.Media.INTERNAL_CONTENT_URI.toString())
                || uri.startsWith(Video.Media.EXTERNAL_CONTENT_URI.toString())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Convert File into Uri.
     *
     * @param file
     * @return uri
     */
    public static Uri getUri(File file) {
        if (file != null) {
            return Uri.fromFile(file);
        }
        return null;
    }

    /**
     * Convert Uri into File.
     *
     * @param uri
     * @return file
     */
    public static File getFile(Uri uri) {
        if (uri != null) {
            String filepath = uri.getPath();
            if (filepath != null) {
                return new File(filepath);
            }
        }
        return null;
    }

    /**
     * Constructs a file from a path and file name.
     *
     * @param curdir
     * @param file
     * @return
     */
    public static File getFile(String curdir, String file) {
        String separator = "/";
        if (curdir.endsWith("/")) {
            separator = "";
        }
        File clickedFile = new File(curdir + separator + file);
        return clickedFile;
    }

    public static File getFile(File curdir, String file) {
        return getFile(curdir.getAbsolutePath(), file);
    }


    public static String formatSize(Context context, long sizeInBytes) {
        return Formatter.formatFileSize(context, sizeInBytes);
    }

    public static long folderSize(File directory) {
        long length = 0;
        File[] files = directory.listFiles();
        if (files != null)
            for (File file : files)
                if (file.isFile())
                    length += file.length();
                else
                    length += folderSize(file);
        return length;
    }

    public static String formatDate(Context context, long dateTime) {
        return DateFormat.getDateFormat(context).format(new Date(dateTime));
    }

    /**
     * use it to calculate file count in the directory recursively
     */
    private static int fileCount = 0;

    public static int getFileCount(File file) {
        fileCount = 0;
        calculateFileCount(file);
        return fileCount;
    }

    /**
     * @param f - file which need be checked
     * @return if is archive - returns true othewise
     */
    public static boolean checkIfZipArchive(File f) {
        if (f.isFile()
                && getExtension(f.getAbsolutePath()).equals(".zip"))
            return true;
        return false;

        // Old way. REALLY slow. Too slow for realtime action loading.
        //        try {
        //            new ZipFile(f);
        //            return true;
        //        } catch (Exception e){
        //            return false;
        //        }
    }

    /**
     * Recursively count all files in the <code>file</code>'s subtree.
     *
     * @param file The root of the tree to count.
     */
    private static void calculateFileCount(File file) {
        if (!file.isDirectory()) {
            fileCount++;
            return;
        }
        if (file.list() == null) {
            return;
        }
        for (String fileName : file.list()) {
            File f = new File(file.getAbsolutePath() + File.separator
                    + fileName);
            calculateFileCount(f);
        }
    }

    /**
     * Returns the path only (without file name).
     *
     * @param file
     * @return
     */
    public static File getPathWithoutFilename(File file) {
        if (file != null) {
            if (file.isDirectory()) {
                // no file to be split off. Return everything
                return file;
            } else {
                String filename = file.getName();
                String filepath = file.getAbsolutePath();

                // Construct path without file name.
                String pathwithoutname = filepath.substring(0,
                        filepath.length() - filename.length());
                if (pathwithoutname.endsWith("/")) {
                    pathwithoutname = pathwithoutname.substring(0,
                            pathwithoutname.length() - 1);
                }
                return new File(pathwithoutname);
            }
        }
        return null;
    }

    public static boolean exists(String uri) {
        return new File(uri).exists();
    }

    public static String hashcode(String uri) {
        return String.valueOf(new File(uri).hashCode());
    }

    public static boolean ensureDir(String paramString) {
        File localFile = new File(paramString);
        if (!localFile.exists()) {
            return localFile.mkdirs();
        } else {
            return true;
        }
    }

    public static long getSize(String uri) {
        return getSize(new File(uri));
    }

    public static long getSize(File file) {
        if (file != null) {
            return file.length();
        } else {
            return -1;
        }
    }

    public static boolean createFile(String uri) {
        boolean result = false;
        try {
            result = new File(uri).createNewFile();
        } catch (IOException e) {
        }
        return result;
    }

    public static boolean ensureFile(String uri) {
        if (!exists(uri)) {
            return createFile(uri);
        } else {
            return true;
        }
    }

    /**
     * support input = {null, "", "/", "x", "x/", "x/y"}
     */
    public static String getFileNameFromUrl(String url) {
        if (url == null) {
            return null;
        } else {
            return url.substring(url.lastIndexOf("/") + 1);
        }
    }

    public static boolean isEmptyDir(File dir) {
        String[] files = dir.list();
        if (files != null && files.length == 0) {
            files = null;
            return true;
        } else {
            files = null;
            return false;
        }
    }

    public static int getDirDepth(File dir) {
        if (dir == null) {
            return -1;
        }

        String[] parts = dir.getAbsolutePath().split("/");
        if (parts != null) {
            int depth = parts.length - 1;
            return depth;
        } else {
            return -1;
        }
    }

    public static boolean deleteDir(String dir) {
        return deleteDir(new File(dir));
    }

    public static boolean deleteDir(File dir) {
        if (dir == null || !dir.exists()) {
            return false;
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return true;
        }

        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                deleteDir(files[i]);
            } else {
                deleteFile(files[i]);
            }
        }

        return dir.delete();
    }

    private static final int COPY_BUFFER_SIZE = 32 * 1024;

    /**
     * The name of the primary volume (LOLLIPOP).
     */
    private static final String PRIMARY_VOLUME_NAME = "primary";

    /**
     * Get an Uri from an file path.
     *
     * @param path The file path.
     * @return The Uri.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static Uri getUriFromFile(final String path, Context context) {
        ContentResolver resolver = context.getContentResolver();

        Cursor filecursor = resolver.query(MediaStore.Files.getContentUri("external"),
                new String[]{BaseColumns._ID}, MediaStore.MediaColumns.DATA + " = ?",
                new String[]{path}, MediaStore.MediaColumns.DATE_ADDED + " desc");
        filecursor.moveToFirst();

        if (filecursor.isAfterLast()) {
            filecursor.close();
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, path);
            return resolver.insert(MediaStore.Files.getContentUri("external"), values);
        } else {
            @SuppressLint("Range") int imageId = filecursor.getInt(filecursor.getColumnIndex(BaseColumns._ID));
            Uri uri = MediaStore.Files.getContentUri("external").buildUpon().appendPath(
                    Integer.toString(imageId)).build();
            filecursor.close();
            return uri;
        }
    }

    /**
     * Delete all files in a folder.
     *
     * @param folder the folder
     * @return true if successful.
     */
    public static boolean deleteFilesInFolder(final File folder, Context context) {
        boolean totalSuccess = true;

        String[] children = folder.list();
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                File file = new File(folder, children[i]);
                if (!file.isDirectory()) {
                    boolean success = deleteFile(file, context);
                    if (!success) {
                        Log.w(TAG, "Failed to delete file" + children[i]);
                        totalSuccess = false;
                    }
                }
            }
        }
        return totalSuccess;
    }

    /**
     * Determine if a file is on external sd card. (Kitkat or higher.)
     *
     * @param file The file.
     * @return true if on external sd card.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isOnExtSdCard(final File file, Context context) {
        return getExtSdCardFolder(file, context.getApplicationContext()) != null;
    }

    /**
     * Get the full path of a document from its tree URI.
     *
     * @param treeUri The tree RI.
     * @return The path (without trailing file separator).
     */
    public static String getFullPathFromTreeUri(final Uri treeUri, Context context) {
        if (treeUri == null) {
            return null;
        }
        String volumePath = getVolumePath(getVolumeIdFromTreeUri(treeUri), context);
        if (volumePath == null) {
            return File.separator;
        }
        if (volumePath.endsWith(File.separator)) {
            volumePath = volumePath.substring(0, volumePath.length() - 1);
        }

        String documentPath = getDocumentPathFromTreeUri(treeUri);
        if (documentPath.endsWith(File.separator)) {
            documentPath = documentPath.substring(0, documentPath.length() - 1);
        }

        if (documentPath != null && documentPath.length() > 0) {
            if (documentPath.startsWith(File.separator)) {
                return volumePath + documentPath;
            } else {
                return volumePath + File.separator + documentPath;
            }
        } else {
            return volumePath;
        }
    }

    /**
     * Get the document path (relative to volume name) for a tree URI (LOLLIPOP).
     *
     * @param treeUri The tree URI.
     * @return the document path.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String getDocumentPathFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");
        if ((split.length >= 2) && (split[1] != null)) {
            return split[1];
        } else {
            return File.separator;
        }
    }

    /**
     * Determine the main folder of the external SD card containing the given file.
     *
     * @param file the file.
     * @return The main folder of the external SD card containing this file, if the file is on an SD card. Otherwise,
     * null is returned.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getExtSdCardFolder(final File file, Context context) {
        String[] extSdPaths = getExtSdCardPaths(context);
        try {
            for (int i = 0; i < extSdPaths.length; i++) {
                if (file.getCanonicalPath().startsWith(extSdPaths[i])) {
                    return extSdPaths[i];
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    /**
     * Get a list of external SD card paths. (Kitkat or higher.)
     *
     * @return A list of external SD card paths.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String[] getExtSdCardPaths(Context context) {
        List<String> paths = new ArrayList<String>();
        for (File file : context.getExternalFilesDirs("external")) {
            if (file != null && !file.equals(context.getExternalFilesDir("external"))) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w(TAG, "Unexpected external file dir: " + file.getAbsolutePath());
                } else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        return paths.toArray(new String[0]);
    }

    /**
     * Get the volume ID from the tree URI.
     *
     * @param treeUri The tree URI.
     * @return The volume ID.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String getVolumeIdFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");

        if (split.length > 0) {
            return split[0];
        } else {
            return null;
        }
    }

    /**
     * Get the path of a certain volume.
     *
     * @param volumeId The volume id.
     * @return The path.
     */
    private static String getVolumePath(final String volumeId, Context context) {
        if (Build.VERSION.SDK_INT < 21) {
            return null;
        }

        try {
            StorageManager mStorageManager =
                    (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

            Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");

            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getUuid = storageVolumeClazz.getMethod("getUuid");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isPrimary = storageVolumeClazz.getMethod("isPrimary");
            Object result = getVolumeList.invoke(mStorageManager);

            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String uuid = (String) getUuid.invoke(storageVolumeElement);
                Boolean primary = (Boolean) isPrimary.invoke(storageVolumeElement);

                // primary volume?
                if (primary.booleanValue() && PRIMARY_VOLUME_NAME.equals(volumeId)) {
                    return (String) getPath.invoke(storageVolumeElement);
                }

                // other volumes?
                if (uuid != null) {
                    if (uuid.equals(volumeId)) {
                        return (String) getPath.invoke(storageVolumeElement);
                    }
                }
            }

            // not found.
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Check is a file is writable. Detects write issues on external SD card.
     *
     * @param file The file
     * @return true if the file is writable.
     */
    public static boolean isWritable(final File file) {
        boolean isExisting = file.exists();

        try {
            FileOutputStream output = new FileOutputStream(file, true);
            try {
                output.close();
            } catch (IOException e) {
                // do nothing.
            }
        } catch (FileNotFoundException e) {
            return false;
        }
        boolean result = file.canWrite();

        // Ensure that file is not created during this process.
        if (!isExisting) {
            file.delete();
        }

        return result;
    }

    public static boolean delete(String fileOrDir) {
        return delete(new File(fileOrDir));
    }

    public static boolean delete(File fileOrDir) {
        if (fileOrDir.isDirectory()) {
            return deleteDir(fileOrDir);
        } else {
            return deleteFile(fileOrDir);
        }
    }

    public static boolean deleteFile(String file) {
        return deleteFile(new File(file));
    }

    public static boolean deleteFile(File file) {
        boolean result = false;
        if (file != null && file.isFile()) {
            result = file.delete();
        }
        return result;
    }

    /**
     * Delete a file. May be even on external SD card.
     *
     * @param file the file to be deleted.
     * @return True if successfully deleted.
     */
    public static boolean deleteFile(final File file, Context context) {
        // First try the normal deletion.
        boolean res = file.delete();
        if (res) {
            return true;
        }

        // Try the Kitkat workaround.
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            ContentResolver resolver = context.getContentResolver();

            try {
                Uri uri = getUriFromFile(file.getAbsolutePath(), context);
                resolver.delete(uri, null, null);
                return !file.exists();
            } catch (Exception e) {
                Log.e(TAG, "Error when deleting file " + file.getAbsolutePath(), e);
                return false;
            }
        }

        return !file.exists();
    }
}
