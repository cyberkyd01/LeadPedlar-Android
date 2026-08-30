package com.example.leadpedlar.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val email: String = "",
    val name: String? = null,
    val role: String = "AGENT",
    val status: String = "ACTIVE"
)

@Serializable
data class LeadItem(
    val id: String,
    val rowIndex: Int,
    val name: String,
    val phone: String,
    val email: String? = null,
    val city: String? = null,
    val budget: String? = null,
    val interest: String? = null,
    val isRevealed: Boolean = false,
    val isCopied: Boolean = false,
    val callCount: Int = 0,
    val lastCalledAt: String? = null,
    val status: String = "NEW", // NEW, CONTACTED, INTERESTED, CALLBACK, CLOSED
    val note: String? = null
)

@Serializable
data class ListingItem(
    val id: String,
    val title: String,
    val description: String? = null,
    val category: String = "GENERAL",
    val country: String = "Global",
    val totalLeads: Int = 0,
    val availableLeads: Int = 0,
    val pricePerLead: Double = 0.0,
    val sellerName: String = "Verified Seller",
    val isApproved: Boolean = true
)

enum class CallAppType(
    val id: String,
    val displayName: String,
    val packageName: String?,
    val description: String,
    val iconEmoji: String
) {
    SYSTEM_DIALER(
        id = "system_dialer",
        displayName = "Phone Dialer",
        packageName = null,
        description = "Native Android dialer app",
        iconEmoji = "📞"
    ),
    WHATSAPP(
        id = "whatsapp",
        displayName = "WhatsApp",
        packageName = "com.whatsapp",
        description = "Direct chat & WhatsApp call",
        iconEmoji = "💬"
    ),
    WHATSAPP_BUSINESS(
        id = "whatsapp_business",
        displayName = "WhatsApp Business",
        packageName = "com.whatsapp.w4b",
        description = "Business communication",
        iconEmoji = "💼"
    ),
    TELEGRAM(
        id = "telegram",
        displayName = "Telegram",
        packageName = "org.telegram.messenger",
        description = "Telegram call & message",
        iconEmoji = "✈️"
    ),
    SKYPE(
        id = "skype",
        displayName = "Skype",
        packageName = "com.skype.raider",
        description = "Skype VoIP call",
        iconEmoji = "🌐"
    ),
    TRUECALLER(
        id = "truecaller",
        displayName = "Truecaller",
        packageName = "com.truecaller",
        description = "Truecaller caller ID & dialer",
        iconEmoji = "🔍"
    ),
    VIBER(
        id = "viber",
        displayName = "Viber",
        packageName = "com.viber.voip",
        description = "Viber voice & message",
        iconEmoji = "💜"
    ),
    SYSTEM_CHOOSER(
        id = "system_chooser",
        displayName = "Always Ask (System Chooser)",
        packageName = null,
        description = "Show full Android application picker",
        iconEmoji = "⚙️"
    );

    companion object {
        fun fromId(id: String?): CallAppType {
            return entries.firstOrNull { it.id == id } ?: SYSTEM_CHOOSER
        }
    }
}
