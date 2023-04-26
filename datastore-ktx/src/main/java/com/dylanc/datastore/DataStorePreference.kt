/*
 * Copyright (c) 2023. Dylan Cai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("unused")

package com.dylanc.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

operator fun <V> Preferences.get(preference: DataStorePreference<V>) = this[preference.key]

interface DataStorePreference<V> {
  val key: Preferences.Key<V>

  val default: V?

  fun asFlow(): Flow<V?>

  fun asLiveData(): LiveData<V?> = asFlow().asLiveData()

  suspend fun get(): V? = asFlow().first()

  suspend fun getOrDefault(): V = get() ?: throw IllegalStateException("No default value")

  suspend fun set(block: suspend V?.(Preferences) -> V?): Preferences

  suspend fun set(value: V?): Preferences = set { value }
}

open class DataStorePreferenceImpl<V>(
  private val dataStore: DataStore<Preferences>,
  override val key: Preferences.Key<V>,
  override val default: V?
) : DataStorePreference<V> {

  override suspend fun set(block: suspend V?.(Preferences) -> V?): Preferences =
    dataStore.edit { preferences ->
      val value = block(preferences[key] ?: default, preferences)
      if (value == null) {
        preferences.remove(key)
      } else {
        preferences[key] = value
      }
    }

  override fun asFlow(): Flow<V?> =
    dataStore.data.map { it[key] ?: default }
}