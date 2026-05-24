package com.mjproductions.fiscal

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest

// Single instance shared across the app
val supabaseClient = createSupabaseClient(
    supabaseUrl = "https://iymwleromrtbsthtlptk.supabase.co",   // replace with yours
    supabaseKey = "sb_publishable_AZafK1NNF2CmXg4cTmTxhg_-sxIpa-d"              // replace with yours
) {
    install(Auth)
    install(Postgrest)
}