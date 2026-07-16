package com.novasec.secureauth.supabase

import android.content.Context
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.android.Android
import android.content.Intent

object SupabaseClient {
    // IMPORTANT: Replace these with your actual Supabase credentials
    private const val SUPABASE_URL = "https://oblshjqrjppahkurcaft.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9ibHNoanFyanBwYWhrdXJjYWZ0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODA0MTk5MDcsImV4cCI6MjA5NTk5NTkwN30.BTv1IwklfVhihEJS0KuFHqciYCLVJPpnsVrMn_rjBVg"
    
    private var instance: SupabaseClient? = null
    
    fun getInstance(context: Context): SupabaseClient {
        if (instance == null) {
            instance = createSupabaseClient(SUPABASE_URL, SUPABASE_ANON_KEY) {
                // Use Android engine for better compatibility
                httpEngine = Android
                
                // Install required modules
                install(Auth)
                install(Postgrest)
                install(Realtime)
                install(Storage)
                
                // Configure auth settings
                auth {
                    // Enable session auto-refresh
                    autoRefreshToken = true
                    // Cache session
                    enableSessionCache()
                }
            }
        }
        return instance!!
    }
}

// Extension function for easier access
fun Context.supabaseClient() = SupabaseClient.getInstance(this)
