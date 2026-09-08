package ir.cutte.nava.worker

import android.content.Context
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ir.cutte.nava.data.SettingsRepository
import ir.cutte.nava.data.navaDataStore
import ir.cutte.nava.engine.HttpDispatcher
import ir.cutte.nava.model.HeartbeatPayload
import ir.cutte.nava.util.BatteryUtil

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

        val batteryStatus = BatteryUtil.getBatteryStatus(applicationContext)
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val deviceInfo = "$manufacturer ${Build.MODEL}"
        val now = System.currentTimeMillis()

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
        return Result.success()
    }
}
