package ir.cutte.nava.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.navaDataStore: DataStore<Preferences> by preferencesDataStore(name = "nava_preferences")
