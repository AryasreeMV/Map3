package com.example.user.map3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.Instrumentation;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.app.Service;
import android.app.UiModeManager;
import android.app.WallpaperManager;
import android.app.backup.BackupAgent;
import android.app.job.JobScheduler;
import android.app.usage.NetworkStatsManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.DefaultDatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.location.LocationListener;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;

import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaRouter;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.IpSecManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.aware.WifiAwareManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HardwarePropertiesManager;
import android.os.Looper;
import android.os.PowerManager;
import android.os.StatFs;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


public class GPSTracker extends Context implements LocationListener {
    private final Context mContext;

    // flag for GPS status
    public boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    public GPSTracker(Context context) {
        this.mContext = context;
        getLocation();
    }

    /**
     * Function to get the user's current location
     *
     * @return
     */
    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(Context.LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            Log.v("isGPSEnabled", "=" + isGPSEnabled);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            Log.v("isNetworkEnabled", "=" + isNetworkEnabled);

            if (isGPSEnabled == false && isNetworkEnabled == false) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                   // location = null;
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.

                        return  location;
                    }
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    location=null;
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    /**
     * Stop using GPS listener Calling this function will stop using GPS in your
     * app
     * */
    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    /**
     * Function to get latitude
     * */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     * */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    /**
     * Function to check GPS/wifi enabled
     *
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog On pressing Settings button will
     * lauch Settings Options
     * */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog
                .setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        mContext.startActivity(intent);
                    }
                });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    /**
     * Returns an AssetManager instance for the application's package.
     * <p>
     * <strong>Note:</strong> Implementations of this method should return
     * an AssetManager instance that is consistent with the Resources instance
     * returned by {@link #getResources()}. For example, they should share the
     * same {@link Configuration} object.
     *
     * @return an AssetManager instance for the application's package
     * @see #getResources()
     */
    @Override
    public AssetManager getAssets() {
        return null;
    }

    /**
     * Returns a Resources instance for the application's package.
     * <p>
     * <strong>Note:</strong> Implementations of this method should return
     * a Resources instance that is consistent with the AssetManager instance
     * returned by {@link #getAssets()}. For example, they should share the
     * same {@link Configuration} object.
     *
     * @return a Resources instance for the application's package
     * @see #getAssets()
     */
    @Override
    public Resources getResources() {
        return null;
    }

    /**
     * Return PackageManager instance to find global package information.
     */
    @Override
    public PackageManager getPackageManager() {
        return null;
    }

    /**
     * Return a ContentResolver instance for your application's package.
     */
    @Override
    public ContentResolver getContentResolver() {
        return null;
    }

    /**
     * Return the Looper for the main thread of the current process.  This is
     * the thread used to dispatch calls to application components (activities,
     * services, etc).
     * <p>
     * By definition, this method returns the same result as would be obtained
     * by calling {@link Looper#getMainLooper() Looper.getMainLooper()}.
     * </p>
     *
     * @return The main looper.
     */
    @Override
    public Looper getMainLooper() {
        return null;
    }

    /**
     * Return the context of the single, global Application object of the
     * current process.  This generally should only be used if you need a
     * Context whose lifecycle is separate from the current context, that is
     * tied to the lifetime of the process rather than the current component.
     * <p>
     * <p>Consider for example how this interacts with
     * {@link #registerReceiver(BroadcastReceiver, IntentFilter)}:
     * <ul>
     * <li> <p>If used from an Activity context, the receiver is being registered
     * within that activity.  This means that you are expected to unregister
     * before the activity is done being destroyed; in fact if you do not do
     * so, the framework will clean up your leaked registration as it removes
     * the activity and log an error.  Thus, if you use the Activity context
     * to register a receiver that is static (global to the process, not
     * associated with an Activity instance) then that registration will be
     * removed on you at whatever point the activity you used is destroyed.
     * <li> <p>If used from the Context returned here, the receiver is being
     * registered with the global state associated with your application.  Thus
     * it will never be unregistered for you.  This is necessary if the receiver
     * is associated with static data, not a particular component.  However
     * using the ApplicationContext elsewhere can easily lead to serious leaks
     * if you forget to unregister, unbind, etc.
     * </ul>
     */
    @Override
    public Context getApplicationContext() {
        return null;
    }

    /**
     * Set the base theme for this context.  Note that this should be called
     * before any views are instantiated in the Context (for example before
     * calling {@link Activity#setContentView} or
     * {@link LayoutInflater#inflate}).
     *
     * @param resid The style resource describing the theme.
     */
    @Override
    public void setTheme(int resid) {

    }

    /**
     * Return the Theme object associated with this Context.
     */
    @Override
    public Resources.Theme getTheme() {
        return null;
    }

    /**
     * Return a class loader you can use to retrieve classes in this package.
     */
    @Override
    public ClassLoader getClassLoader() {
        return null;
    }

    /**
     * Return the name of this application's package.
     */
    @Override
    public String getPackageName() {
        return null;
    }

    /**
     * Return the full application info for this context's package.
     */
    @Override
    public ApplicationInfo getApplicationInfo() {
        return null;
    }

    /**
     * Return the full path to this context's primary Android package.
     * The Android package is a ZIP file which contains the application's
     * primary resources.
     * <p>
     * <p>Note: this is not generally useful for applications, since they should
     * not be directly accessing the file system.
     *
     * @return String Path to the resources.
     */
    @Override
    public String getPackageResourcePath() {
        return null;
    }

    /**
     * Return the full path to this context's primary Android package.
     * The Android package is a ZIP file which contains application's
     * primary code and assets.
     * <p>
     * <p>Note: this is not generally useful for applications, since they should
     * not be directly accessing the file system.
     *
     * @return String Path to the code and assets.
     */
    @Override
    public String getPackageCodePath() {
        return null;
    }

    /**
     * Retrieve and hold the contents of the preferences file 'name', returning
     * a SharedPreferences through which you can retrieve and modify its
     * values.  Only one instance of the SharedPreferences object is returned
     * to any callers for the same name, meaning they will see each other's
     * edits as soon as they are made.
     * <p>
     * This method is thead-safe.
     *
     * @param name Desired preferences file. If a preferences file by this name
     *             does not exist, it will be created when you retrieve an
     *             editor (SharedPreferences.edit()) and then commit changes (Editor.commit()).
     * @param mode Operating mode.
     * @return The single {@link SharedPreferences} instance that can be used
     * to retrieve and modify the preference values.
     * @see #MODE_PRIVATE
     */
    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return null;
    }

    /**
     * Move an existing shared preferences file from the given source storage
     * context to this context. This is typically used to migrate data between
     * storage locations after an upgrade, such as moving to device protected
     * storage.
     *
     * @param sourceContext The source context which contains the existing
     *                      shared preferences to move.
     * @param name          The name of the shared preferences file.
     * @return {@code true} if the move was successful or if the shared
     * preferences didn't exist in the source context, otherwise
     * {@code false}.
     * @see #createDeviceProtectedStorageContext()
     */
    @Override
    public boolean moveSharedPreferencesFrom(Context sourceContext, String name) {
        return false;
    }

    /**
     * Delete an existing shared preferences file.
     *
     * @param name The name (unique in the application package) of the shared
     *             preferences file.
     * @return {@code true} if the shared preferences file was successfully
     * deleted; else {@code false}.
     * @see #getSharedPreferences(String, int)
     */
    @Override
    public boolean deleteSharedPreferences(String name) {
        return false;
    }

    /**
     * Open a private file associated with this Context's application package
     * for reading.
     *
     * @param name The name of the file to open; can not contain path
     *             separators.
     * @return The resulting {@link FileInputStream}.
     * @see #openFileOutput
     * @see #fileList
     * @see #deleteFile
     * @see FileInputStream#FileInputStream(String)
     */
    @Override
    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        return null;
    }

    /**
     * Open a private file associated with this Context's application package
     * for writing. Creates the file if it doesn't already exist.
     * <p>
     * No additional permissions are required for the calling app to read or
     * write the returned file.
     *
     * @param name The name of the file to open; can not contain path
     *             separators.
     * @param mode Operating mode.
     * @return The resulting {@link FileOutputStream}.
     * @see #MODE_APPEND
     * @see #MODE_PRIVATE
     * @see #openFileInput
     * @see #fileList
     * @see #deleteFile
     * @see FileOutputStream#FileOutputStream(String)
     */
    @Override
    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        return null;
    }

    /**
     * Delete the given private file associated with this Context's
     * application package.
     *
     * @param name The name of the file to delete; can not contain path
     *             separators.
     * @return {@code true} if the file was successfully deleted; else
     * {@code false}.
     * @see #openFileInput
     * @see #openFileOutput
     * @see #fileList
     * @see File#delete()
     */
    @Override
    public boolean deleteFile(String name) {
        return false;
    }

    /**
     * Returns the absolute path on the filesystem where a file created with
     * {@link #openFileOutput} is stored.
     * <p>
     * The returned path may change over time if the calling app is moved to an
     * adopted storage device, so only relative paths should be persisted.
     *
     * @param name The name of the file for which you would like to get
     *             its path.
     * @return An absolute path to the given file.
     * @see #openFileOutput
     * @see #getFilesDir
     * @see #getDir
     */
    @Override
    public File getFileStreamPath(String name) {
        return null;
    }

    /**
     * Returns the absolute path to the directory on the filesystem where all
     * private files belonging to this app are stored. Apps should not use this
     * path directly; they should instead use {@link #getFilesDir()},
     * {@link #getCacheDir()}, {@link #getDir(String, int)}, or other storage
     * APIs on this class.
     * <p>
     * The returned path may change over time if the calling app is moved to an
     * adopted storage device, so only relative paths should be persisted.
     * <p>
     * No additional permissions are required for the calling app to read or
     * write files under the returned path.
     *
     * @see ApplicationInfo#dataDir
     */
    @Override
    public File getDataDir() {
        return null;
    }

    /**
     * Returns the absolute path to the directory on the filesystem where files
     * created with {@link #openFileOutput} are stored.
     * <p>
     * The returned path may change over time if the calling app is moved to an
     * adopted storage device, so only relative paths should be persisted.
     * <p>
     * No additional permissions are required for the calling app to read or
     * write files under the returned path.
     *
     * @return The path of the directory holding application files.
     * @see #openFileOutput
     * @see #getFileStreamPath
     * @see #getDir
     */
    @Override
    public File getFilesDir() {
        return null;
    }

    /**
     * Returns the absolute path to the directory on the filesystem similar to
     * {@link #getFilesDir()}. The difference is that files placed under this
     * directory will be excluded from automatic backup to remote storage. See
     * {@link BackupAgent BackupAgent} for a full discussion
     * of the automatic backup mechanism in Android.
     * <p>
     * The returned path may change over time if the calling app is moved to an
     * adopted storage device, so only relative paths should be persisted.
     * <p>
     * No additional permissions are required for the calling app to read or
     * write files under the returned path.
     *
     * @return The path of the directory holding application files that will not
     * be automatically backed up to remote storage.
     * @see #openFileOutput
     * @see #getFileStreamPath
     * @see #getDir
     * @see BackupAgent
     */
    @Override
    public File getNoBackupFilesDir() {
        return null;
    }

    /**
     * Returns the absolute path to the directory on the primary shared/external
     * storage device where the application can place persistent files it owns.
     * These files are internal to the applications, and not typically visible
     * to the user as media.
     * <p>
     * This is like {@link #getFilesDir()} in that these files will be deleted
     * when the application is uninstalled, however there are some important
     * differences:
     * <ul>
     * <li>Shared storage may not always be available, since removable media can
     * be ejected by the user. Media state can be checked using
     * {@link Environment#getExternalStorageState(File)}.
     * <li>There is no security enforced with these files. For example, any
     * application holding
     * {@link Manifest.permission#WRITE_EXTERNAL_STORAGE} can write to
     * these files.
     * </ul>
     * <p>
     * If a shared storage device is emulated (as determined by
     * {@link Environment#isExternalStorageEmulated(File)}), it's contents are
     * backed by a private user data partition, which means there is little
     * benefit to storing data here instead of the private directories returned
     * by {@link #getFilesDir()}, etc.
     * <p>
     * Starting in {@link Build.VERSION_CODES#KITKAT}, no permissions
     * are required to read or write to the returned path; it's always
     * accessible to the calling app. This only applies to paths generated for
     * package name of the calling application. To access paths belonging to
     * other packages,
     * {@link Manifest.permission#WRITE_EXTERNAL_STORAGE} and/or
     * {@link Manifest.permission#READ_EXTERNAL_STORAGE} are required.
     * <p>
     * On devices with multiple users (as described by {@link UserManager}),
     * each user has their own isolated shared storage. Applications only have
     * access to the shared storage for the user they're running as.
     * <p>
     * The returned path may change over time if different shared storage media
     * is inserted, so only relative paths should be persisted.
     * <p>
     * Here is an example of typical code to manipulate a file in an
     * application's shared storage:
     * </p>
     * {@sample development/samples/ApiDemos/src/com/example/android/apis/content/ExternalStorage.java
     * private_file}
     * <p>
     * If you supply a non-null <var>type</var> to this function, the returned
     * file will be a path to a sub-directory of the given type. Though these
     * files are not automatically scanned by the media scanner, you can
     * explicitly add them to the media database with
     * {@link MediaScannerConnection#scanFile(Context, String[], String[], MediaScannerConnection.OnScanCompletedListener)
     * MediaScannerConnection.scanFile}. Note that this is not the same as
     * {@link Environment#getExternalStoragePublicDirectory
     * Environment.getExternalStoragePublicDirectory()}, which provides
     * directories of media shared by all applications. The directories returned
     * here are owned by the application, and their contents will be removed
     * when the application is uninstalled. Unlike
     * {@link Environment#getExternalStoragePublicDirectory
     * Environment.getExternalStoragePublicDirectory()}, the directory returned
     * here will be automatically created for you.
     * <p>
     * Here is an example of typical code to manipulate a picture in an
     * application's shared storage and add it to the media database:
     * </p>
     * {@sample development/samples/ApiDemos/src/com/example/android/apis/content/ExternalStorage.java
     * private_picture}
     *
     * @param type The type of files directory to return. May be {@code null}
     *             for the root of the files directory or one of the following
     *             constants for a subdirectory:
     *             {@link Environment#DIRECTORY_MUSIC},
     *             {@link Environment#DIRECTORY_PODCASTS},
     *             {@link Environment#DIRECTORY_RINGTONES},
     *             {@link Environment#DIRECTORY_ALARMS},
     *             {@link Environment#DIRECTORY_NOTIFICATIONS},
     *             {@link Environment#DIRECTORY_PICTURES}, or
     *             {@link Environment#DIRECTORY_MOVIES}.
     * @return the absolute path to application-specific directory. May return
     * {@code null} if shared storage is not currently available.
     * @see #getFilesDir
     * @see #getExternalFilesDirs(String)
     * @see Environment#getExternalStorageState(File)
     * @see Environment#isExternalStorageEmulated(File)
     * @see Environment#isExternalStorageRemovable(File)
     */
    @Nullable
    @Override
    public File getExternalFilesDir(@Nullable String type) {
        return null;
    }

    /**
     * Returns absolute paths to application-specific directories on all
     * shared/external storage devices where the application can place
     * persistent files it owns. These files are internal to the application,
     * and not typically visible to the user as media.
     * <p>
     * This is like {@link #getFilesDir()} in that these files will be deleted
     * when the application is uninstalled, however there are some important
     * differences:
     * <ul>
     * <li>Shared storage may not always be available, since removable media can
     * be ejected by the user. Media state can be checked using
     * {@link Environment#getExternalStorageState(File)}.
     * <li>There is no security enforced with these files. For example, any
     * application holding
     * {@link Manifest.permission#WRITE_EXTERNAL_STORAGE} can write to
     * these files.
     * </ul>
     * <p>
     * If a shared storage device is emulated (as determined by
     * {@link Environment#isExternalStorageEmulated(File)}), it's contents are
     * backed by a private user data partition, which means there is little
     * benefit to storing data here instead of the private directories returned
     * by {@link #getFilesDir()}, etc.
     * <p>
     * Shared storage devices returned here are considered a stable part of the
     * device, including physical media slots under a protective cover. The
     * returned paths do not include transient devices, such as USB flash drives
     * connected to handheld devices.
     * <p>
     * An application may store data on any or all of the returned devices. For
     * example, an app may choose to store large files on the device with the
     * most available space, as measured by {@link StatFs}.
     * <p>
     * No additional permissions are required for the calling app to read or
     * write files under the returned path. Write access outside of these paths
     * on secondary external storage devices is not available.
     * <p>
     * The returned path may change over time if different shared storage media
     * is inserted, so only relative paths should be persisted.
     *
     * @param type The type of files directory to return. May be {@code null}
     *             for the root of the files directory or one of the following
     *             constants for a subdirectory:
     *             {@link Environment#DIRECTORY_MUSIC},
     *             {@link Environment#DIRECTORY_PODCASTS},
     *             {@link Environment#DIRECTORY_RINGTONES},
     *             {@link Environment#DIRECTORY_ALARMS},
     *             {@link Environment#DIRECTORY_NOTIFICATIONS},
     *             {@link Environment#DIRECTORY_PICTURES}, or
     *             {@link Environment#DIRECTORY_MOVIES}.
     * @return the absolute paths to application-specific directories. Some
     * individual paths may be {@code null} if that shared storage is
     * not currently available. The first path returned is the same as
     * {@link #getExternalFilesDir(String)}.
     * @see #getExternalFilesDir(String)
     * @see Environment#getExternalStorageState(File)
     * @see Environment#isExternalStorageEmulated(File)
     * @see Environment#isExternalStorageRemovable(File)
     */
    @Override
    public File[] getExternalFilesDirs(String type) {
        return new File[0];
    }

    /**
     * Return the primary shared/external storage directory where this
     * application's OBB files (if there are any) can be found. Note if the
     * application does not have any OBB files, this directory may not exist.
     * <p>
     * This is like {@link #getFilesDir()} in that these files will be deleted
     * when the application is uninstalled, however there are some important
     * differences:
     * <ul>
     * <li>Shared storage may not always be available, since removable media can
     * be ejected by the user. Media state can be checked using
     * {@link Environment#getExternalStorageState(File)}.
     * <li>There is no security enforced with these files. For example, any
     * application holding
     * {@link Manifest.permission#WRITE_EXTERNAL_STORAGE} can write to
     * these files.
     * </ul>
     * <p>
     * Starting in {@link Build.VERSION_CODES#KITKAT}, no permissions
     * are required to read or write to the path that this method returns.
     * However, starting from {@link Build.VERSION_CODES#M},
     * to read the OBB expansion files, you must declare the
     * {@link Manifest.permission#READ_EXTERNAL_STORAGE} permission in the app manifest and ask for
     * permission at runtime as follows:
     * </p>
     * <p>
     * {@code <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
     * android:maxSdkVersion="23" />}
     * </p>
     * <p>
     * Starting from {@link Build.VERSION_CODES#N},
     * {@link Manifest.permission#READ_EXTERNAL_STORAGE}
     * permission is not required, so don’t ask for this
     * permission at runtime. To handle both cases, your app must first try to read the OBB file,
     * and if it fails, you must request
     * {@link Manifest.permission#READ_EXTERNAL_STORAGE} permission at runtime.
     * </p>
     * <p>
     * <p>
     * The following code snippet shows how to do this:
     * </p>
     * <p>
     * <pre>
     * File obb = new File(obb_filename);
     * boolean open_failed = false;
     *
     * try {
     *     BufferedReader br = new BufferedReader(new FileReader(obb));
     *     open_failed = false;
     *     ReadObbFile(br);
     * } catch (IOException e) {
     *     open_failed = true;
     * }
     *
     * if (open_failed) {
     *     // request READ_EXTERNAL_STORAGE permission before reading OBB file
     *     ReadObbFileWithPermission();
     * }
     * </pre>
     * <p>
     * On devices with multiple users (as described by {@link UserManager}),
     * multiple users may share the same OBB storage location. Applications
     * should ensure that multiple instances running under different users don't
     * interfere with each other.
     *
     * @return the absolute path to application-specific directory. May return
     * {@code null} if shared storage is not currently available.
     * @see #getObbDirs()
     * @see Environment#getExternalStorageState(File)
     * @see Environment#isExternalStorageEmulated(File)
     * @see Environment#isExternalStorageRemovable(File)
     */
    @Override
    public File getObbDir() {
        return null;
    }

    /**
     * Returns absolute paths to application-specific directories on all
     * shared/external storage devices where the application's OBB files (if
     * there are any) can be found. Note if the application does not have any
     * OBB files, these directories may not exist.
     * <p>
     * This is like {@link #getFilesDir()} in that these files will be deleted
     * when the application is uninstalled, however there are some important
     * differences:
     * <ul>
     * <li>Shared storage may not always be available, since removable media can
     * be ejected by the user. Media state can be checked using
     * {@link Environment#getExternalStorageState(File)}.
     * <li>There is no security enforced with these files. For example, any
     * application holding
     * {@link Manifest.permission#WRITE_EXTERNAL_STORAGE} can write to
     * these files.
     * </ul>
     * <p>
     * Shared storage devices returned here are considered a stable part of the
     * device, including physical media slots under a protective cover. The
     * returned paths do not include transient devices, such as USB flash drives
     * connected to handheld devices.
     * <p>
     * An application may store data on any or all of the returned devices. For
     * example, an app may choose to store large files on the device with the
     * most available space, as measured by {@link StatFs}.
     * <p>
     * No additional permissions are required for the calling app to read or
     * write files under the returned path. Write access outside of these paths
     * on secondary external storage devices is not available.
     *
     * @return the absolute paths to application-specific directories. Some
     * individual paths may be {@code null} if that shared storage is
     * not currently available. The first path returned is the same as
     * {@link #getObbDir()}
     * @see #getObbDir()
     * @see Environment#getExternalStorageState(File)
     * @see Environment#isExternalStorageEmulated(File)
     * @see Environment#isExternalStorageRemovable(File)
     */
    @Override
    public File[] getObbDirs() {
        return new File[0];
    }

    /**
     * Returns the absolute path to the application specific cache directory on
     * the filesystem.
     * <p>
     * The system will automatically delete files in this directory as disk
     * space is needed elsewhere on the device. The system will always delete
     * older files first, as reported by {@link File#lastModified()}. If
     * desired, you can exert more control over how files are deleted using
     * {@link StorageManager#setCacheBehaviorGroup(File, boolean)} and
     * {@link StorageManager#setCacheBehaviorTombstone(File, boolean)}.
     * <p>
     * Apps are strongly encouraged to keep their usage of cache space below the
     * quota returned by
     * {@link StorageManager#getCacheQuotaBytes(UUID)}. If your app
     * goes above this quota, your cached files will be some of the first to be
     * deleted when additional disk space is needed. Conversely, if your app
     * stays under this quota, your cached files will be some of the last to be
     * deleted when additional disk space is needed.
     * <p>
     * Note that your cache quota will change over time depending on how
     * frequently the user interacts with your app, and depending on how much
     * system-wide disk space is used.
     * <p>
     * The returned path may change over time if the calling app is moved to an
     * adopted storage device, so only relative paths should be persisted.
     * <p>
     * Apps require no extra permissions to read or write to the returned path,
     * since this path lives in their private storage.
     *
     * @return The path of the directory holding application cache files.
     * @see #openFileOutput
     * @see #getFileStreamPath
     * @see #getDir
     * @see #getExternalCacheDir
     */
    @Override
    public File getCacheDir() {
        return null;
    }

    /**
     * Returns the absolute path to the application specific cache directory on
     * the filesystem designed for storing cached code.
     * <p>
     * The system will delete any files stored in this location both when your
     * specific application is upgraded, and when the entire platform is
     * upgraded.
     * <p>
     * This location is optimal for storing compiled or optimized code generated
     * by your application at runtime.
     * <p>
     * The returned path may change over time if the calling app is moved to an
     * adopted storage device, so only relative paths should be persisted.
     * <p>
     * Apps require no extra permissions to read or write to the returned path,
     * since this path lives in their private storage.
     *
     * @return The path of the directory holding application code cache files.
     */
    @Override
    public File getCodeCacheDir() {
        return null;
    }

    /**
     * Returns absolute path to application-specific directory on the primary
     * shared/external storage device where the application can place cache
     * files it owns. These files are internal to the application, and not
     * typically visible to the user as media.
     * <p>
     * This is like {@link #getCacheDir()} in that these files will be deleted
     * when the application is uninstalled, however there are some important
     * differences:
     * <ul>
     * <li>The platform does not always monitor the space available in shared
     * storage, and thus may not automatically delete these files. Apps should
     * always manage the maximum space used in this location. Currently the only
     * time files here will be deleted by the platform is when running on
     * {@link Build.VERSION_CODES#JELLY_BEAN_MR1} or later and
     * {@link Environment#isExternalStorageEmulated(File)} returns true.
     * <li>Shared storage may not always be available, since removable media can
     * be ejected by the user. Media state can be checked using
     * {@link Environment#getExternalStorageState(File)}.
     * <li>There is no security enforced with these files. For example, any
     * application holding
     * {@link Manifest.permission#WRITE_EXTERNAL_STORAGE} can write to
     * these files.
     * </ul>
     * <p>
     * If a shared storage device is emulated (as determined by
     * {@link Environment#isExternalStorageEmulated(File)}), its contents are
     * backed by a private user data partition, which means there is little
     * benefit to storing data here instead of the private directory returned by
     * {@link #getCacheDir()}.
     * <p>
     * Starting in {@link Build.VERSION_CODES#KITKAT}, no permissions
     * are required to read or write to the returned path; it's always
     * accessible to the calling app. This only applies to paths generated for
     * package name of the calling application. To access paths belonging to
     * other packages,
     * {@link Manifest.permission#WRITE_EXTERNAL_STORAGE} and/or
     * {@link Manifest.permission#READ_EXTERNAL_STORAGE} are required.
     * <p>
     * On devices with multiple users (as described by {@link UserManager}),
     * each user has their own isolated shared storage. Applications only have
     * access to the shared storage for the user they're running as.
     * <p>
     * The returned path may change over time if different shared storage media
     * is inserted, so only relative paths should be persisted.
     *
     * @return the absolute path to application-specific directory. May return
     * {@code null} if shared storage is not currently available.
     * @see #getCacheDir
     * @see #getExternalCacheDirs()
     * @see Environment#getExternalStorageState(File)
     * @see Environment#isExternalStorageEmulated(File)
     * @see Environment#isExternalStorageRemovable(File)
     */
    @Nullable
    @Override
    public File getExternalCacheDir() {
        return null;
    }

    /**
     * Returns absolute paths to application-specific directories on all
     * shared/external storage devices where the application can place cache
     * files it owns. These files are internal to the application, and not
     * typically visible to the user as media.
     * <p>
     * This is like {@link #getCacheDir()} in that these files will be deleted
     * when the application is uninstalled, however there are some important
     * differences:
     * <ul>
     * <li>The platform does not always monitor the space available in shared
     * storage, and thus may not automatically delete these files. Apps should
     * always manage the maximum space used in this location. Currently the only
     * time files here will be deleted by the platform is when running on
     * {@link Build.VERSION_CODES#JELLY_BEAN_MR1} or later and
     * {@link Environment#isExternalStorageEmulated(File)} returns true.
     * <li>Shared storage may not always be available, since removable media can
     * be ejected by the user. Media state can be checked using
     * {@link Environment#getExternalStorageState(File)}.
     * <li>There is no security enforced with these files. For example, any
     * application holding
     * {@link Manifest.permission#WRITE_EXTERNAL_STORAGE} can write to
     * these files.
     * </ul>
     * <p>
     * If a shared storage device is emulated (as determined by
     * {@link Environment#isExternalStorageEmulated(File)}), it's contents are
     * backed by a private user data partition, which means there is little
     * benefit to storing data here instead of the private directory returned by
     * {@link #getCacheDir()}.
     * <p>
     * Shared storage devices returned here are considered a stable part of the
     * device, including physical media slots under a protective cover. The
     * returned paths do not include transient devices, such as USB flash drives
     * connected to handheld devices.
     * <p>
     * An application may store data on any or all of the returned devices. For
     * example, an app may choose to store large files on the device with the
     * most available space, as measured by {@link StatFs}.
     * <p>
     * No additional permissions are required for the calling app to read or
     * write files under the returned path. Write access outside of these paths
     * on secondary external storage devices is not available.
     * <p>
     * The returned paths may change over time if different shared storage media
     * is inserted, so only relative paths should be persisted.
     *
     * @return the absolute paths to application-specific directories. Some
     * individual paths may be {@code null} if that shared storage is
     * not currently available. The first path returned is the same as
     * {@link #getExternalCacheDir()}.
     * @see #getExternalCacheDir()
     * @see Environment#getExternalStorageState(File)
     * @see Environment#isExternalStorageEmulated(File)
     * @see Environment#isExternalStorageRemovable(File)
     */
    @Override
    public File[] getExternalCacheDirs() {
        return new File[0];
    }

    /**
     * Returns absolute paths to application-specific directories on all
     * shared/external storage devices where the application can place media
     * files. These files are scanned and made available to other apps through
     * {@link MediaStore}.
     * <p>
     * This is like {@link #getExternalFilesDirs} in that these files will be
     * deleted when the application is uninstalled, however there are some
     * important differences:
     * <ul>
     * <li>Shared storage may not always be available, since removable media can
     * be ejected by the user. Media state can be checked using
     * {@link Environment#getExternalStorageState(File)}.
     * <li>There is no security enforced with these files. For example, any
     * application holding
     * {@link Manifest.permission#WRITE_EXTERNAL_STORAGE} can write to
     * these files.
     * </ul>
     * <p>
     * Shared storage devices returned here are considered a stable part of the
     * device, including physical media slots under a protective cover. The
     * returned paths do not include transient devices, such as USB flash drives
     * connected to handheld devices.
     * <p>
     * An application may store data on any or all of the returned devices. For
     * example, an app may choose to store large files on the device with the
     * most available space, as measured by {@link StatFs}.
     * <p>
     * No additional permissions are required for the calling app to read or
     * write files under the returned path. Write access outside of these paths
     * on secondary external storage devices is not available.
     * <p>
     * The returned paths may change over time if different shared storage media
     * is inserted, so only relative paths should be persisted.
     *
     * @return the absolute paths to application-specific directories. Some
     * individual paths may be {@code null} if that shared storage is
     * not currently available.
     * @see Environment#getExternalStorageState(File)
     * @see Environment#isExternalStorageEmulated(File)
     * @see Environment#isExternalStorageRemovable(File)
     */
    @Override
    public File[] getExternalMediaDirs() {
        return new File[0];
    }

    /**
     * Returns an array of strings naming the private files associated with
     * this Context's application package.
     *
     * @return Array of strings naming the private files.
     * @see #openFileInput
     * @see #openFileOutput
     * @see #deleteFile
     */
    @Override
    public String[] fileList() {
        return new String[0];
    }

    /**
     * Retrieve, creating if needed, a new directory in which the application
     * can place its own custom data files.  You can use the returned File
     * object to create and access files in this directory.  Note that files
     * created through a File object will only be accessible by your own
     * application; you can only set the mode of the entire directory, not
     * of individual files.
     * <p>
     * The returned path may change over time if the calling app is moved to an
     * adopted storage device, so only relative paths should be persisted.
     * <p>
     * Apps require no extra permissions to read or write to the returned path,
     * since this path lives in their private storage.
     *
     * @param name Name of the directory to retrieve.  This is a directory
     *             that is created as part of your application data.
     * @param mode Operating mode.
     * @return A {@link File} object for the requested directory.  The directory
     * will have been created if it does not already exist.
     * @see #openFileOutput(String, int)
     */
    @Override
    public File getDir(String name, int mode) {
        return null;
    }

    /**
     * Open a new private SQLiteDatabase associated with this Context's
     * application package. Create the database file if it doesn't exist.
     *
     * @param name    The name (unique in the application package) of the database.
     * @param mode    Operating mode.
     * @param factory An optional factory class that is called to instantiate a
     *                cursor when query is called.
     * @return The contents of a newly created database with the given name.
     * @throws SQLiteException if the database file
     *                         could not be opened.
     * @see #MODE_PRIVATE
     * @see #MODE_ENABLE_WRITE_AHEAD_LOGGING
     * @see #MODE_NO_LOCALIZED_COLLATORS
     * @see #deleteDatabase
     */
    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
        return null;
    }

    /**
     * Open a new private SQLiteDatabase associated with this Context's
     * application package. Creates the database file if it doesn't exist.
     * <p>
     * Accepts input param: a concrete instance of {@link DatabaseErrorHandler}
     * to be used to handle corruption when sqlite reports database corruption.
     * </p>
     *
     * @param name         The name (unique in the application package) of the database.
     * @param mode         Operating mode.
     * @param factory      An optional factory class that is called to instantiate a
     *                     cursor when query is called.
     * @param errorHandler the {@link DatabaseErrorHandler} to be used when
     *                     sqlite reports database corruption. if null,
     *                     {@link DefaultDatabaseErrorHandler} is
     *                     assumed.
     * @return The contents of a newly created database with the given name.
     * @throws SQLiteException if the database file
     *                         could not be opened.
     * @see #MODE_PRIVATE
     * @see #MODE_ENABLE_WRITE_AHEAD_LOGGING
     * @see #MODE_NO_LOCALIZED_COLLATORS
     * @see #deleteDatabase
     */
    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, @Nullable DatabaseErrorHandler errorHandler) {
        return null;
    }

    /**
     * Move an existing database file from the given source storage context to
     * this context. This is typically used to migrate data between storage
     * locations after an upgrade, such as migrating to device protected
     * storage.
     * <p>
     * The database must be closed before being moved.
     *
     * @param sourceContext The source context which contains the existing
     *                      database to move.
     * @param name          The name of the database file.
     * @return {@code true} if the move was successful or if the database didn't
     * exist in the source context, otherwise {@code false}.
     * @see #createDeviceProtectedStorageContext()
     */
    @Override
    public boolean moveDatabaseFrom(Context sourceContext, String name) {
        return false;
    }

    /**
     * Delete an existing private SQLiteDatabase associated with this Context's
     * application package.
     *
     * @param name The name (unique in the application package) of the
     *             database.
     * @return {@code true} if the database was successfully deleted; else {@code false}.
     * @see #openOrCreateDatabase
     */
    @Override
    public boolean deleteDatabase(String name) {
        return false;
    }

    /**
     * Returns the absolute path on the filesystem where a database created with
     * {@link #openOrCreateDatabase} is stored.
     * <p>
     * The returned path may change over time if the calling app is moved to an
     * adopted storage device, so only relative paths should be persisted.
     *
     * @param name The name of the database for which you would like to get
     *             its path.
     * @return An absolute path to the given database.
     * @see #openOrCreateDatabase
     */
    @Override
    public File getDatabasePath(String name) {
        return null;
    }

    /**
     * Returns an array of strings naming the private databases associated with
     * this Context's application package.
     *
     * @return Array of strings naming the private databases.
     * @see #openOrCreateDatabase
     * @see #deleteDatabase
     */
    @Override
    public String[] databaseList() {
        return new String[0];
    }

    /**
     * @deprecated Use {@link WallpaperManager#getDrawable
     * WallpaperManager.get()} instead.
     */
    @Override
    public Drawable getWallpaper() {
        return null;
    }

    /**
     * @deprecated Use {@link WallpaperManager#peekDrawable
     * WallpaperManager.peek()} instead.
     */
    @Override
    public Drawable peekWallpaper() {
        return null;
    }

    /**
     * @deprecated Use {@link WallpaperManager#getDesiredMinimumWidth()
     * WallpaperManager.getDesiredMinimumWidth()} instead.
     */
    @Override
    public int getWallpaperDesiredMinimumWidth() {
        return 0;
    }

    /**
     * @deprecated Use {@link WallpaperManager#getDesiredMinimumHeight()
     * WallpaperManager.getDesiredMinimumHeight()} instead.
     */
    @Override
    public int getWallpaperDesiredMinimumHeight() {
        return 0;
    }

    /**
     * @param bitmap
     * @deprecated Use {@link WallpaperManager#setBitmap(Bitmap)
     * WallpaperManager.set()} instead.
     * <p>This method requires the caller to hold the permission
     * {@link Manifest.permission#SET_WALLPAPER}.
     */
    @Override
    public void setWallpaper(Bitmap bitmap) throws IOException {

    }

    /**
     * @param data
     * @deprecated Use {@link WallpaperManager#setStream(InputStream)
     * WallpaperManager.set()} instead.
     * <p>This method requires the caller to hold the permission
     * {@link Manifest.permission#SET_WALLPAPER}.
     */
    @Override
    public void setWallpaper(InputStream data) throws IOException {

    }

    /**
     * @deprecated Use {@link WallpaperManager#clear
     * WallpaperManager.clear()} instead.
     * <p>This method requires the caller to hold the permission
     * {@link Manifest.permission#SET_WALLPAPER}.
     */
    @Override
    public void clearWallpaper() throws IOException {

    }

    /**
     * Same as {@link #startActivity(Intent, Bundle)} with no options
     * specified.
     *
     * @param intent The description of the activity to start.
     * @throws ActivityNotFoundException &nbsp;
     *                                   `
     * @see #startActivity(Intent, Bundle)
     * @see PackageManager#resolveActivity
     */
    @Override
    public void startActivity(Intent intent) {

    }

    /**
     * Launch a new activity.  You will not receive any information about when
     * the activity exits.
     * <p>
     * <p>Note that if this method is being called from outside of an
     * {@link Activity} Context, then the Intent must include
     * the {@link Intent#FLAG_ACTIVITY_NEW_TASK} launch flag.  This is because,
     * without being started from an existing Activity, there is no existing
     * task in which to place the new activity and thus it needs to be placed
     * in its own separate task.
     * <p>
     * <p>This method throws {@link ActivityNotFoundException}
     * if there was no Activity found to run the given Intent.
     *
     * @param intent  The description of the activity to start.
     * @param options Additional options for how the Activity should be started.
     *                May be null if there are no options.  See {@link ActivityOptions}
     *                for how to build the Bundle supplied here; there are no supported definitions
     *                for building it manually.
     * @throws ActivityNotFoundException &nbsp;
     * @see #startActivity(Intent)
     * @see PackageManager#resolveActivity
     */
    @Override
    public void startActivity(Intent intent, @Nullable Bundle options) {

    }

    /**
     * Same as {@link #startActivities(Intent[], Bundle)} with no options
     * specified.
     *
     * @param intents An array of Intents to be started.
     * @throws ActivityNotFoundException &nbsp;
     * @see #startActivities(Intent[], Bundle)
     * @see PackageManager#resolveActivity
     */
    @Override
    public void startActivities(Intent[] intents) {

    }

    /**
     * Launch multiple new activities.  This is generally the same as calling
     * {@link #startActivity(Intent)} for the first Intent in the array,
     * that activity during its creation calling {@link #startActivity(Intent)}
     * for the second entry, etc.  Note that unlike that approach, generally
     * none of the activities except the last in the array will be created
     * at this point, but rather will be created when the user first visits
     * them (due to pressing back from the activity on top).
     * <p>
     * <p>This method throws {@link ActivityNotFoundException}
     * if there was no Activity found for <em>any</em> given Intent.  In this
     * case the state of the activity stack is undefined (some Intents in the
     * list may be on it, some not), so you probably want to avoid such situations.
     *
     * @param intents An array of Intents to be started.
     * @param options Additional options for how the Activity should be started.
     *                See {@link Context#startActivity(Intent, Bundle)}
     *                Context.startActivity(Intent, Bundle)} for more details.
     * @throws ActivityNotFoundException &nbsp;
     * @see #startActivities(Intent[])
     * @see PackageManager#resolveActivity
     */
    @Override
    public void startActivities(Intent[] intents, Bundle options) {

    }

    /**
     * Same as {@link #startIntentSender(IntentSender, Intent, int, int, int, Bundle)}
     * with no options specified.
     *
     * @param intent       The IntentSender to launch.
     * @param fillInIntent If non-null, this will be provided as the
     *                     intent parameter to {@link IntentSender#sendIntent}.
     * @param flagsMask    Intent flags in the original IntentSender that you
     *                     would like to change.
     * @param flagsValues  Desired values for any bits set in
     *                     <var>flagsMask</var>
     * @param extraFlags   Always set to 0.
     * @see #startActivity(Intent)
     * @see #startIntentSender(IntentSender, Intent, int, int, int, Bundle)
     */
    @Override
    public void startIntentSender(IntentSender intent, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws IntentSender.SendIntentException {

    }

    /**
     * Like {@link #startActivity(Intent, Bundle)}, but taking a IntentSender
     * to start.  If the IntentSender is for an activity, that activity will be started
     * as if you had called the regular {@link #startActivity(Intent)}
     * here; otherwise, its associated action will be executed (such as
     * sending a broadcast) as if you had called
     * {@link IntentSender#sendIntent IntentSender.sendIntent} on it.
     *
     * @param intent       The IntentSender to launch.
     * @param fillInIntent If non-null, this will be provided as the
     *                     intent parameter to {@link IntentSender#sendIntent}.
     * @param flagsMask    Intent flags in the original IntentSender that you
     *                     would like to change.
     * @param flagsValues  Desired values for any bits set in
     *                     <var>flagsMask</var>
     * @param extraFlags   Always set to 0.
     * @param options      Additional options for how the Activity should be started.
     *                     See {@link Context#startActivity(Intent, Bundle)}
     *                     Context.startActivity(Intent, Bundle)} for more details.  If options
     *                     have also been supplied by the IntentSender, options given here will
     *                     override any that conflict with those given by the IntentSender.
     * @see #startActivity(Intent, Bundle)
     * @see #startIntentSender(IntentSender, Intent, int, int, int)
     */
    @Override
    public void startIntentSender(IntentSender intent, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, @Nullable Bundle options) throws IntentSender.SendIntentException {

    }

    /**
     * Broadcast the given intent to all interested BroadcastReceivers.  This
     * call is asynchronous; it returns immediately, and you will continue
     * executing while the receivers are run.  No results are propagated from
     * receivers and receivers can not abort the broadcast. If you want
     * to allow receivers to propagate results or abort the broadcast, you must
     * send an ordered broadcast using
     * {@link #sendOrderedBroadcast(Intent, String)}.
     * <p>
     * <p>See {@link BroadcastReceiver} for more information on Intent broadcasts.
     *
     * @param intent The Intent to broadcast; all receivers matching this
     *               Intent will receive the broadcast.
     * @see BroadcastReceiver
     * @see #registerReceiver
     * @see #sendBroadcast(Intent, String)
     * @see #sendOrderedBroadcast(Intent, String)
     * @see #sendOrderedBroadcast(Intent, String, BroadcastReceiver, Handler, int, String, Bundle)
     */
    @Override
    public void sendBroadcast(Intent intent) {

    }

    /**
     * Broadcast the given intent to all interested BroadcastReceivers, allowing
     * an optional required permission to be enforced.  This
     * call is asynchronous; it returns immediately, and you will continue
     * executing while the receivers are run.  No results are propagated from
     * receivers and receivers can not abort the broadcast. If you want
     * to allow receivers to propagate results or abort the broadcast, you must
     * send an ordered broadcast using
     * {@link #sendOrderedBroadcast(Intent, String)}.
     * <p>
     * <p>See {@link BroadcastReceiver} for more information on Intent broadcasts.
     *
     * @param intent             The Intent to broadcast; all receivers matching this
     *                           Intent will receive the broadcast.
     * @param receiverPermission (optional) String naming a permission that
     *                           a receiver must hold in order to receive your broadcast.
     *                           If null, no permission is required.
     * @see BroadcastReceiver
     * @see #registerReceiver
     * @see #sendBroadcast(Intent)
     * @see #sendOrderedBroadcast(Intent, String)
     * @see #sendOrderedBroadcast(Intent, String, BroadcastReceiver, Handler, int, String, Bundle)
     */
    @Override
    public void sendBroadcast(Intent intent, @Nullable String receiverPermission) {

    }

    /**
     * Broadcast the given intent to all interested BroadcastReceivers, delivering
     * them one at a time to allow more preferred receivers to consume the
     * broadcast before it is delivered to less preferred receivers.  This
     * call is asynchronous; it returns immediately, and you will continue
     * executing while the receivers are run.
     * <p>
     * <p>See {@link BroadcastReceiver} for more information on Intent broadcasts.
     *
     * @param intent             The Intent to broadcast; all receivers matching this
     *                           Intent will receive the broadcast.
     * @param receiverPermission (optional) String naming a permissions that
     *                           a receiver must hold in order to receive your broadcast.
     *                           If null, no permission is required.
     * @see BroadcastReceiver
     * @see #registerReceiver
     * @see #sendBroadcast(Intent)
     * @see #sendOrderedBroadcast(Intent, String, BroadcastReceiver, Handler, int, String, Bundle)
     */
    @Override
    public void sendOrderedBroadcast(Intent intent, @Nullable String receiverPermission) {

    }

    /**
     * Version of {@link #sendBroadcast(Intent)} that allows you to
     * receive data back from the broadcast.  This is accomplished by
     * supplying your own BroadcastReceiver when calling, which will be
     * treated as a final receiver at the end of the broadcast -- its
     * {@link BroadcastReceiver#onReceive} method will be called with
     * the result values collected from the other receivers.  The broadcast will
     * be serialized in the same way as calling
     * {@link #sendOrderedBroadcast(Intent, String)}.
     * <p>
     * <p>Like {@link #sendBroadcast(Intent)}, this method is
     * asynchronous; it will return before
     * resultReceiver.onReceive() is called.
     * <p>
     * <p>See {@link BroadcastReceiver} for more information on Intent broadcasts.
     *
     * @param intent             The Intent to broadcast; all receivers matching this
     *                           Intent will receive the broadcast.
     * @param receiverPermission String naming a permissions that
     *                           a receiver must hold in order to receive your broadcast.
     *                           If null, no permission is required.
     * @param resultReceiver     Your own BroadcastReceiver to treat as the final
     *                           receiver of the broadcast.
     * @param scheduler          A custom Handler with which to schedule the
     *                           resultReceiver callback; if null it will be
     *                           scheduled in the Context's main thread.
     * @param initialCode        An initial value for the result code.  Often
     *                           Activity.RESULT_OK.
     * @param initialData        An initial value for the result data.  Often
     *                           null.
     * @param initialExtras      An initial value for the result extras.  Often
     *                           null.
     * @see #sendBroadcast(Intent)
     * @see #sendBroadcast(Intent, String)
     * @see #sendOrderedBroadcast(Intent, String)
     * @see BroadcastReceiver
     * @see #registerReceiver
     * @see Activity#RESULT_OK
     */
    @Override
    public void sendOrderedBroadcast(@NonNull Intent intent, @Nullable String receiverPermission, @Nullable BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

    }

    /**
     * Version of {@link #sendBroadcast(Intent)} that allows you to specify the
     * user the broadcast will be sent to.  This is not available to applications
     * that are not pre-installed on the system image.
     *
     * @param intent The intent to broadcast
     * @param user   UserHandle to send the intent to.
     * @see #sendBroadcast(Intent)
     */
    @Override
    public void sendBroadcastAsUser(Intent intent, UserHandle user) {

    }

    /**
     * Version of {@link #sendBroadcast(Intent, String)} that allows you to specify the
     * user the broadcast will be sent to.  This is not available to applications
     * that are not pre-installed on the system image.
     *
     * @param intent             The Intent to broadcast; all receivers matching this
     *                           Intent will receive the broadcast.
     * @param user               UserHandle to send the intent to.
     * @param receiverPermission (optional) String naming a permission that
     *                           a receiver must hold in order to receive your broadcast.
     *                           If null, no permission is required.
     * @see #sendBroadcast(Intent, String)
     */
    @Override
    public void sendBroadcastAsUser(Intent intent, UserHandle user, @Nullable String receiverPermission) {

    }

    /**
     * Version of
     * {@link #sendOrderedBroadcast(Intent, String, BroadcastReceiver, Handler, int, String, Bundle)}
     * that allows you to specify the
     * user the broadcast will be sent to.  This is not available to applications
     * that are not pre-installed on the system image.
     * <p>
     * <p>See {@link BroadcastReceiver} for more information on Intent broadcasts.
     *
     * @param intent             The Intent to broadcast; all receivers matching this
     *                           Intent will receive the broadcast.
     * @param user               UserHandle to send the intent to.
     * @param receiverPermission String naming a permissions that
     *                           a receiver must hold in order to receive your broadcast.
     *                           If null, no permission is required.
     * @param resultReceiver     Your own BroadcastReceiver to treat as the final
     *                           receiver of the broadcast.
     * @param scheduler          A custom Handler with which to schedule the
     *                           resultReceiver callback; if null it will be
     *                           scheduled in the Context's main thread.
     * @param initialCode        An initial value for the result code.  Often
     *                           Activity.RESULT_OK.
     * @param initialData        An initial value for the result data.  Often
     *                           null.
     * @param initialExtras      An initial value for the result extras.  Often
     *                           null.
     * @see #sendOrderedBroadcast(Intent, String, BroadcastReceiver, Handler, int, String, Bundle)
     */
    @Override
    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, @Nullable String receiverPermission, BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

    }

    /**
     * <p>Perform a {@link #sendBroadcast(Intent)} that is "sticky," meaning the
     * Intent you are sending stays around after the broadcast is complete,
     * so that others can quickly retrieve that data through the return
     * value of {@link #registerReceiver(BroadcastReceiver, IntentFilter)}.  In
     * all other ways, this behaves the same as
     * {@link #sendBroadcast(Intent)}.
     *
     * @param intent The Intent to broadcast; all receivers matching this
     *               Intent will receive the broadcast, and the Intent will be held to
     *               be re-broadcast to future receivers.
     * @see #sendBroadcast(Intent)
     * @see #sendStickyOrderedBroadcast(Intent, BroadcastReceiver, Handler, int, String, Bundle)
     * @deprecated Sticky broadcasts should not be used.  They provide no security (anyone
     * can access them), no protection (anyone can modify them), and many other problems.
     * The recommended pattern is to use a non-sticky broadcast to report that <em>something</em>
     * has changed, with another mechanism for apps to retrieve the current value whenever
     * desired.
     */
    @Override
    public void sendStickyBroadcast(Intent intent) {

    }

    /**
     * <p>Version of {@link #sendStickyBroadcast} that allows you to
     * receive data back from the broadcast.  This is accomplished by
     * supplying your own BroadcastReceiver when calling, which will be
     * treated as a final receiver at the end of the broadcast -- its
     * {@link BroadcastReceiver#onReceive} method will be called with
     * the result values collected from the other receivers.  The broadcast will
     * be serialized in the same way as calling
     * {@link #sendOrderedBroadcast(Intent, String)}.
     * <p>
     * <p>Like {@link #sendBroadcast(Intent)}, this method is
     * asynchronous; it will return before
     * resultReceiver.onReceive() is called.  Note that the sticky data
     * stored is only the data you initially supply to the broadcast, not
     * the result of any changes made by the receivers.
     * <p>
     * <p>See {@link BroadcastReceiver} for more information on Intent broadcasts.
     *
     * @param intent         The Intent to broadcast; all receivers matching this
     *                       Intent will receive the broadcast.
     * @param resultReceiver Your own BroadcastReceiver to treat as the final
     *                       receiver of the broadcast.
     * @param scheduler      A custom Handler with which to schedule the
     *                       resultReceiver callback; if null it will be
     *                       scheduled in the Context's main thread.
     * @param initialCode    An initial value for the result code.  Often
     *                       Activity.RESULT_OK.
     * @param initialData    An initial value for the result data.  Often
     *                       null.
     * @param initialExtras  An initial value for the result extras.  Often
     *                       null.
     * @see #sendBroadcast(Intent)
     * @see #sendBroadcast(Intent, String)
     * @see #sendOrderedBroadcast(Intent, String)
     * @see #sendStickyBroadcast(Intent)
     * @see BroadcastReceiver
     * @see #registerReceiver
     * @see Activity#RESULT_OK
     * @deprecated Sticky broadcasts should not be used.  They provide no security (anyone
     * can access them), no protection (anyone can modify them), and many other problems.
     * The recommended pattern is to use a non-sticky broadcast to report that <em>something</em>
     * has changed, with another mechanism for apps to retrieve the current value whenever
     * desired.
     */
    @Override
    public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

    }

    /**
     * <p>Remove the data previously sent with {@link #sendStickyBroadcast},
     * so that it is as if the sticky broadcast had never happened.
     *
     * @param intent The Intent that was previously broadcast.
     * @see #sendStickyBroadcast
     * @deprecated Sticky broadcasts should not be used.  They provide no security (anyone
     * can access them), no protection (anyone can modify them), and many other problems.
     * The recommended pattern is to use a non-sticky broadcast to report that <em>something</em>
     * has changed, with another mechanism for apps to retrieve the current value whenever
     * desired.
     */
    @Override
    public void removeStickyBroadcast(Intent intent) {

    }

    /**
     * <p>Version of {@link #sendStickyBroadcast(Intent)} that allows you to specify the
     * user the broadcast will be sent to.  This is not available to applications
     * that are not pre-installed on the system image.
     *
     * @param intent The Intent to broadcast; all receivers matching this
     *               Intent will receive the broadcast, and the Intent will be held to
     *               be re-broadcast to future receivers.
     * @param user   UserHandle to send the intent to.
     * @see #sendBroadcast(Intent)
     * @deprecated Sticky broadcasts should not be used.  They provide no security (anyone
     * can access them), no protection (anyone can modify them), and many other problems.
     * The recommended pattern is to use a non-sticky broadcast to report that <em>something</em>
     * has changed, with another mechanism for apps to retrieve the current value whenever
     * desired.
     */
    @Override
    public void sendStickyBroadcastAsUser(Intent intent, UserHandle user) {

    }

    /**
     * <p>Version of
     * {@link #sendStickyOrderedBroadcast(Intent, BroadcastReceiver, Handler, int, String, Bundle)}
     * that allows you to specify the
     * user the broadcast will be sent to.  This is not available to applications
     * that are not pre-installed on the system image.
     * <p>
     * <p>See {@link BroadcastReceiver} for more information on Intent broadcasts.
     *
     * @param intent         The Intent to broadcast; all receivers matching this
     *                       Intent will receive the broadcast.
     * @param user           UserHandle to send the intent to.
     * @param resultReceiver Your own BroadcastReceiver to treat as the final
     *                       receiver of the broadcast.
     * @param scheduler      A custom Handler with which to schedule the
     *                       resultReceiver callback; if null it will be
     *                       scheduled in the Context's main thread.
     * @param initialCode    An initial value for the result code.  Often
     *                       Activity.RESULT_OK.
     * @param initialData    An initial value for the result data.  Often
     *                       null.
     * @param initialExtras  An initial value for the result extras.  Often
     *                       null.
     * @see #sendStickyOrderedBroadcast(Intent, BroadcastReceiver, Handler, int, String, Bundle)
     * @deprecated Sticky broadcasts should not be used.  They provide no security (anyone
     * can access them), no protection (anyone can modify them), and many other problems.
     * The recommended pattern is to use a non-sticky broadcast to report that <em>something</em>
     * has changed, with another mechanism for apps to retrieve the current value whenever
     * desired.
     */
    @Override
    public void sendStickyOrderedBroadcastAsUser(Intent intent, UserHandle user, BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

    }

    /**
     * <p>Version of {@link #removeStickyBroadcast(Intent)} that allows you to specify the
     * user the broadcast will be sent to.  This is not available to applications
     * that are not pre-installed on the system image.
     * <p>
     * <p>You must hold the {@link Manifest.permission#BROADCAST_STICKY}
     * permission in order to use this API.  If you do not hold that
     * permission, {@link SecurityException} will be thrown.
     *
     * @param intent The Intent that was previously broadcast.
     * @param user   UserHandle to remove the sticky broadcast from.
     * @see #sendStickyBroadcastAsUser
     * @deprecated Sticky broadcasts should not be used.  They provide no security (anyone
     * can access them), no protection (anyone can modify them), and many other problems.
     * The recommended pattern is to use a non-sticky broadcast to report that <em>something</em>
     * has changed, with another mechanism for apps to retrieve the current value whenever
     * desired.
     */
    @Override
    public void removeStickyBroadcastAsUser(Intent intent, UserHandle user) {

    }

    /**
     * Register a BroadcastReceiver to be run in the main activity thread.  The
     * <var>receiver</var> will be called with any broadcast Intent that
     * matches <var>filter</var>, in the main application thread.
     * <p>
     * <p>The system may broadcast Intents that are "sticky" -- these stay
     * around after the broadcast has finished, to be sent to any later
     * registrations. If your IntentFilter matches one of these sticky
     * Intents, that Intent will be returned by this function
     * <strong>and</strong> sent to your <var>receiver</var> as if it had just
     * been broadcast.
     * <p>
     * <p>There may be multiple sticky Intents that match <var>filter</var>,
     * in which case each of these will be sent to <var>receiver</var>.  In
     * this case, only one of these can be returned directly by the function;
     * which of these that is returned is arbitrarily decided by the system.
     * <p>
     * <p>If you know the Intent your are registering for is sticky, you can
     * supply null for your <var>receiver</var>.  In this case, no receiver is
     * registered -- the function simply returns the sticky Intent that
     * matches <var>filter</var>.  In the case of multiple matches, the same
     * rules as described above apply.
     * <p>
     * <p>See {@link BroadcastReceiver} for more information on Intent broadcasts.
     * <p>
     * <p>As of {@link Build.VERSION_CODES#ICE_CREAM_SANDWICH}, receivers
     * registered with this method will correctly respect the
     * {@link Intent#setPackage(String)} specified for an Intent being broadcast.
     * Prior to that, it would be ignored and delivered to all matching registered
     * receivers.  Be careful if using this for security.</p>
     * <p>
     * <p class="note">Note: this method <em>cannot be called from a
     * {@link BroadcastReceiver} component;</em> that is, from a BroadcastReceiver
     * that is declared in an application's manifest.  It is okay, however, to call
     * this method from another BroadcastReceiver that has itself been registered
     * at run time with {@link #registerReceiver}, since the lifetime of such a
     * registered BroadcastReceiver is tied to the object that registered it.</p>
     *
     * @param receiver The BroadcastReceiver to handle the broadcast.
     * @param filter   Selects the Intent broadcasts to be received.
     * @return The first sticky intent found that matches <var>filter</var>,
     * or null if there are none.
     * @see #registerReceiver(BroadcastReceiver, IntentFilter, String, Handler)
     * @see #sendBroadcast
     * @see #unregisterReceiver
     */
    @Nullable
    @Override
    public Intent registerReceiver(@Nullable BroadcastReceiver receiver, IntentFilter filter) {
        return null;
    }

    /**
     * Register to receive intent broadcasts, with the receiver optionally being
     * exposed to Instant Apps. See
     * {@link #registerReceiver(BroadcastReceiver, IntentFilter)} for more
     * information. By default Instant Apps cannot interact with receivers in other
     * applications, this allows you to expose a receiver that Instant Apps can
     * interact with.
     * <p>
     * <p>See {@link BroadcastReceiver} for more information on Intent broadcasts.
     * <p>
     * <p>As of {@link Build.VERSION_CODES#ICE_CREAM_SANDWICH}, receivers
     * registered with this method will correctly respect the
     * {@link Intent#setPackage(String)} specified for an Intent being broadcast.
     * Prior to that, it would be ignored and delivered to all matching registered
     * receivers.  Be careful if using this for security.</p>
     *
     * @param receiver The BroadcastReceiver to handle the broadcast.
     * @param filter   Selects the Intent broadcasts to be received.
     * @param flags    Additional options for the receiver. May be 0 or
     *                 {@link #RECEIVER_VISIBLE_TO_INSTANT_APPS}.
     * @return The first sticky intent found that matches <var>filter</var>,
     * or null if there are none.
     * @see #registerReceiver(BroadcastReceiver, IntentFilter)
     * @see #sendBroadcast
     * @see #unregisterReceiver
     */
    @Nullable
    @Override
    public Intent registerReceiver(@Nullable BroadcastReceiver receiver, IntentFilter filter, int flags) {
        return null;
    }

    /**
     * Register to receive intent broadcasts, to run in the context of
     * <var>scheduler</var>.  See
     * {@link #registerReceiver(BroadcastReceiver, IntentFilter)} for more
     * information.  This allows you to enforce permissions on who can
     * broadcast intents to your receiver, or have the receiver run in
     * a different thread than the main application thread.
     * <p>
     * <p>See {@link BroadcastReceiver} for more information on Intent broadcasts.
     * <p>
     * <p>As of {@link Build.VERSION_CODES#ICE_CREAM_SANDWICH}, receivers
     * registered with this method will correctly respect the
     * {@link Intent#setPackage(String)} specified for an Intent being broadcast.
     * Prior to that, it would be ignored and delivered to all matching registered
     * receivers.  Be careful if using this for security.</p>
     *
     * @param receiver            The BroadcastReceiver to handle the broadcast.
     * @param filter              Selects the Intent broadcasts to be received.
     * @param broadcastPermission String naming a permissions that a
     *                            broadcaster must hold in order to send an Intent to you.  If null,
     *                            no permission is required.
     * @param scheduler           Handler identifying the thread that will receive
     *                            the Intent.  If null, the main thread of the process will be used.
     * @return The first sticky intent found that matches <var>filter</var>,
     * or null if there are none.
     * @see #registerReceiver(BroadcastReceiver, IntentFilter)
     * @see #sendBroadcast
     * @see #unregisterReceiver
     */
    @Nullable
    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, @Nullable String broadcastPermission, @Nullable Handler scheduler) {
        return null;
    }

    /**
     * Register to receive intent broadcasts, to run in the context of
     * <var>scheduler</var>. See
     * {@link #registerReceiver(BroadcastReceiver, IntentFilter, int)} and
     * {@link #registerReceiver(BroadcastReceiver, IntentFilter, String, Handler)}
     * for more information.
     * <p>
     * <p>See {@link BroadcastReceiver} for more information on Intent broadcasts.
     * <p>
     * <p>As of {@link Build.VERSION_CODES#ICE_CREAM_SANDWICH}, receivers
     * registered with this method will correctly respect the
     * {@link Intent#setPackage(String)} specified for an Intent being broadcast.
     * Prior to that, it would be ignored and delivered to all matching registered
     * receivers.  Be careful if using this for security.</p>
     *
     * @param receiver            The BroadcastReceiver to handle the broadcast.
     * @param filter              Selects the Intent broadcasts to be received.
     * @param broadcastPermission String naming a permissions that a
     *                            broadcaster must hold in order to send an Intent to you.  If null,
     *                            no permission is required.
     * @param scheduler           Handler identifying the thread that will receive
     *                            the Intent.  If null, the main thread of the process will be used.
     * @param flags               Additional options for the receiver. May be 0 or
     *                            {@link #RECEIVER_VISIBLE_TO_INSTANT_APPS}.
     * @return The first sticky intent found that matches <var>filter</var>,
     * or null if there are none.
     * @see #registerReceiver(BroadcastReceiver, IntentFilter, int)
     * @see #registerReceiver(BroadcastReceiver, IntentFilter, String, Handler)
     * @see #sendBroadcast
     * @see #unregisterReceiver
     */
    @Nullable
    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, @Nullable String broadcastPermission, @Nullable Handler scheduler, int flags) {
        return null;
    }

    /**
     * Unregister a previously registered BroadcastReceiver.  <em>All</em>
     * filters that have been registered for this BroadcastReceiver will be
     * removed.
     *
     * @param receiver The BroadcastReceiver to unregister.
     * @see #registerReceiver
     */
    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {

    }

    /**
     * Request that a given application service be started.  The Intent
     * should either contain the complete class name of a specific service
     * implementation to start, or a specific package name to target.  If the
     * Intent is less specified, it logs a warning about this.  In this case any of the
     * multiple matching services may be used.  If this service
     * is not already running, it will be instantiated and started (creating a
     * process for it if needed); if it is running then it remains running.
     * <p>
     * <p>Every call to this method will result in a corresponding call to
     * the target service's {@link Service#onStartCommand} method,
     * with the <var>intent</var> given here.  This provides a convenient way
     * to submit jobs to a service without having to bind and call on to its
     * interface.
     * <p>
     * <p>Using startService() overrides the default service lifetime that is
     * managed by {@link #bindService}: it requires the service to remain
     * running until {@link #stopService} is called, regardless of whether
     * any clients are connected to it.  Note that calls to startService()
     * do not nest: no matter how many times you call startService(),
     * a single call to {@link #stopService} will stop it.
     * <p>
     * <p>The system attempts to keep running services around as much as
     * possible.  The only time they should be stopped is if the current
     * foreground application is using so many resources that the service needs
     * to be killed.  If any errors happen in the service's process, it will
     * automatically be restarted.
     * <p>
     * <p>This function will throw {@link SecurityException} if you do not
     * have permission to start the given service.
     * <p>
     * <p class="note"><strong>Note:</strong> Each call to startService()
     * results in significant work done by the system to manage service
     * lifecycle surrounding the processing of the intent, which can take
     * multiple milliseconds of CPU time. Due to this cost, startService()
     * should not be used for frequent intent delivery to a service, and only
     * for scheduling significant work. Use {@link #bindService bound services}
     * for high frequency calls.
     * </p>
     *
     * @param service Identifies the service to be started.  The Intent must be
     *                fully explicit (supplying a component name).  Additional values
     *                may be included in the Intent extras to supply arguments along with
     *                this specific start call.
     * @return If the service is being started or is already running, the
     * {@link ComponentName} of the actual service that was started is
     * returned; else if the service does not exist null is returned.
     * @throws SecurityException     If the caller does not have permission to access the service
     *                               or the service can not be found.
     * @throws IllegalStateException If the application is in a state where the service
     *                               can not be started (such as not in the foreground in a state when services are allowed).
     * @see #stopService
     * @see #bindService
     */
    @Nullable
    @Override
    public ComponentName startService(Intent service) {
        return null;
    }

    /**
     * Similar to {@link #startService(Intent)}, but with an implicit promise that the
     * Service will call {@link Service#startForeground(int, Notification)
     * startForeground(int, android.app.Notification)} once it begins running.  The service is given
     * an amount of time comparable to the ANR interval to do this, otherwise the system
     * will automatically stop the service and declare the app ANR.
     * <p>
     * <p>Unlike the ordinary {@link #startService(Intent)}, this method can be used
     * at any time, regardless of whether the app hosting the service is in a foreground
     * state.
     *
     * @param service Identifies the service to be started.  The Intent must be
     *                fully explicit (supplying a component name).  Additional values
     *                may be included in the Intent extras to supply arguments along with
     *                this specific start call.
     * @return If the service is being started or is already running, the
     * {@link ComponentName} of the actual service that was started is
     * returned; else if the service does not exist null is returned.
     * @throws SecurityException If the caller does not have permission to access the service
     *                           or the service can not be found.
     * @see #stopService
     * @see Service#startForeground(int, Notification)
     */
    @Nullable
    @Override
    public ComponentName startForegroundService(Intent service) {
        return null;
    }

    /**
     * Request that a given application service be stopped.  If the service is
     * not running, nothing happens.  Otherwise it is stopped.  Note that calls
     * to startService() are not counted -- this stops the service no matter
     * how many times it was started.
     * <p>
     * <p>Note that if a stopped service still has {@link ServiceConnection}
     * objects bound to it with the {@link #BIND_AUTO_CREATE} set, it will
     * not be destroyed until all of these bindings are removed.  See
     * the {@link Service} documentation for more details on a
     * service's lifecycle.
     * <p>
     * <p>This function will throw {@link SecurityException} if you do not
     * have permission to stop the given service.
     *
     * @param service Description of the service to be stopped.  The Intent must be either
     *                fully explicit (supplying a component name) or specify a specific package
     *                name it is targetted to.
     * @return If there is a service matching the given Intent that is already
     * running, then it is stopped and {@code true} is returned; else {@code false} is returned.
     * @throws SecurityException     If the caller does not have permission to access the service
     *                               or the service can not be found.
     * @throws IllegalStateException If the application is in a state where the service
     *                               can not be started (such as not in the foreground in a state when services are allowed).
     * @see #startService
     */
    @Override
    public boolean stopService(Intent service) {
        return false;
    }

    /**
     * Connect to an application service, creating it if needed.  This defines
     * a dependency between your application and the service.  The given
     * <var>conn</var> will receive the service object when it is created and be
     * told if it dies and restarts.  The service will be considered required
     * by the system only for as long as the calling context exists.  For
     * example, if this Context is an Activity that is stopped, the service will
     * not be required to continue running until the Activity is resumed.
     * <p>
     * <p>If the service does not support binding, it may return {@code null} from
     * its {@link Service#onBind(Intent) onBind()} method.  If it does, then
     * the ServiceConnection's
     * {@link ServiceConnection#onNullBinding(ComponentName) onNullBinding()} method
     * will be invoked instead of
     * .
     * <p>
     * <p>This method will throw {@link SecurityException} if the calling app does not
     * have permission to bind to the given service.
     * <p>
     * <p class="note">Note: this method <em>cannot be called from a
     * {@link BroadcastReceiver} component</em>.  A pattern you can use to
     * communicate from a BroadcastReceiver to a Service is to call
     * {@link #startService} with the arguments containing the command to be
     * sent, with the service calling its
     * {@link Service#stopSelf(int)} method when done executing
     * that command.  See the API demo App/Service/Service Start Arguments
     * Controller for an illustration of this.  It is okay, however, to use
     * this method from a BroadcastReceiver that has been registered with
     * {@link #registerReceiver}, since the lifetime of this BroadcastReceiver
     * is tied to another object (the one that registered it).</p>
     *
     * @param service Identifies the service to connect to.  The Intent must
     *                specify an explicit component name.
     * @param conn    Receives information as the service is started and stopped.
     *                This must be a valid ServiceConnection object; it must not be null.
     * @param flags   Operation options for the binding.  May be 0,
     *                {@link #BIND_AUTO_CREATE}, {@link #BIND_DEBUG_UNBIND},
     *                {@link #BIND_NOT_FOREGROUND}, {@link #BIND_ABOVE_CLIENT},
     *                {@link #BIND_ALLOW_OOM_MANAGEMENT}, or
     *                {@link #BIND_WAIVE_PRIORITY}.
     * @return {@code true} if the system is in the process of bringing up a
     * service that your client has permission to bind to; {@code false}
     * if the system couldn't find the service or if your client doesn't
     * have permission to bind to it. If this value is {@code true}, you
     * should later call {@link #unbindService} to release the
     * connection.
     * @throws SecurityException If the caller does not have permission to access the service
     *                           or the service can not be found.
     * @see #unbindService
     * @see #startService
     * @see #BIND_AUTO_CREATE
     * @see #BIND_DEBUG_UNBIND
     * @see #BIND_NOT_FOREGROUND
     */
    @Override
    public boolean bindService(Intent service, @NonNull ServiceConnection conn, int flags) {
        return false;
    }

    /**
     * Disconnect from an application service.  You will no longer receive
     * calls as the service is restarted, and the service is now allowed to
     * stop at any time.
     *
     * @param conn The connection interface previously supplied to
     *             bindService().  This parameter must not be null.
     * @see #bindService
     */
    @Override
    public void unbindService(@NonNull ServiceConnection conn) {

    }

    /**
     * Start executing an {@link Instrumentation} class.  The given
     * Instrumentation component will be run by killing its target application
     * (if currently running), starting the target process, instantiating the
     * instrumentation component, and then letting it drive the application.
     * <p>
     * <p>This function is not synchronous -- it returns as soon as the
     * instrumentation has started and while it is running.
     * <p>
     * <p>Instrumentation is normally only allowed to run against a package
     * that is either unsigned or signed with a signature that the
     * the instrumentation package is also signed with (ensuring the target
     * trusts the instrumentation).
     *
     * @param className   Name of the Instrumentation component to be run.
     * @param profileFile Optional path to write profiling data as the
     *                    instrumentation runs, or null for no profiling.
     * @param arguments   Additional optional arguments to pass to the
     *                    instrumentation, or null.
     * @return {@code true} if the instrumentation was successfully started,
     * else {@code false} if it could not be found.
     */
    @Override
    public boolean startInstrumentation(@NonNull ComponentName className, @Nullable String profileFile, @Nullable Bundle arguments) {
        return false;
    }

    /**
     * Return the handle to a system-level service by name. The class of the
     * returned object varies by the requested name. Currently available names
     * are:
     * <p>
     * <dl>
     * <dt> {@link #WINDOW_SERVICE} ("window")
     * <dd> The top-level window manager in which you can place custom
     * windows.  The returned object is a {@link WindowManager}.
     * <dt> {@link #LAYOUT_INFLATER_SERVICE} ("layout_inflater")
     * <dd> A {@link LayoutInflater} for inflating layout resources
     * in this context.
     * <dt> {@link #ACTIVITY_SERVICE} ("activity")
     * <dd> A {@link ActivityManager} for interacting with the
     * global activity state of the system.
     * <dt> {@link #POWER_SERVICE} ("power")
     * <dd> A {@link PowerManager} for controlling power
     * management.
     * <dt> {@link #ALARM_SERVICE} ("alarm")
     * <dd> A {@link AlarmManager} for receiving intents at the
     * time of your choosing.
     * <dt> {@link #NOTIFICATION_SERVICE} ("notification")
     * <dd> A {@link NotificationManager} for informing the user
     * of background events.
     * <dt> {@link #KEYGUARD_SERVICE} ("keyguard")
     * <dd> A {@link KeyguardManager} for controlling keyguard.
     * <dt> {@link #LOCATION_SERVICE} ("location")
     * <dd> A {@link LocationManager} for controlling location
     * (e.g., GPS) updates.
     * <dt> {@link #SEARCH_SERVICE} ("search")
     * <dd> A {@link SearchManager} for handling search.
     * <dt> {@link #VIBRATOR_SERVICE} ("vibrator")
     * <dd> A {@link Vibrator} for interacting with the vibrator
     * hardware.
     * <dt> {@link #CONNECTIVITY_SERVICE} ("connection")
     * <dd> A {@link ConnectivityManager ConnectivityManager} for
     * handling management of network connections.
     * <dt> {@link #IPSEC_SERVICE} ("ipsec")
     * <dd> A {@link IpSecManager IpSecManager} for managing IPSec on
     * sockets and networks.
     * <dt> {@link #WIFI_SERVICE} ("wifi")
     * <dd> A {@link WifiManager WifiManager} for management of Wi-Fi
     * connectivity.  On releases before NYC, it should only be obtained from an application
     * context, and not from any other derived context to avoid memory leaks within the calling
     * process.
     * <dt> {@link #WIFI_AWARE_SERVICE} ("wifiaware")
     * <dd> A {@link WifiAwareManager WifiAwareManager} for management of
     * Wi-Fi Aware discovery and connectivity.
     * <dt> {@link #WIFI_P2P_SERVICE} ("wifip2p")
     * <dd> A {@link WifiP2pManager WifiP2pManager} for management of
     * Wi-Fi Direct connectivity.
     * <dt> {@link #INPUT_METHOD_SERVICE} ("input_method")
     * <dd> An {@link InputMethodManager InputMethodManager}
     * for management of input methods.
     * <dt> {@link #UI_MODE_SERVICE} ("uimode")
     * <dd> An {@link UiModeManager} for controlling UI modes.
     * <dt> {@link #DOWNLOAD_SERVICE} ("download")
     * <dd> A {@link DownloadManager} for requesting HTTP downloads
     * <dt> {@link #BATTERY_SERVICE} ("batterymanager")
     * <dd> A {@link BatteryManager} for managing battery state
     * <dt> {@link #JOB_SCHEDULER_SERVICE} ("taskmanager")
     * <dd>  A {@link JobScheduler} for managing scheduled tasks
     * <dt> {@link #NETWORK_STATS_SERVICE} ("netstats")
     * <dd> A {@link NetworkStatsManager NetworkStatsManager} for querying network
     * usage statistics.
     * <dt> {@link #HARDWARE_PROPERTIES_SERVICE} ("hardware_properties")
     * <dd> A {@link HardwarePropertiesManager} for accessing hardware properties.
     * </dl>
     * <p>
     * <p>Note:  System services obtained via this API may be closely associated with
     * the Context in which they are obtained from.  In general, do not share the
     * service objects between various different contexts (Activities, Applications,
     * Services, Providers, etc.)
     * <p>
     * <p>Note: Instant apps, for which {@link PackageManager#isInstantApp()} returns true,
     * don't have access to the following system services: {@link #DEVICE_POLICY_SERVICE},
     * {@link #FINGERPRINT_SERVICE}, {@link #SHORTCUT_SERVICE}, {@link #USB_SERVICE},
     * {@link #WALLPAPER_SERVICE}, {@link #WIFI_P2P_SERVICE}, {@link #WIFI_SERVICE},
     * {@link #WIFI_AWARE_SERVICE}. For these services this method will return <code>null</code>.
     * Generally, if you are running as an instant app you should always check whether the result
     * of this method is null.
     *
     * @param name The name of the desired service.
     * @return The service or null if the name does not exist.
     * @see #WINDOW_SERVICE
     * @see WindowManager
     * @see #LAYOUT_INFLATER_SERVICE
     * @see LayoutInflater
     * @see #ACTIVITY_SERVICE
     * @see ActivityManager
     * @see #POWER_SERVICE
     * @see PowerManager
     * @see #ALARM_SERVICE
     * @see AlarmManager
     * @see #NOTIFICATION_SERVICE
     * @see NotificationManager
     * @see #KEYGUARD_SERVICE
     * @see KeyguardManager
     * @see #LOCATION_SERVICE
     * @see LocationManager
     * @see #SEARCH_SERVICE
     * @see SearchManager
     * @see #SENSOR_SERVICE
     * @see SensorManager
     * @see #STORAGE_SERVICE
     * @see StorageManager
     * @see #VIBRATOR_SERVICE
     * @see Vibrator
     * @see #CONNECTIVITY_SERVICE
     * @see ConnectivityManager
     * @see #WIFI_SERVICE
     * @see WifiManager
     * @see #AUDIO_SERVICE
     * @see AudioManager
     * @see #MEDIA_ROUTER_SERVICE
     * @see MediaRouter
     * @see #TELEPHONY_SERVICE
     * @see TelephonyManager
     * @see #TELEPHONY_SUBSCRIPTION_SERVICE
     * @see SubscriptionManager
     * @see #CARRIER_CONFIG_SERVICE
     * @see CarrierConfigManager
     * @see #INPUT_METHOD_SERVICE
     * @see InputMethodManager
     * @see #UI_MODE_SERVICE
     * @see UiModeManager
     * @see #DOWNLOAD_SERVICE
     * @see DownloadManager
     * @see #BATTERY_SERVICE
     * @see BatteryManager
     * @see #JOB_SCHEDULER_SERVICE
     * @see JobScheduler
     * @see #NETWORK_STATS_SERVICE
     * @see NetworkStatsManager
     * @see HardwarePropertiesManager
     * @see #HARDWARE_PROPERTIES_SERVICE
     */
    @Nullable
    @Override
    public Object getSystemService(@NonNull String name) {
        return null;
    }

    /**
     * Gets the name of the system-level service that is represented by the specified class.
     *
     * @param serviceClass The class of the desired service.
     * @return The service name or null if the class is not a supported system service.
     */
    @Nullable
    @Override
    public String getSystemServiceName(@NonNull Class<?> serviceClass) {
        return null;
    }

    /**
     * Determine whether the given permission is allowed for a particular
     * process and user ID running in the system.
     *
     * @param permission The name of the permission being checked.
     * @param pid        The process ID being checked against.  Must be > 0.
     * @param uid        The user ID being checked against.  A uid of 0 is the root
     *                   user, which will pass every permission check.
     * @return {@link PackageManager#PERMISSION_GRANTED} if the given
     * pid/uid is allowed that permission, or
     * {@link PackageManager#PERMISSION_DENIED} if it is not.
     * @see PackageManager#checkPermission(String, String)
     * @see #checkCallingPermission
     */
    @SuppressLint("WrongConstant")
    @Override
    public int checkPermission(@NonNull String permission, int pid, int uid) {
        return 0;
    }

    /**
     * Determine whether the calling process of an IPC you are handling has been
     * granted a particular permission.  This is basically the same as calling
     * {@link #checkPermission(String, int, int)} with the pid and uid returned
     * by {@link Binder#getCallingPid} and
     * {@link Binder#getCallingUid}.  One important difference
     * is that if you are not currently processing an IPC, this function
     * will always fail.  This is done to protect against accidentally
     * leaking permissions; you can use {@link #checkCallingOrSelfPermission}
     * to avoid this protection.
     *
     * @param permission The name of the permission being checked.
     * @return {@link PackageManager#PERMISSION_GRANTED} if the calling
     * pid/uid is allowed that permission, or
     * {@link PackageManager#PERMISSION_DENIED} if it is not.
     * @see PackageManager#checkPermission(String, String)
     * @see #checkPermission
     * @see #checkCallingOrSelfPermission
     */
    @SuppressLint("WrongConstant")
    @Override
    public int checkCallingPermission(@NonNull String permission) {
        return 0;
    }

    /**
     * Determine whether the calling process of an IPC <em>or you</em> have been
     * granted a particular permission.  This is the same as
     * {@link #checkCallingPermission}, except it grants your own permissions
     * if you are not currently processing an IPC.  Use with care!
     *
     * @param permission The name of the permission being checked.
     * @return {@link PackageManager#PERMISSION_GRANTED} if the calling
     * pid/uid is allowed that permission, or
     * {@link PackageManager#PERMISSION_DENIED} if it is not.
     * @see PackageManager#checkPermission(String, String)
     * @see #checkPermission
     * @see #checkCallingPermission
     */
    @SuppressLint("WrongConstant")
    @Override
    public int checkCallingOrSelfPermission(@NonNull String permission) {
        return 0;
    }

    /**
     * Determine whether <em>you</em> have been granted a particular permission.
     *
     * @param permission The name of the permission being checked.
     * @return {@link PackageManager#PERMISSION_GRANTED} if you have the
     * permission, or {@link PackageManager#PERMISSION_DENIED} if not.
     * @see PackageManager#checkPermission(String, String)
     * @see #checkCallingPermission(String)
     */
    @SuppressLint("WrongConstant")
    @Override
    public int checkSelfPermission(@NonNull String permission) {
        return 0;
    }

    /**
     * If the given permission is not allowed for a particular process
     * and user ID running in the system, throw a {@link SecurityException}.
     *
     * @param permission The name of the permission being checked.
     * @param pid        The process ID being checked against.  Must be &gt; 0.
     * @param uid        The user ID being checked against.  A uid of 0 is the root
     *                   user, which will pass every permission check.
     * @param message    A message to include in the exception if it is thrown.
     * @see #checkPermission(String, int, int)
     */
    @Override
    public void enforcePermission(@NonNull String permission, int pid, int uid, @Nullable String message) {

    }

    /**
     * If the calling process of an IPC you are handling has not been
     * granted a particular permission, throw a {@link
     * SecurityException}.  This is basically the same as calling
     * {@link #enforcePermission(String, int, int, String)} with the
     * pid and uid returned by {@link Binder#getCallingPid}
     * and {@link Binder#getCallingUid}.  One important
     * difference is that if you are not currently processing an IPC,
     * this function will always throw the SecurityException.  This is
     * done to protect against accidentally leaking permissions; you
     * can use {@link #enforceCallingOrSelfPermission} to avoid this
     * protection.
     *
     * @param permission The name of the permission being checked.
     * @param message    A message to include in the exception if it is thrown.
     * @see #checkCallingPermission(String)
     */
    @Override
    public void enforceCallingPermission(@NonNull String permission, @Nullable String message) {

    }

    /**
     * If neither you nor the calling process of an IPC you are
     * handling has been granted a particular permission, throw a
     * {@link SecurityException}.  This is the same as {@link
     * #enforceCallingPermission}, except it grants your own
     * permissions if you are not currently processing an IPC.  Use
     * with care!
     *
     * @param permission The name of the permission being checked.
     * @param message    A message to include in the exception if it is thrown.
     * @see #checkCallingOrSelfPermission(String)
     */
    @Override
    public void enforceCallingOrSelfPermission(@NonNull String permission, @Nullable String message) {

    }

    /**
     * Grant permission to access a specific Uri to another package, regardless
     * of whether that package has general permission to access the Uri's
     * content provider.  This can be used to grant specific, temporary
     * permissions, typically in response to user interaction (such as the
     * user opening an attachment that you would like someone else to
     * display).
     * <p>
     * <p>Normally you should use {@link Intent#FLAG_GRANT_READ_URI_PERMISSION
     * Intent.FLAG_GRANT_READ_URI_PERMISSION} or
     * {@link Intent#FLAG_GRANT_WRITE_URI_PERMISSION
     * Intent.FLAG_GRANT_WRITE_URI_PERMISSION} with the Intent being used to
     * start an activity instead of this function directly.  If you use this
     * function directly, you should be sure to call
     * {@link #revokeUriPermission} when the target should no longer be allowed
     * to access it.
     * <p>
     * <p>To succeed, the content provider owning the Uri must have set the
     * {@link android.R.styleable#AndroidManifestProvider_grantUriPermissions
     * grantUriPermissions} attribute in its manifest or included the
     * {@link android.R.styleable#AndroidManifestGrantUriPermission
     * &lt;grant-uri-permissions&gt;} tag.
     *
     * @param toPackage The package you would like to allow to access the Uri.
     * @param uri       The Uri you would like to grant access to.
     * @param modeFlags The desired access modes.
     * @see #revokeUriPermission
     */
    @Override
    public void grantUriPermission(String toPackage, Uri uri, int modeFlags) {

    }

    /**
     * Remove all permissions to access a particular content provider Uri
     * that were previously added with {@link #grantUriPermission} or <em>any other</em> mechanism.
     * The given Uri will match all previously granted Uris that are the same or a
     * sub-path of the given Uri.  That is, revoking "content://foo/target" will
     * revoke both "content://foo/target" and "content://foo/target/sub", but not
     * "content://foo".  It will not remove any prefix grants that exist at a
     * higher level.
     * <p>
     * <p>Prior to {@link Build.VERSION_CODES#LOLLIPOP}, if you did not have
     * regular permission access to a Uri, but had received access to it through
     * a specific Uri permission grant, you could not revoke that grant with this
     * function and a {@link SecurityException} would be thrown.  As of
     * {@link Build.VERSION_CODES#LOLLIPOP}, this function will not throw a security
     * exception, but will remove whatever permission grants to the Uri had been given to the app
     * (or none).</p>
     * <p>
     * <p>Unlike {@link #revokeUriPermission(String, Uri, int)}, this method impacts all permission
     * grants matching the given Uri, for any package they had been granted to, through any
     * mechanism this had happened (such as indirectly through the clipboard, activity launch,
     * service start, etc).  That means this can be potentially dangerous to use, as it can
     * revoke grants that another app could be strongly expecting to stick around.</p>
     *
     * @param uri       The Uri you would like to revoke access to.
     * @param modeFlags The access modes to revoke.
     * @see #grantUriPermission
     */
    @Override
    public void revokeUriPermission(Uri uri, int modeFlags) {

    }

    /**
     * Remove permissions to access a particular content provider Uri
     * that were previously added with {@link #grantUriPermission} for a specific target
     * package.  The given Uri will match all previously granted Uris that are the same or a
     * sub-path of the given Uri.  That is, revoking "content://foo/target" will
     * revoke both "content://foo/target" and "content://foo/target/sub", but not
     * "content://foo".  It will not remove any prefix grants that exist at a
     * higher level.
     * <p>
     * <p>Unlike {@link #revokeUriPermission(Uri, int)}, this method will <em>only</em>
     * revoke permissions that had been explicitly granted through {@link #grantUriPermission}
     * and only for the package specified.  Any matching grants that have happened through
     * other mechanisms (clipboard, activity launching, service starting, etc) will not be
     * removed.</p>
     *
     * @param toPackage The package you had previously granted access to.
     * @param uri       The Uri you would like to revoke access to.
     * @param modeFlags The access modes to revoke.
     * @see #grantUriPermission
     */
    @Override
    public void revokeUriPermission(String toPackage, Uri uri, int modeFlags) {

    }

    /**
     * Determine whether a particular process and user ID has been granted
     * permission to access a specific URI.  This only checks for permissions
     * that have been explicitly granted -- if the given process/uid has
     * more general access to the URI's content provider then this check will
     * always fail.
     *
     * @param uri       The uri that is being checked.
     * @param pid       The process ID being checked against.  Must be &gt; 0.
     * @param uid       The user ID being checked against.  A uid of 0 is the root
     *                  user, which will pass every permission check.
     * @param modeFlags The access modes to check.
     * @return {@link PackageManager#PERMISSION_GRANTED} if the given
     * pid/uid is allowed to access that uri, or
     * {@link PackageManager#PERMISSION_DENIED} if it is not.
     * @see #checkCallingUriPermission
     */
    @SuppressLint("WrongConstant")
    @Override
    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
        return 0;
    }

    /**
     * Determine whether the calling process and user ID has been
     * granted permission to access a specific URI.  This is basically
     * the same as calling {@link #checkUriPermission(Uri, int, int, * int)} with the pid and uid returned by {@link
     * Binder#getCallingPid} and {@link
     * Binder#getCallingUid}.  One important difference is
     * that if you are not currently processing an IPC, this function
     * will always fail.
     *
     * @param uri       The uri that is being checked.
     * @param modeFlags The access modes to check.
     * @return {@link PackageManager#PERMISSION_GRANTED} if the caller
     * is allowed to access that uri, or
     * {@link PackageManager#PERMISSION_DENIED} if it is not.
     * @see #checkUriPermission(Uri, int, int, int)
     */
    @SuppressLint("WrongConstant")
    @Override
    public int checkCallingUriPermission(Uri uri, int modeFlags) {
        return 0;
    }

    /**
     * Determine whether the calling process of an IPC <em>or you</em> has been granted
     * permission to access a specific URI.  This is the same as
     * {@link #checkCallingUriPermission}, except it grants your own permissions
     * if you are not currently processing an IPC.  Use with care!
     *
     * @param uri       The uri that is being checked.
     * @param modeFlags The access modes to check.
     * @return {@link PackageManager#PERMISSION_GRANTED} if the caller
     * is allowed to access that uri, or
     * {@link PackageManager#PERMISSION_DENIED} if it is not.
     * @see #checkCallingUriPermission
     */
    @SuppressLint("WrongConstant")
    @Override
    public int checkCallingOrSelfUriPermission(Uri uri, int modeFlags) {
        return 0;
    }

    /**
     * Check both a Uri and normal permission.  This allows you to perform
     * both {@link #checkPermission} and {@link #checkUriPermission} in one
     * call.
     *
     * @param uri             The Uri whose permission is to be checked, or null to not
     *                        do this check.
     * @param readPermission  The permission that provides overall read access,
     *                        or null to not do this check.
     * @param writePermission The permission that provides overall write
     *                        access, or null to not do this check.
     * @param pid             The process ID being checked against.  Must be &gt; 0.
     * @param uid             The user ID being checked against.  A uid of 0 is the root
     *                        user, which will pass every permission check.
     * @param modeFlags       The access modes to check.
     * @return {@link PackageManager#PERMISSION_GRANTED} if the caller
     * is allowed to access that uri or holds one of the given permissions, or
     * {@link PackageManager#PERMISSION_DENIED} if it is not.
     */
    @SuppressLint("WrongConstant")
    @Override
    public int checkUriPermission(@Nullable Uri uri, @Nullable String readPermission, @Nullable String writePermission, int pid, int uid, int modeFlags) {
        return 0;
    }

    /**
     * If a particular process and user ID has not been granted
     * permission to access a specific URI, throw {@link
     * SecurityException}.  This only checks for permissions that have
     * been explicitly granted -- if the given process/uid has more
     * general access to the URI's content provider then this check
     * will always fail.
     *
     * @param uri       The uri that is being checked.
     * @param pid       The process ID being checked against.  Must be &gt; 0.
     * @param uid       The user ID being checked against.  A uid of 0 is the root
     *                  user, which will pass every permission check.
     * @param modeFlags The access modes to enforce.
     * @param message   A message to include in the exception if it is thrown.
     * @see #checkUriPermission(Uri, int, int, int)
     */
    @Override
    public void enforceUriPermission(Uri uri, int pid, int uid, int modeFlags, String message) {

    }

    /**
     * If the calling process and user ID has not been granted
     * permission to access a specific URI, throw {@link
     * SecurityException}.  This is basically the same as calling
     * {@link #enforceUriPermission(Uri, int, int, int, String)} with
     * the pid and uid returned by {@link
     * Binder#getCallingPid} and {@link
     * Binder#getCallingUid}.  One important difference is
     * that if you are not currently processing an IPC, this function
     * will always throw a SecurityException.
     *
     * @param uri       The uri that is being checked.
     * @param modeFlags The access modes to enforce.
     * @param message   A message to include in the exception if it is thrown.
     * @see #checkCallingUriPermission(Uri, int)
     */
    @Override
    public void enforceCallingUriPermission(Uri uri, int modeFlags, String message) {

    }

    /**
     * If the calling process of an IPC <em>or you</em> has not been
     * granted permission to access a specific URI, throw {@link
     * SecurityException}.  This is the same as {@link
     * #enforceCallingUriPermission}, except it grants your own
     * permissions if you are not currently processing an IPC.  Use
     * with care!
     *
     * @param uri       The uri that is being checked.
     * @param modeFlags The access modes to enforce.
     * @param message   A message to include in the exception if it is thrown.
     * @see #checkCallingOrSelfUriPermission(Uri, int)
     */
    @Override
    public void enforceCallingOrSelfUriPermission(Uri uri, int modeFlags, String message) {

    }

    /**
     * Enforce both a Uri and normal permission.  This allows you to perform
     * both {@link #enforcePermission} and {@link #enforceUriPermission} in one
     * call.
     *
     * @param uri             The Uri whose permission is to be checked, or null to not
     *                        do this check.
     * @param readPermission  The permission that provides overall read access,
     *                        or null to not do this check.
     * @param writePermission The permission that provides overall write
     *                        access, or null to not do this check.
     * @param pid             The process ID being checked against.  Must be &gt; 0.
     * @param uid             The user ID being checked against.  A uid of 0 is the root
     *                        user, which will pass every permission check.
     * @param modeFlags       The access modes to enforce.
     * @param message         A message to include in the exception if it is thrown.
     * @see #checkUriPermission(Uri, String, String, int, int, int)
     */
    @Override
    public void enforceUriPermission(@Nullable Uri uri, @Nullable String readPermission, @Nullable String writePermission, int pid, int uid, int modeFlags, @Nullable String message) {

    }

    /**
     * Return a new Context object for the given application name.  This
     * Context is the same as what the named application gets when it is
     * launched, containing the same resources and class loader.  Each call to
     * this method returns a new instance of a Context object; Context objects
     * are not shared, however they share common state (Resources, ClassLoader,
     * etc) so the Context instance itself is fairly lightweight.
     * <p>
     * <p>Throws {@link PackageManager.NameNotFoundException} if there is no
     * application with the given package name.
     * <p>
     * <p>Throws {@link SecurityException} if the Context requested
     * can not be loaded into the caller's process for security reasons (see
     * {@link #CONTEXT_INCLUDE_CODE} for more information}.
     *
     * @param packageName Name of the application's package.
     * @param flags       Option flags.
     * @return A {@link Context} for the application.
     * @throws SecurityException                    &nbsp;
     * @throws PackageManager.NameNotFoundException if there is no application with
     *                                              the given package name.
     */
    @Override
    public Context createPackageContext(String packageName, int flags) throws PackageManager.NameNotFoundException {
        return null;
    }

    /**
     * Return a new Context object for the given split name. The new Context has a ClassLoader and
     * Resources object that can access the split's and all of its dependencies' code/resources.
     * Each call to this method returns a new instance of a Context object;
     * Context objects are not shared, however common state (ClassLoader, other Resources for
     * the same split) may be so the Context itself can be fairly lightweight.
     *
     * @param splitName The name of the split to include, as declared in the split's
     *                  <code>AndroidManifest.xml</code>.
     * @return A {@link Context} with the given split's code and/or resources loaded.
     */
    @Override
    public Context createContextForSplit(String splitName) throws PackageManager.NameNotFoundException {
        return null;
    }

    /**
     * Return a new Context object for the current Context but whose resources
     * are adjusted to match the given Configuration.  Each call to this method
     * returns a new instance of a Context object; Context objects are not
     * shared, however common state (ClassLoader, other Resources for the
     * same configuration) may be so the Context itself can be fairly lightweight.
     *
     * @param overrideConfiguration A {@link Configuration} specifying what
     *                              values to modify in the base Configuration of the original Context's
     *                              resources.  If the base configuration changes (such as due to an
     *                              orientation change), the resources of this context will also change except
     *                              for those that have been explicitly overridden with a value here.
     * @return A {@link Context} with the given configuration override.
     */
    @Override
    public Context createConfigurationContext(@NonNull Configuration overrideConfiguration) {
        return null;
    }

    /**
     * Return a new Context object for the current Context but whose resources
     * are adjusted to match the metrics of the given Display.  Each call to this method
     * returns a new instance of a Context object; Context objects are not
     * shared, however common state (ClassLoader, other Resources for the
     * same configuration) may be so the Context itself can be fairly lightweight.
     * <p>
     * The returned display Context provides a {@link WindowManager}
     * (see {@link #getSystemService(String)}) that is configured to show windows
     * on the given display.  The WindowManager's {@link WindowManager#getDefaultDisplay}
     * method can be used to retrieve the Display from the returned Context.
     *
     * @param display A {@link Display} object specifying the display
     *                for whose metrics the Context's resources should be tailored and upon which
     *                new windows should be shown.
     * @return A {@link Context} for the display.
     */
    @Override
    public Context createDisplayContext(@NonNull Display display) {
        return null;
    }

    /**
     * Return a new Context object for the current Context but whose storage
     * APIs are backed by device-protected storage.
     * <p>
     * On devices with direct boot, data stored in this location is encrypted
     * with a key tied to the physical device, and it can be accessed
     * immediately after the device has booted successfully, both
     * <em>before and after</em> the user has authenticated with their
     * credentials (such as a lock pattern or PIN).
     * <p>
     * Because device-protected data is available without user authentication,
     * you should carefully limit the data you store using this Context. For
     * example, storing sensitive authentication tokens or passwords in the
     * device-protected area is strongly discouraged.
     * <p>
     * If the underlying device does not have the ability to store
     * device-protected and credential-protected data using different keys, then
     * both storage areas will become available at the same time. They remain as
     * two distinct storage locations on disk, and only the window of
     * availability changes.
     * <p>
     * Each call to this method returns a new instance of a Context object;
     * Context objects are not shared, however common state (ClassLoader, other
     * Resources for the same configuration) may be so the Context itself can be
     * fairly lightweight.
     *
     * @see #isDeviceProtectedStorage()
     */
    @Override
    public Context createDeviceProtectedStorageContext() {
        return null;
    }

    /**
     * Indicates if the storage APIs of this Context are backed by
     * device-protected storage.
     *
     * @see #createDeviceProtectedStorageContext()
     */
    @Override
    public boolean isDeviceProtectedStorage() {
        return false;
    }
}

