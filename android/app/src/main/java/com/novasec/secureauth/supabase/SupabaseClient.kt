package com.novasec.secureauth.supabase

import android.content.Context
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.android.Android

// Renamed from "SupabaseClient" — that name collided with the imported
// io.github.jan.supabase.SupabaseClient class. Kotlin let the local
// declaration silently shadow the import, so every reference to
// "SupabaseClient" in this file (the property type, the return type)
// resolved to this empty singleton instead of the real client type,
// which is why .auth / .postgrest / etc. were all unresolved elsewhere.
object SupabaseManager {
    // IMPORTANT: Replace these with your actual Supabase credentials
    private const val SUPABASE_URL = "https://oblshjqrjppahkurcaft.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9ibHNoanFyanBwYWhrdXJjYWZ0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODA0MTk5MDcsImV4cCI6MjA5NTk5NTkwN30.BTv1IwklfVhihEJS0KuFHqciYCLVJPpnsVrMn_rjBVg"

    private var instance: SupabaseClient? = null

    fun getInstance(context: Context): SupabaseClient {
        if (instance == null) {
            instance = createSupabaseClient(SUPABASE_URL, SUPABASE_ANON_KEY) {
                // Android needs the engine *factory* here, not the object itself
                httpEngine = Android.create()

                install(Auth) {
                    // v3 config lives inside the install block, not a separate auth { } call
                    alwaysAutoRefresh = true
                    autoLoadFromStorage = true
                }
                install(Postgrest)
                install(Realtime)
                install(Storage)
            }
        }
        return instance!!
    }
}

// Extension function for easier access
fun Context.supabaseClient() = SupabaseManager.getInstance(this)
