package ir.cutte.nava.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ir.cutte.nava.data.SettingsRepository
import ir.cutte.nava.data.navaDataStore
import ir.cutte.nava.service.NavaForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) {
            return
        }

        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        scope.launch {
            try {
                val repository = SettingsRepository(context.applicationContext.navaDataStore)
                val settings = repository.getSnapshot()
                if (settings.isServiceEnabled) {
                    NavaForegroundService.start(context.applicationContext)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
