package com.example.vibevision.data.local

import android.content.Context
import com.example.vibevision.model.UserProfile

interface UserProfileStorage {
    fun loadProfile(userId: String?): UserProfile
    fun saveProfile(userId: String?, profile: UserProfile)
}

class SharedPreferencesUserProfileStorage(context: Context) : UserProfileStorage {
    private val preferences = context.getSharedPreferences("vibevision_profile", Context.MODE_PRIVATE)

    private fun scopedKey(userId: String?, field: String): String {
        val owner = userId?.takeIf { it.isNotBlank() } ?: "guest"
        return "${owner}_$field"
    }

    override fun loadProfile(userId: String?): UserProfile {
        return UserProfile(
            name = preferences.getString(scopedKey(userId, "name"), "").orEmpty(),
            address = preferences.getString(scopedKey(userId, "address"), "").orEmpty(),
            phone = preferences.getString(scopedKey(userId, "phone"), "").orEmpty(),
            email = preferences.getString(scopedKey(userId, "email"), "").orEmpty()
        )
    }

    override fun saveProfile(userId: String?, profile: UserProfile) {
        preferences.edit()
            .putString(scopedKey(userId, "name"), profile.name)
            .putString(scopedKey(userId, "address"), profile.address)
            .putString(scopedKey(userId, "phone"), profile.phone)
            .putString(scopedKey(userId, "email"), profile.email)
            .apply()
    }
}
