package com.venkateshgowda.personallibrary.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore("library_settings")

class AppSettings(private val context: Context) {
    val theme = context.settingsDataStore.data.map { it[THEME] ?: "System" }
    val onboardingComplete = context.settingsDataStore.data.map { it[ONBOARDING_COMPLETE] ?: false }
    val lockTimeout = context.settingsDataStore.data.map { it[LOCK_TIMEOUT] ?: "5 minutes" }
    val appLockEnabled = context.settingsDataStore.data.map { it[APP_LOCK_ENABLED] ?: false }
    val remindersEnabled = context.settingsDataStore.data.map { it[REMINDERS_ENABLED] ?: false }
    val detailedReminders = context.settingsDataStore.data.map { it[DETAILED_REMINDERS] ?: false }
    val fuzzyThreshold = context.settingsDataStore.data.map { it[FUZZY_THRESHOLD] ?: 70 }
    val activeLibraryId = context.settingsDataStore.data.map { it[ACTIVE_LIBRARY_ID] }
    val signedInUserId = context.settingsDataStore.data.map { it[SIGNED_IN_USER_ID] }

    suspend fun setTheme(value: String) = context.settingsDataStore.edit { it[THEME] = value }
    suspend fun setOnboardingComplete() = context.settingsDataStore.edit { it[ONBOARDING_COMPLETE] = true }
    suspend fun setLockTimeout(value: String) = context.settingsDataStore.edit { it[LOCK_TIMEOUT] = value }
    suspend fun setAppLockEnabled(value: Boolean) = context.settingsDataStore.edit { it[APP_LOCK_ENABLED] = value }
    suspend fun setRemindersEnabled(value: Boolean) = context.settingsDataStore.edit { it[REMINDERS_ENABLED] = value }
    suspend fun setDetailedReminders(value: Boolean) = context.settingsDataStore.edit { it[DETAILED_REMINDERS] = value }
    suspend fun setFuzzyThreshold(value: Int) = context.settingsDataStore.edit { it[FUZZY_THRESHOLD] = value.coerceIn(60, 90) }
    suspend fun setActiveLibraryId(value: Long) = context.settingsDataStore.edit { it[ACTIVE_LIBRARY_ID] = value }
    suspend fun setSignedInUserId(value: Long) = context.settingsDataStore.edit { it[SIGNED_IN_USER_ID] = value }
    suspend fun clearSignedInUser() = context.settingsDataStore.edit { it.remove(SIGNED_IN_USER_ID) }

    private companion object {
        val THEME = stringPreferencesKey("theme")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val LOCK_TIMEOUT = stringPreferencesKey("lock_timeout")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val DETAILED_REMINDERS = booleanPreferencesKey("detailed_reminders")
        val FUZZY_THRESHOLD = intPreferencesKey("fuzzy_threshold")
        val ACTIVE_LIBRARY_ID = longPreferencesKey("active_library_id")
        val SIGNED_IN_USER_ID = longPreferencesKey("signed_in_user_id")
    }
}