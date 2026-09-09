package ir.cutte.nava.worker

import android.content.Context
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ir.cutte.nava.data.SettingsRepository
import ir.cutte.nava.data.navaDataStore
import ir.cutte.nava.engine.HttpDispatcher
import ir.cutte.nava.model.HeartbeatPayload
import ir.cutte.nava.model.ProbeStatus
import ir.cutte.nava.util.BatteryUtil
import ir.cutte.nava.util.NetworkUtil

class HeartbeatWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repository = SettingsRepository(applicationContext.navaDataStore)
        val settings = repository.getSnapshot()

        if (!settings.isHeartbeatEnabled) {
            return Result.success()
        }

        val now = System.currentTimeMillis()

        if (!NetworkUtil.isOnline(applicationContext)) {
            repository.updateProbeStatus(ProbeStatus.OFFLINE, now)
            return Result.success()
        }

        val batteryStatus = BatteryUtil.getBatteryStatus(applicationContext)
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val deviceInfo = "$manufacturer ${Build.MODEL}"

        val payload = HeartbeatPayload(
            status = "alive",
            batteryLevel = batteryStatus.batteryLevel,
            isCharging = batteryStatus.isCharging,
            deviceInfo = deviceInfo,
            timestamp = now
        )

        val dispatcher = HttpDispatcher()
        val probeStatus = dispatcher.probeHealth(
            primaryUrl = settings.primaryWorkerUrl,
            secondaryUrl = settings.secondaryWorkerUrl,
            authToken = settings.authToken,
            payload = payload
        )

        repository.updateProbeStatus(probeStatus, now)

        val isConnected = probeStatus == ProbeStatus.CONNECTED_PRIMARY || probeStatus == ProbeStatus.CONNECTED_BACKUP

        if (isConnected) {
            if (settings.firstFailedProbeTimestamp != 0L || settings.currentProbeIntervalMinutes != 30L) {
                repository.resetProbeBackoff()
                HeartbeatScheduler.schedule(applicationContext, 30L)
            }
        } else {
            val failureStart = if (settings.firstFailedProbeTimestamp > 0L) settings.firstFailedProbeTimestamp else now
            val elapsedFailure = now - failureStart
            val targetInterval = when {
                elapsedFailure >= 24 * 60 * 60 * 1000L -> 120L
                elapsedFailure >= 6 * 60 * 60 * 1000L -> 60L
                else -> 30L
            }

            repository.updateProbeBackoff(failureStart, targetInterval)
            if (targetInterval != settings.currentProbeIntervalMinutes) {
                HeartbeatScheduler.schedule(applicationContext, targetInterval)
            }
        }

        return Result.success()
    }
}
