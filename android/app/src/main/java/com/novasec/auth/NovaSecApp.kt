package com.novasec.auth

import android.app.Application
import com.novasec.auth.data.remote.SupabaseClient

class NovaSecApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize Supabase client
        SupabaseClient.initialize()
    }
}
