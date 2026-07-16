package com.novasec.secureauth.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth

object SupabaseClient {
    
    // Replace with your actual Supabase project credentials
    private const val SUPABASE_URL = "https://oblshjqrjppahkurcaft.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9ibHNoanFyanBwYWhrdXJjYWZ0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODA0MTk5MDcsImV4cCI6MjA5NTk5NTkwN30.BTv1IwklfVhihEJS0KuFHqciYCLVJPpnsVrMn_rjBVg"
    
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            install(Auth)
        }
    }
    
    val auth: Auth
        get() = client.auth
}
