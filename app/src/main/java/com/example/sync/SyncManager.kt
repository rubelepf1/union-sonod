package com.example.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SyncManager {
    private const val ONE_TIME_SYNC_WORK = "one_time_certificate_sync"
    private const val PERIODIC_SYNC_WORK = "periodic_certificate_sync"

    @Volatile
    private var isNetworkCallbackRegistered = false

    /**
     * Initializes background sync and registers network connectivity observer.
     */
    fun init(context: Context) {
        schedulePeriodicSync(context)
        registerNetworkCallback(context)
    }

    /**
     * Triggers an immediate one-time sync with NetworkType.CONNECTED constraint.
     */
    fun triggerImmediateSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<CertificateSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            ONE_TIME_SYNC_WORK,
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    /**
     * Schedules periodic 15-minute background sync when device is connected.
     */
    fun schedulePeriodicSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicRequest = PeriodicWorkRequestBuilder<CertificateSyncWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_SYNC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }

    /**
     * Automatically triggers immediate push when device regains connectivity.
     */
    fun registerNetworkCallback(context: Context) {
        if (isNetworkCallbackRegistered) return
        try {
            val cm = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (cm != null) {
                cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        super.onAvailable(network)
                        Log.i("SyncManager", "Network connected! Pushing pending local data to Supabase.")
                        triggerImmediateSync(context.applicationContext)
                    }
                })
                isNetworkCallbackRegistered = true
            }
        } catch (e: Exception) {
            Log.w("SyncManager", "Could not register default network callback", e)
        }
    }
}
