// Copyright 2023 Citra Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package org.citra.citra_emu

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.preference.PreferenceManager
import org.citra.citra_emu.utils.DirectoryInitialization
import org.citra.citra_emu.utils.DocumentsTree
import org.citra.citra_emu.utils.GpuDriverHelper
import org.citra.citra_emu.utils.Log
import org.citra.citra_emu.utils.PermissionsHandler
import org.citra.citra_emu.vr.VRUtils

class CitraApplication : Application() {
    private fun createNotificationChannel() {
        with(getSystemService(NotificationManager::class.java)) {
            // General notification
            val name: CharSequence = getString(R.string.app_notification_channel_name)
            val description = getString(R.string.app_notification_channel_description)
            val generalChannel = NotificationChannel(
                getString(R.string.app_notification_channel_id),
                name,
                NotificationManager.IMPORTANCE_LOW
            )
            generalChannel.description = description
            generalChannel.setSound(null, null)
            generalChannel.vibrationPattern = null
            createNotificationChannel(generalChannel)

            // CIA Install notifications
            val ciaChannel = NotificationChannel(
                getString(R.string.cia_install_notification_channel_id),
                getString(R.string.cia_install_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            ciaChannel.description =
                getString(R.string.cia_install_notification_channel_description)
            ciaChannel.setSound(null, null)
            ciaChannel.vibrationPattern = null
            createNotificationChannel(ciaChannel)
        }
    }

    private fun updateLaunchVersionPrefs() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val releaseVersionPrev : String = preferences.getString(VRUtils.PREF_RELEASE_VERSION_NAME_LAUNCH_CURRENT, "")!!
        val releaseVersionCur : String = BuildConfig.VERSION_NAME
        preferences.edit()
            .putString(VRUtils.PREF_RELEASE_VERSION_NAME_LAUNCH_PREV, releaseVersionPrev)
            .putString(VRUtils.PREF_RELEASE_VERSION_NAME_LAUNCH_CURRENT, releaseVersionCur)
            .apply()
        Log.info("Version: \"${preferences.getString(VRUtils.PREF_RELEASE_VERSION_NAME_LAUNCH_PREV, "")}\" (prev) -> \"${preferences.getString(
            VRUtils.PREF_RELEASE_VERSION_NAME_LAUNCH_CURRENT, "")}\" (current)")
    }

    override fun onCreate() {
        super.onCreate()
        updateLaunchVersionPrefs()
        application = this
        documentsTree = DocumentsTree()
        if (PermissionsHandler.hasWriteAccess(applicationContext)) {
            DirectoryInitialization.start()
        }

        NativeLibrary.logDeviceInfo()
        createNotificationChannel()
    }

    companion object {
        private var application: CitraApplication? = null

        val appContext: Context get() = application!!.applicationContext

        @SuppressLint("StaticFieldLeak")
        lateinit var documentsTree: DocumentsTree
    }
}
