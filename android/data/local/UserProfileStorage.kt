package com.example.vibevision.data.local

import android.content.Context
import com.example.vibevision.model.UserProfile

interface UserProfileStorage {
    fun loadProfile(): UserProfile
    fun saveProfile(profile: UserProfile)
}

class SharedPreferencesUserProfileStorage(context: Context) : UserProfileStorage {
    private val preferences = context.getSharedPreferences("vibevision_profile", Context.MODE_PRIVATE)

    override fun loadProfile(): UserProfile {
        return UserProfile(
            name = preferences.getString("name", "").orEmpty(),
            address = preferences.getString("address", "").orEmpty(),
            phone = preferences.getString("phone", "").orEmpty(),
            email = preferences.getString("email", "").orEmpty()
        )
    }

    override fun saveProfile(profile: UserProfile) {
        preferences.edit()
            .putString("name", profile.name)
            .putString("address", profile.address)
            .putString("phone", profile.phone)
            .putString("email", profile.email)
            .apply()
    }
}
