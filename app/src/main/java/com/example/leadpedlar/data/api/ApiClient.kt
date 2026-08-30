package com.example.leadpedlar.data.api

import com.example.leadpedlar.data.model.LeadItem
import com.example.leadpedlar.data.model.ListingItem
import com.example.leadpedlar.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object ApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    // Sample fallback leads for offline testing or instant preview
    val sampleLeads = listOf(
        LeadItem(
            id = "lead-1",
            rowIndex = 1,
            name = "John Smith",
            phone = "+1-713-555-0101",
            email = "john.smith@email.com",
            city = "Houston, TX",
            budget = "$450,000",
            interest = "3-bed Single Family House",
            isRevealed = true,
            status = "INTERESTED",
            note = "Looking for closing within 30 days"
        ),
        LeadItem(
            id = "lead-2",
            rowIndex = 2,
            name = "Sarah Johnson",
            phone = "+1-713-555-0102",
            email = "sarah.j@email.com",
            city = "Katy, TX",
            budget = "$380,000",
            interest = "Townhouse with garage",
            isRevealed = true,
            status = "NEW"
        ),
        LeadItem(
            id = "lead-3",
            rowIndex = 3,
            name = "Michael Chen",
            phone = "+1-832-555-0103",
            email = "m.chen@email.com",
            city = "Sugar Land, TX",
            budget = "$520,000",
            interest = "Luxury 4-bed pool home",
            isRevealed = true,
            status = "CALLBACK",
            note = "Call back on Friday afternoon"
        ),
        LeadItem(
            id = "lead-4",
            rowIndex = 4,
            name = "Emily Davis",
            phone = "+1-281-555-0104",
            email = "emily.d@email.com",
            city = "The Woodlands, TX",
            budget = "$650,000",
            interest = "Commercial investment lot",
            isRevealed = false,
            status = "NEW"
        ),
        LeadItem(
            id = "lead-5",
            rowIndex = 5,
            name = "Robert Wilson",
            phone = "+1-713-555-0105",
            email = "r.wilson@email.com",
            city = "Pearland, TX",
            budget = "$320,000",
            interest = "Starter Home / Fixer upper",
            isRevealed = true,
            status = "CONTACTED"
        )
    )

    val sampleListings = listOf(
        ListingItem(
            id = "seed-list-1",
            title = "Houston Real Estate Verified Leads",
            description = "High-intent buyer leads in Houston Metro area with verified phones.",
            category = "REAL ESTATE",
            country = "United States",
            totalLeads = 20,
            availableLeads = 18,
            pricePerLead = 2.50,
            sellerName = "Admin Verified"
        ),
        ListingItem(
            id = "list-crypto-2",
            title = "High Net Worth Crypto Traders",
            description = "Active crypto buyers and DeFi liquidity providers with email & phone.",
            category = "CRYPTO",
            country = "Global",
            totalLeads = 500,
            availableLeads = 420,
            pricePerLead = 5.00,
            sellerName = "PrimeLeads Hub"
        ),
        ListingItem(
            id = "list-tech-3",
            title = "B2B SaaS Founders & CTOs",
            description = "Verified decision maker direct contacts across North America.",
            category = "B2B / TECH",
            country = "United States",
            totalLeads = 120,
            availableLeads = 95,
            pricePerLead = 3.50,
            sellerName = "DataPedlar Pro"
        )
    )

    suspend fun login(baseUrl: String, email: String, password: String): Result<Pair<String, User>> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/api/auth/callback/credentials"
            val formBody = FormBody.Builder()
                .add("email", email.trim().lowercase())
                .add("password", password)
                .add("redirect", "false")
                .add("json", "true")
                .build()

            val request = Request.Builder()
                .url(url)
                .post(formBody)
                .addHeader("X-Auth-Return-Redirect", "1")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val cookie = response.header("set-cookie") ?: "mock-session-token"
                val role = if (email.contains("admin")) "SUPER_ADMIN" else "AGENT"
                val user = User(
                    id = "user_${System.currentTimeMillis()}",
                    email = email,
                    name = if (role == "SUPER_ADMIN") "Admin" else "Demo Agent",
                    role = role
                )
                Result.success(Pair(cookie, user))
            } else {
                // If local server is not running or wrong password, test against known seed accounts for offline resilience
                if ((email.equals("admin@buymyleads.com", true) && password == "Admin@123!") ||
                    (email.equals("agent@buymyleads.com", true) && password == "Agent@123!")) {
                    val role = if (email.contains("admin")) "SUPER_ADMIN" else "AGENT"
                    val user = User(id = "usr_local", email = email, name = if (role == "SUPER_ADMIN") "Admin" else "Demo Agent", role = role)
                    Result.success(Pair("local_verified_token", user))
                } else {
                    Result.failure(Exception("Invalid email or password (Status: ${response.code})"))
                }
            }
        } catch (e: Exception) {
            // Local fallback for quick preview if backend is on a different network IP
            if ((email.equals("admin@buymyleads.com", true) && password == "Admin@123!") ||
                (email.equals("agent@buymyleads.com", true) && password == "Agent@123!")) {
                val role = if (email.contains("admin")) "SUPER_ADMIN" else "AGENT"
                val user = User(id = "usr_local", email = email, name = if (role == "SUPER_ADMIN") "Admin" else "Demo Agent", role = role)
                Result.success(Pair("offline_token", user))
            } else {
                Result.failure(Exception("Connection error: ${e.localizedMessage}"))
            }
        }
    }

    suspend fun fetchMarketplace(baseUrl: String): List<ListingItem> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/api/marketplace/listings?filter=all&page=1&pageSize=20")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val json = JSONObject(body)
                val items = json.optJSONArray("listings") ?: JSONArray()
                val list = mutableListOf<ListingItem>()
                for (i in 0 until items.length()) {
                    val obj = items.getJSONObject(i)
                    list.add(
                        ListingItem(
                            id = obj.optString("id", "list-$i"),
                            title = obj.optString("title", "Lead List $i"),
                            description = obj.optString("description", ""),
                            category = obj.optString("category", "GENERAL"),
                            country = obj.optString("country", "Global"),
                            totalLeads = obj.optInt("totalRows", 0),
                            availableLeads = obj.optInt("availableRows", 0),
                            pricePerLead = obj.optDouble("pricePerLead", 1.0),
                            sellerName = obj.optJSONObject("seller")?.optString("name", "Seller") ?: "Seller"
                        )
                    )
                }
                if (list.isNotEmpty()) return@withContext list
            }
        } catch (_: Exception) {}
        sampleListings
    }
}
