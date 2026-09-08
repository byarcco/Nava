package ir.cutte.nava.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage
import ir.cutte.nava.data.SettingsRepository
import ir.cutte.nava.data.navaDataStore
import ir.cutte.nava.engine.HttpDispatcher
import ir.cutte.nava.engine.MessageMatcher
import ir.cutte.nava.engine.SenderNormalizer
import ir.cutte.nava.model.ForwardingActivity
import ir.cutte.nava.model.SmsPayload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

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
                val timestamp = messages.first().timestampMillis.takeIf { it > 0 } ?: System.currentTimeMillis()

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

                val matchResult = MessageMatcher.match(
                    rawSender = rawSender,
                    body = fullBody,
                    whitelist = settings.whitelistSenders,
                    keywords = settings.keywords
                )

                if (!matchResult.isMatched) {
                    return@launch
                }

                val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
                val deviceInfo = "$manufacturer ${Build.MODEL}"

                val payload = SmsPayload(
                    sender = rawSender,
                    body = fullBody,
                    timestamp = timestamp,
                    simSlot = simSlot,
                    matchedKeyword = matchResult.matchedKeyword,
                    deviceInfo = deviceInfo
                )

                val dispatcher = HttpDispatcher()
                val dispatchResult = dispatcher.dispatch(settings.workerUrl, payload)

                val normalizedSender = SenderNormalizer.normalize(rawSender)
                val snippet = if (fullBody.length > 60) fullBody.take(60) + "..." else fullBody

                val activity = ForwardingActivity(
                    id = UUID.randomUUID().toString(),
                    timestamp = timestamp,
                    normalizedSender = normalizedSender,
                    matchedKeyword = matchResult.matchedKeyword,
                    httpStatusCode = dispatchResult.statusCode,
                    isSuccess = dispatchResult.isSuccess,
                    snippet = snippet
                )

                repository.recordActivity(activity)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
