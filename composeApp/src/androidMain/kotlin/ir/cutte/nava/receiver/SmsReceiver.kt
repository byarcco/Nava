package ir.cutte.nava.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage
import ir.cutte.nava.data.SettingsRepository
import ir.cutte.nava.data.navaDataStore
import ir.cutte.nava.engine.SmsIngestionProcessor
import ir.cutte.nava.util.BatteryUtil
import ir.cutte.nava.util.NetworkUtil
import ir.cutte.nava.worker.SmsDispatchWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        scope.launch {
            try {
                val repository = SettingsRepository(context.applicationContext.navaDataStore)
                val settings = repository.getSnapshot()

                if (!settings.isServiceEnabled) {
                    return@launch
                }

                val bundle = intent.extras ?: return@launch
                val pdus = bundle.get("pdus") as? Array<*> ?: return@launch
                val format = bundle.getString("format")

                val messages = mutableListOf<SmsMessage>()
                for (pdu in pdus) {
                    val pduBytes = pdu as? ByteArray ?: continue
                    val smsMessage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        SmsMessage.createFromPdu(pduBytes, format)
                    } else {
                        @Suppress("DEPRECATION")
                        SmsMessage.createFromPdu(pduBytes)
                    }
                    if (smsMessage != null) {
                        messages.add(smsMessage)
                    }
                }

                if (messages.isEmpty()) {
                    return@launch
                }

                val rawSender = messages.first().displayOriginatingAddress ?: ""
                val fullBody = messages.joinToString("") { it.displayMessageBody ?: "" }
                val simSlot = intent.getIntExtra(
                    "slot",
                    intent.getIntExtra(
                        "simSlot",
                        intent.getIntExtra(
                            "phone",
                            intent.getIntExtra("subscription", 0)
                        )
                    )
                )

                val batteryStatus = BatteryUtil.getBatteryStatus(context)
                val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
                val deviceInfo = "$manufacturer ${Build.MODEL}"
                val isOnline = NetworkUtil.isOnline(context)

                val processor = SmsIngestionProcessor(repository)
                processor.processIncoming(
                    settings = settings,
                    rawSender = rawSender,
                    body = fullBody,
                    simSlot = simSlot,
                    batteryLevel = batteryStatus.batteryLevel,
                    isCharging = batteryStatus.isCharging,
                    deviceInfo = deviceInfo,
                    isOnline = isOnline,
                    onEnqueueWorker = { activityId, payload ->
                        SmsDispatchWorker.enqueue(
                            context = context.applicationContext,
                            activityId = activityId,
                            primaryUrl = settings.primaryWorkerUrl,
                            secondaryUrl = settings.secondaryWorkerUrl,
                            authToken = settings.authToken,
                            payload = payload
                        )
                    }
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}
