package com.example.leadpedlar.calling

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import com.example.leadpedlar.data.model.CallAppType

data class CallAppInfo(
    val appType: CallAppType,
    val isInstalled: Boolean,
    val isDefault: Boolean = false
)

object CallManager {

    fun cleanPhoneNumber(rawPhone: String): String {
        // Keep digits and leading +
        val trimmed = rawPhone.trim()
        val hasPlus = trimmed.startsWith("+")
        val digits = trimmed.replace(Regex("[^0-9]"), "")
        return if (hasPlus) "+$digits" else digits
    }

    fun isPackageInstalled(context: Context, packageName: String?): Boolean {
        if (packageName == null) return true // System dialer / chooser is always supported
        return try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    fun getAvailableCallApps(context: Context, currentDefault: CallAppType): List<CallAppInfo> {
        return CallAppType.entries.map { type ->
            val installed = isPackageInstalled(context, type.packageName)
            val isDef = (type == currentDefault)
            CallAppInfo(appType = type, isInstalled = installed, isDefault = isDef)
        }.sortedWith(
            compareByDescending<CallAppInfo> { it.isInstalled }
                .thenByDescending { it.isDefault }
        )
    }

    fun launchCall(context: Context, appType: CallAppType, phoneNumber: String) {
        val cleanPhone = cleanPhoneNumber(phoneNumber)

        try {
            when (appType) {
                CallAppType.SYSTEM_DIALER -> {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$cleanPhone")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }

                CallAppType.WHATSAPP -> {
                    if (isPackageInstalled(context, "com.whatsapp")) {
                        // International format without leading + for WhatsApp URL API
                        val waDigits = cleanPhone.replace("+", "")
                        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$waDigits")
                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                            setPackage("com.whatsapp")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } else {
                        openPlayStore(context, "com.whatsapp", "WhatsApp is not installed")
                    }
                }

                CallAppType.WHATSAPP_BUSINESS -> {
                    if (isPackageInstalled(context, "com.whatsapp.w4b")) {
                        val waDigits = cleanPhone.replace("+", "")
                        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$waDigits")
                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                            setPackage("com.whatsapp.w4b")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } else {
                        openPlayStore(context, "com.whatsapp.w4b", "WhatsApp Business is not installed")
                    }
                }

                CallAppType.SKYPE -> {
                    if (isPackageInstalled(context, "com.skype.raider")) {
                        val uri = Uri.parse("skype:$cleanPhone?call")
                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } else {
                        openPlayStore(context, "com.skype.raider", "Skype is not installed")
                    }
                }

                CallAppType.TELEGRAM -> {
                    if (isPackageInstalled(context, "org.telegram.messenger")) {
                        val uri = Uri.parse("https://t.me/$cleanPhone")
                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                            setPackage("org.telegram.messenger")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } else {
                        openPlayStore(context, "org.telegram.messenger", "Telegram is not installed")
                    }
                }

                CallAppType.TRUECALLER -> {
                    if (isPackageInstalled(context, "com.truecaller")) {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$cleanPhone")
                            setPackage("com.truecaller")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } else {
                        openPlayStore(context, "com.truecaller", "Truecaller is not installed")
                    }
                }

                CallAppType.VIBER -> {
                    if (isPackageInstalled(context, "com.viber.voip")) {
                        val uri = Uri.parse("viber://chat?number=$cleanPhone")
                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } else {
                        openPlayStore(context, "com.viber.voip", "Viber is not installed")
                    }
                }

                CallAppType.SYSTEM_CHOOSER -> {
                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$cleanPhone")
                    }
                    val chooser = Intent.createChooser(dialIntent, "Make call with...").apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(chooser)
                }
            }
        } catch (e: Exception) {
            // Fallback to simple phone dialer
            try {
                val fallbackIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$cleanPhone")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
            } catch (err: Exception) {
                Toast.makeText(context, "Unable to place call: ${err.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openPlayStore(context: Context, packageName: String, message: String) {
        Toast.makeText(context, "$message. Opening Google Play...", Toast.LENGTH_SHORT).show()
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
        }
    }
}
