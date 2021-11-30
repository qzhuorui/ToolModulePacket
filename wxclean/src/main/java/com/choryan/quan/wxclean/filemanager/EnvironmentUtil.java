package com.choryan.quan.wxclean.filemanager;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class EnvironmentUtil {
    private static final String TAG = EnvironmentUtil.class.getSimpleName();
    public static final String EXTERNAL_STORAGE = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static String TRUE_EXTERNAL_STORAGE;

    public static void initExternalStoragePath(Context context) {
        TRUE_EXTERNAL_STORAGE = getTrueExternalStorage(context);
    }

    public static String[] getExternalStorageArray() {
        if (EXTERNAL_STORAGE.equals(TRUE_EXTERNAL_STORAGE) || TextUtils.isEmpty(TRUE_EXTERNAL_STORAGE)) {
            String[] array = new String[1];
            array[0] = EXTERNAL_STORAGE;
            return array;
        } else {
            String[] array = new String[2];
            array[0] = EXTERNAL_STORAGE;
            array[1] = TRUE_EXTERNAL_STORAGE;
            return array;
        }
    }

    private static String getTrueExternalStorage(Context context) {
        int sdk = Build.VERSION.SDK_INT;
        if (sdk < 14) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            String secStore = getStoragePath(context, true);
            if (!TextUtils.isEmpty(secStore)) {
                return secStore;
            }
            return "";
        }
    }

    private static long StatFs(String file) {
        long total = 0;
        try {
            StatFs stat = new StatFs(file);
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            total = totalBlocks * blockSize;
        } catch (Exception e) {
        }
        return total;
    }

    private static boolean isExist(String paramString) {
        File localFile = new File(paramString);
        return localFile.exists();
    }

    public static boolean isPathWriteable(String path) {
        String uri = path + "/.writeable";

        FileUtil.ensureDir(path);
        FileUtil.ensureFile(uri);

        return FileUtil.exists(uri);
    }

    //http://stackoverflow.com/questions/11281010/how-can-i-get-external-sd-card-path-for-android-4-0/18871043#18871043
    private static final Pattern DIR_SEPORATOR = Pattern.compile("/");

    /**
     * Raturns all available SD-Cards in the system (include emulated)
     * <p>
     * Warning: Hack! Based on Android source code of version 4.3 (API 18)
     * Because there is no standart way to get it.
     * TODO: Test on future Android versions 4.4+
     *
     * @return paths to all available SD-Cards in the system (include emulated)
     */
    public static String[] getStorageDirectories() {
        // Final set of paths
        final Set<String> rv = new HashSet<String>();
        // Primary physical SD-CARD (not emulated)
        final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
        // All Secondary SD-CARDs (all exclude primary) separated by ":"
        final String rawSecondaryStoragesStr = System
                .getenv("SECONDARY_STORAGE");
        // Primary emulated SD-CARD
        final String rawEmulatedStorageTarget = System
                .getenv("EMULATED_STORAGE_TARGET");
        if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
            // Device has physical external storage; use plain paths.
            if (TextUtils.isEmpty(rawExternalStorage)) {
                // EXTERNAL_STORAGE undefined; falling back to default.
                rv.add("/storage/sdcard0");
            } else {
                rv.add(rawExternalStorage);
            }
        } else {
            // Device has emulated storage; external storage paths should have
            // userId burned into them.
            final String rawUserId;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                rawUserId = "";
            } else {
                final String path = Environment.getExternalStorageDirectory()
                        .getAbsolutePath();
                final String[] folders = DIR_SEPORATOR.split(path);
                final String lastFolder = folders[folders.length - 1];
                boolean isDigit = false;
                try {
                    Integer.valueOf(lastFolder);
                    isDigit = true;
                } catch (NumberFormatException ignored) {
                }
                rawUserId = isDigit ? lastFolder : "";
            }
            // /storage/emulated/0[1,2,...]
            if (TextUtils.isEmpty(rawUserId)) {
                rv.add(rawEmulatedStorageTarget);
            } else {
                rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
            }
        }
        // Add all secondary storages
        if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
            // All Secondary SD-CARDs splited into array
            final String[] rawSecondaryStorages = rawSecondaryStoragesStr
                    .split(File.pathSeparator);
            Collections.addAll(rv, rawSecondaryStorages);
        }
        return rv.toArray(new String[rv.size()]);
    }

    //false internal
    // true ex
    public static String getStoragePath(Context mContext, boolean canRemove) {

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (canRemove == removable && checkSDCardMount(mContext, path)) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    //判断sdcard是否挂载上，返回值为true证明挂载上了，否则不存在
    protected static boolean checkSDCardMount(Context context, String mountPoint) {
        if (mountPoint == null) {
            return false;
        }
        String state = null;
        try {
            Method getVolumeStateMethod = StorageManager.class.getMethod("getVolumeState", new Class[]{String.class});
            state = (String) getVolumeStateMethod.invoke(getSM(context), mountPoint);
        } catch (Exception e) {
            Log.e("", "getStorageState() failed", e);
        }
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static StorageManager getSM(Context context) {
        if (context != null) {
            return (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        } else {
            return null;
        }
    }
}