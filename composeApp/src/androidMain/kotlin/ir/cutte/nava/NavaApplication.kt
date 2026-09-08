package ir.cutte.nava

import android.app.Application
import ir.cutte.nava.data.SettingsRepository
import ir.cutte.nava.data.navaDataStore
import ir.cutte.nava.service.NavaForegroundService
import ir.cutte.nava.util.PermissionHelper
import ir.cutte.nava.worker.HeartbeatScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NavaApplication : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            val repository = SettingsRepository(navaDataStore)
            val settings = repository.getSnapshot()
            if (settings.isServiceEnabled && PermissionHelper.hasForegroundServicePermission(this@NavaApplication)) {
                try {
                    NavaForegroundService.start(this@NavaApplication)
                } catch (ignored: Exception) {
                }
            }
            if (settings.isHeartbeatEnabled) {
                try {
                    HeartbeatScheduler.schedule(this@NavaApplication)
                } catch (ignored: Exception) {
                }
            }
        }
    }
}
