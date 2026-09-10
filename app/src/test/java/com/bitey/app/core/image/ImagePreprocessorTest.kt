package com.bitey.app.core.image

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ImagePreprocessorTest {

    // Dummy test double for Context and ExifMetadataExtractor when testing pure calculation
    private val preprocessor = ImagePreprocessor(
        context = object : Context() {
            override fun getAssets() = throw NotImplementedError()
            override fun getResources() = throw NotImplementedError()
            override fun getPackageManager() = throw NotImplementedError()
            override fun getContentResolver() = throw NotImplementedError()
            override fun getMainLooper() = throw NotImplementedError()
            override fun getApplicationContext() = this
            override fun setTheme(resid: Int) {}
            override fun getTheme() = throw NotImplementedError()
            override fun getClassLoader() = throw NotImplementedError()
            override fun getPackageName() = "com.bitey.app"
            override fun getApplicationInfo() = throw NotImplementedError()
            override fun getPackageResourcePath() = throw NotImplementedError()
            override fun getPackageCodePath() = throw NotImplementedError()
            override fun getSharedPreferences(name: String?, mode: Int) = throw NotImplementedError()
            override fun moveSharedPreferencesFrom(sourceContext: Context?, name: String?) = false
            override fun deleteSharedPreferences(name: String?) = false
            override fun openFileInput(name: String?) = throw NotImplementedError()
            override fun openFileOutput(name: String?, mode: Int) = throw NotImplementedError()
            override fun deleteFile(name: String?) = false
            override fun getFileStreamPath(name: String?) = throw NotImplementedError()
            override fun getDataDir() = File("/tmp")
            override fun getFilesDir() = File("/tmp/bitey_test_files")
            override fun getNoBackupFilesDir() = File("/tmp")
            override fun getExternalFilesDir(type: String?) = null
            override fun getExternalFilesDirs(type: String?) = arrayOf<File>()
            override fun getObbDir() = File("/tmp")
            override fun getObbDirs() = arrayOf<File>()
            override fun getCacheDir() = File("/tmp/bitey_test_cache")
            override fun getCodeCacheDir() = File("/tmp")
            override fun getExternalCacheDir() = null
            override fun getExternalCacheDirs() = arrayOf<File>()
            override fun getExternalMediaDirs() = arrayOf<File>()
            override fun fileList() = arrayOf<String>()
            override fun getDir(name: String?, mode: Int) = File("/tmp")
            override fun openOrCreateDatabase(name: String?, mode: Int, factory: android.database.sqlite.SQLiteDatabase.CursorFactory?) = throw NotImplementedError()
            override fun openOrCreateDatabase(name: String?, mode: Int, factory: android.database.sqlite.SQLiteDatabase.CursorFactory?, errorHandler: android.database.DatabaseErrorHandler?) = throw NotImplementedError()
            override fun moveDatabaseFrom(sourceContext: Context?, name: String?) = false
            override fun deleteDatabase(name: String?) = false
            override fun getDatabasePath(name: String?) = throw NotImplementedError()
            override fun databaseList() = arrayOf<String>()
            override fun getWallpaper() = throw NotImplementedError()
            override fun peekWallpaper() = throw NotImplementedError()
            override fun getWallpaperDesiredMinimumWidth() = 0
            override fun getWallpaperDesiredMinimumHeight() = 0
            override fun setWallpaper(bitmap: android.graphics.Bitmap?) {}
            override fun setWallpaper(data: java.io.InputStream?) {}
            override fun clearWallpaper() {}
            override fun registerReceiver(receiver: android.content.BroadcastReceiver?, filter: android.content.IntentFilter?) = null
            override fun registerReceiver(receiver: android.content.BroadcastReceiver?, filter: android.content.IntentFilter?, flags: Int) = null
            override fun registerReceiver(receiver: android.content.BroadcastReceiver?, filter: android.content.IntentFilter?, broadcastPermission: String?, scheduler: android.os.Handler?) = null
            override fun registerReceiver(receiver: android.content.BroadcastReceiver?, filter: android.content.IntentFilter?, broadcastPermission: String?, scheduler: android.os.Handler?, flags: Int) = null
            override fun unregisterReceiver(receiver: android.content.BroadcastReceiver?) {}
            override fun sendBroadcast(intent: android.content.Intent?) {}
            override fun sendBroadcast(intent: android.content.Intent?, receiverPermission: String?) {}
            override fun sendOrderedBroadcast(intent: android.content.Intent?, receiverPermission: String?) {}
            override fun sendOrderedBroadcast(intent: android.content.Intent, receiverPermission: String?, resultReceiver: android.content.BroadcastReceiver?, scheduler: android.os.Handler?, initialCode: Int, initialData: String?, initialExtras: android.os.Bundle?) {}
            override fun sendBroadcastAsUser(intent: android.content.Intent?, user: android.os.UserHandle?) {}
            override fun sendBroadcastAsUser(intent: android.content.Intent?, user: android.os.UserHandle?, receiverPermission: String?) {}
            override fun sendOrderedBroadcastAsUser(intent: android.content.Intent?, user: android.os.UserHandle?, receiverPermission: String?, resultReceiver: android.content.BroadcastReceiver?, scheduler: android.os.Handler?, initialCode: Int, initialData: String?, initialExtras: android.os.Bundle?) {}
            override fun sendStickyBroadcast(intent: android.content.Intent?) {}
            override fun sendStickyOrderedBroadcast(intent: android.content.Intent?, resultReceiver: android.content.BroadcastReceiver?, scheduler: android.os.Handler?, initialCode: Int, initialData: String?, initialExtras: android.os.Bundle?) {}
            override fun removeStickyBroadcast(intent: android.content.Intent?) {}
            override fun sendStickyBroadcastAsUser(intent: android.content.Intent?, user: android.os.UserHandle?) {}
            override fun sendStickyOrderedBroadcastAsUser(intent: android.content.Intent?, user: android.os.UserHandle?, resultReceiver: android.content.BroadcastReceiver?, scheduler: android.os.Handler?, initialCode: Int, initialData: String?, initialExtras: android.os.Bundle?) {}
            override fun removeStickyBroadcastAsUser(intent: android.content.Intent?, user: android.os.UserHandle?) {}
            override fun startService(service: android.content.Intent?) = null
            override fun startForegroundService(service: android.content.Intent?) = null
            override fun stopService(service: android.content.Intent?) = false
            override fun bindService(service: android.content.Intent, conn: android.content.ServiceConnection, flags: Int) = false
            override fun unbindService(conn: android.content.ServiceConnection) {}
            override fun startInstrumentation(className: android.content.ComponentName, profileFile: String?, arguments: android.os.Bundle?) = false
            override fun getSystemService(name: String) = null
            override fun getSystemServiceName(serviceClass: Class<*>) = null
            override fun checkPermission(permission: String, pid: Int, uid: Int) = 0
            override fun checkCallingPermission(permission: String) = 0
            override fun checkCallingOrSelfPermission(permission: String) = 0
            override fun checkSelfPermission(permission: String) = 0
            override fun enforcePermission(permission: String, pid: Int, uid: Int, message: String?) {}
            override fun enforceCallingPermission(permission: String, message: String?) {}
            override fun enforceCallingOrSelfPermission(permission: String, message: String?) {}
            override fun grantUriPermission(toPackage: String?, uri: android.net.Uri?, modeFlags: Int) {}
            override fun revokeUriPermission(uri: android.net.Uri?, modeFlags: Int) {}
            override fun revokeUriPermission(toPackage: String?, uri: android.net.Uri?, modeFlags: Int) {}
            override fun checkUriPermission(uri: android.net.Uri?, pid: Int, uid: Int, modeFlags: Int) = 0
            override fun checkCallingUriPermission(uri: android.net.Uri?, modeFlags: Int) = 0
            override fun checkCallingOrSelfUriPermission(uri: android.net.Uri?, modeFlags: Int) = 0
            override fun checkUriPermission(uri: android.net.Uri?, readPermission: String?, writePermission: String?, pid: Int, uid: Int, modeFlags: Int) = 0
            override fun enforceUriPermission(uri: android.net.Uri?, pid: Int, uid: Int, modeFlags: Int, message: String?) {}
            override fun enforceCallingUriPermission(uri: android.net.Uri?, modeFlags: Int, message: String?) {}
            override fun enforceCallingOrSelfUriPermission(uri: android.net.Uri?, modeFlags: Int, message: String?) {}
            override fun enforceUriPermission(uri: android.net.Uri?, readPermission: String?, writePermission: String?, pid: Int, uid: Int, modeFlags: Int, message: String?) {}
            override fun createPackageContext(packageName: String?, flags: Int) = throw NotImplementedError()
            override fun createContextForSplit(splitName: String?) = throw NotImplementedError()
            override fun createConfigurationContext(overrideConfiguration: android.content.res.Configuration) = throw NotImplementedError()
            override fun createDisplayContext(display: android.view.Display) = throw NotImplementedError()
            override fun createDeviceProtectedStorageContext() = throw NotImplementedError()
            override fun isDeviceProtectedStorage() = false
            override fun startActivity(intent: android.content.Intent?) {}
            override fun startActivity(intent: android.content.Intent?, options: android.os.Bundle?) {}
            override fun startActivities(intents: Array<out android.content.Intent>?) {}
            override fun startActivities(intents: Array<out android.content.Intent>?, options: android.os.Bundle?) {}
            override fun startIntentSender(intent: android.content.IntentSender?, fillInIntent: android.content.Intent?, flagsMask: Int, flagsValues: Int, extraFlags: Int) {}
            override fun startIntentSender(intent: android.content.IntentSender?, fillInIntent: android.content.Intent?, flagsMask: Int, flagsValues: Int, extraFlags: Int, options: android.os.Bundle?) {}
        },
        exifMetadataExtractor = ExifMetadataExtractor(object : Context() {
            override fun getAssets() = throw NotImplementedError()
            override fun getResources() = throw NotImplementedError()
            override fun getPackageManager() = throw NotImplementedError()
            override fun getContentResolver() = throw NotImplementedError()
            override fun getMainLooper() = throw NotImplementedError()
            override fun getApplicationContext() = this
            override fun setTheme(resid: Int) {}
            override fun getTheme() = throw NotImplementedError()
            override fun getClassLoader() = throw NotImplementedError()
            override fun getPackageName() = "com.bitey.app"
            override fun getApplicationInfo() = throw NotImplementedError()
            override fun getPackageResourcePath() = throw NotImplementedError()
            override fun getPackageCodePath() = throw NotImplementedError()
            override fun getSharedPreferences(name: String?, mode: Int) = throw NotImplementedError()
            override fun moveSharedPreferencesFrom(sourceContext: Context?, name: String?) = false
            override fun deleteSharedPreferences(name: String?) = false
            override fun openFileInput(name: String?) = throw NotImplementedError()
            override fun openFileOutput(name: String?, mode: Int) = throw NotImplementedError()
            override fun deleteFile(name: String?) = false
            override fun getFileStreamPath(name: String?) = throw NotImplementedError()
            override fun getDataDir() = File("/tmp")
            override fun getFilesDir() = File("/tmp")
            override fun getNoBackupFilesDir() = File("/tmp")
            override fun getExternalFilesDir(type: String?) = null
            override fun getExternalFilesDirs(type: String?) = arrayOf<File>()
            override fun getObbDir() = File("/tmp")
            override fun getObbDirs() = arrayOf<File>()
            override fun getCacheDir() = File("/tmp")
            override fun getCodeCacheDir() = File("/tmp")
            override fun getExternalCacheDir() = null
            override fun getExternalCacheDirs() = arrayOf<File>()
            override fun getExternalMediaDirs() = arrayOf<File>()
            override fun fileList() = arrayOf<String>()
            override fun getDir(name: String?, mode: Int) = File("/tmp")
            override fun openOrCreateDatabase(name: String?, mode: Int, factory: android.database.sqlite.SQLiteDatabase.CursorFactory?) = throw NotImplementedError()
            override fun openOrCreateDatabase(name: String?, mode: Int, factory: android.database.sqlite.SQLiteDatabase.CursorFactory?, errorHandler: android.database.DatabaseErrorHandler?) = throw NotImplementedError()
            override fun moveDatabaseFrom(sourceContext: Context?, name: String?) = false
            override fun deleteDatabase(name: String?) = false
            override fun getDatabasePath(name: String?) = throw NotImplementedError()
            override fun databaseList() = arrayOf<String>()
            override fun getWallpaper() = throw NotImplementedError()
            override fun peekWallpaper() = throw NotImplementedError()
            override fun getWallpaperDesiredMinimumWidth() = 0
            override fun getWallpaperDesiredMinimumHeight() = 0
            override fun setWallpaper(bitmap: android.graphics.Bitmap?) {}
            override fun setWallpaper(data: java.io.InputStream?) {}
            override fun clearWallpaper() {}
            override fun registerReceiver(receiver: android.content.BroadcastReceiver?, filter: android.content.IntentFilter?) = null
            override fun registerReceiver(receiver: android.content.BroadcastReceiver?, filter: android.content.IntentFilter?, flags: Int) = null
            override fun registerReceiver(receiver: android.content.BroadcastReceiver?, filter: android.content.IntentFilter?, broadcastPermission: String?, scheduler: android.os.Handler?) = null
            override fun registerReceiver(receiver: android.content.BroadcastReceiver?, filter: android.content.IntentFilter?, broadcastPermission: String?, scheduler: android.os.Handler?, flags: Int) = null
            override fun unregisterReceiver(receiver: android.content.BroadcastReceiver?) {}
            override fun sendBroadcast(intent: android.content.Intent?) {}
            override fun sendBroadcast(intent: android.content.Intent?, receiverPermission: String?) {}
            override fun sendOrderedBroadcast(intent: android.content.Intent?, receiverPermission: String?) {}
            override fun sendOrderedBroadcast(intent: android.content.Intent, receiverPermission: String?, resultReceiver: android.content.BroadcastReceiver?, scheduler: android.os.Handler?, initialCode: Int, initialData: String?, initialExtras: android.os.Bundle?) {}
            override fun sendBroadcastAsUser(intent: android.content.Intent?, user: android.os.UserHandle?) {}
            override fun sendBroadcastAsUser(intent: android.content.Intent?, user: android.os.UserHandle?, receiverPermission: String?) {}
            override fun sendOrderedBroadcastAsUser(intent: android.content.Intent?, user: android.os.UserHandle?, receiverPermission: String?, resultReceiver: android.content.BroadcastReceiver?, scheduler: android.os.Handler?, initialCode: Int, initialData: String?, initialExtras: android.os.Bundle?) {}
            override fun sendStickyBroadcast(intent: android.content.Intent?) {}
            override fun sendStickyOrderedBroadcast(intent: android.content.Intent?, resultReceiver: android.content.BroadcastReceiver?, scheduler: android.os.Handler?, initialCode: Int, initialData: String?, initialExtras: android.os.Bundle?) {}
            override fun removeStickyBroadcast(intent: android.content.Intent?) {}
            override fun sendStickyBroadcastAsUser(intent: android.content.Intent?, user: android.os.UserHandle?) {}
            override fun sendStickyOrderedBroadcastAsUser(intent: android.content.Intent?, user: android.os.UserHandle?, resultReceiver: android.content.BroadcastReceiver?, scheduler: android.os.Handler?, initialCode: Int, initialData: String?, initialExtras: android.os.Bundle?) {}
            override fun removeStickyBroadcastAsUser(intent: android.content.Intent?, user: android.os.UserHandle?) {}
            override fun startService(service: android.content.Intent?) = null
            override fun startForegroundService(service: android.content.Intent?) = null
            override fun stopService(service: android.content.Intent?) = false
            override fun bindService(service: android.content.Intent, conn: android.content.ServiceConnection, flags: Int) = false
            override fun unbindService(conn: android.content.ServiceConnection) {}
            override fun startInstrumentation(className: android.content.ComponentName, profileFile: String?, arguments: android.os.Bundle?) = false
            override fun getSystemService(name: String) = null
            override fun getSystemServiceName(serviceClass: Class<*>) = null
            override fun checkPermission(permission: String, pid: Int, uid: Int) = 0
            override fun checkCallingPermission(permission: String) = 0
            override fun checkCallingOrSelfPermission(permission: String) = 0
            override fun checkSelfPermission(permission: String) = 0
            override fun enforcePermission(permission: String, pid: Int, uid: Int, message: String?) {}
            override fun enforceCallingPermission(permission: String, message: String?) {}
            override fun enforceCallingOrSelfPermission(permission: String, message: String?) {}
            override fun grantUriPermission(toPackage: String?, uri: android.net.Uri?, modeFlags: Int) {}
            override fun revokeUriPermission(uri: android.net.Uri?, modeFlags: Int) {}
            override fun revokeUriPermission(toPackage: String?, uri: android.net.Uri?, modeFlags: Int) {}
            override fun checkUriPermission(uri: android.net.Uri?, pid: Int, uid: Int, modeFlags: Int) = 0
            override fun checkCallingUriPermission(uri: android.net.Uri?, modeFlags: Int) = 0
            override fun checkCallingOrSelfUriPermission(uri: android.net.Uri?, modeFlags: Int) = 0
            override fun checkUriPermission(uri: android.net.Uri?, readPermission: String?, writePermission: String?, pid: Int, uid: Int, modeFlags: Int) = 0
            override fun enforceUriPermission(uri: android.net.Uri?, pid: Int, uid: Int, modeFlags: Int, message: String?) {}
            override fun enforceCallingUriPermission(uri: android.net.Uri?, modeFlags: Int, message: String?) {}
            override fun enforceCallingOrSelfUriPermission(uri: android.net.Uri?, modeFlags: Int, message: String?) {}
            override fun enforceUriPermission(uri: android.net.Uri?, readPermission: String?, writePermission: String?, pid: Int, uid: Int, modeFlags: Int, message: String?) {}
            override fun createPackageContext(packageName: String?, flags: Int) = throw NotImplementedError()
            override fun createContextForSplit(splitName: String?) = throw NotImplementedError()
            override fun createConfigurationContext(overrideConfiguration: android.content.res.Configuration) = throw NotImplementedError()
            override fun createDisplayContext(display: android.view.Display) = throw NotImplementedError()
            override fun createDeviceProtectedStorageContext() = throw NotImplementedError()
            override fun isDeviceProtectedStorage() = false
            override fun startActivity(intent: android.content.Intent?) {}
            override fun startActivity(intent: android.content.Intent?, options: android.os.Bundle?) {}
            override fun startActivities(intents: Array<out android.content.Intent>?) {}
            override fun startActivities(intents: Array<out android.content.Intent>?, options: android.os.Bundle?) {}
            override fun startIntentSender(intent: android.content.IntentSender?, fillInIntent: android.content.Intent?, flagsMask: Int, flagsValues: Int, extraFlags: Int) {}
            override fun startIntentSender(intent: android.content.IntentSender?, fillInIntent: android.content.Intent?, flagsMask: Int, flagsValues: Int, extraFlags: Int, options: android.os.Bundle?) {}
        })
    )

    @Test
    fun calculateInSampleSize_smallerThanTarget_returns1() {
        val sampleSize = preprocessor.calculateInSampleSize(800, 600, 1920, 1080)
        assertEquals(1, sampleSize)
    }

    @Test
    fun calculateInSampleSize_exactTarget_returns1() {
        val sampleSize = preprocessor.calculateInSampleSize(1920, 1080, 1920, 1080)
        assertEquals(1, sampleSize)
    }

    @Test
    fun calculateInSampleSize_12Megapixels_returnsPowerOfTwo() {
        // 4032 x 3024 (standard 12MP smartphone photo)
        val sampleSize = preprocessor.calculateInSampleSize(4032, 3024, 1920, 1080)
        assertEquals(2, sampleSize)
        // 4032 / 2 = 2016, 3024 / 2 = 1512
    }

    @Test
    fun calculateInSampleSize_48Megapixels_returns4() {
        // 8000 x 6000 (standard 48MP smartphone photo)
        val sampleSize = preprocessor.calculateInSampleSize(8000, 6000, 1920, 1080)
        assertEquals(4, sampleSize)
        // 8000 / 4 = 2000, 6000 / 4 = 1500
    }
}
