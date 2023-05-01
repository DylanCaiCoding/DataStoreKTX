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

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class DataStoreOwner(name: String) : IDataStoreOwner {
  private val Context.dataStore by preferencesDataStore(name)
  override val dataStore get() = context.dataStore
}

interface IDataStoreOwner {
  val context: Context get() = application

  val dataStore: DataStore<Preferences>

  fun intPreference(default: Int? = null): ReadOnlyProperty<IDataStoreOwner, DataStorePreference<Int>> =
    PreferenceProperty(::intPreferencesKey, default)

  fun doublePreference(default: Double? = null): ReadOnlyProperty<IDataStoreOwner, DataStorePreference<Double>> =
    PreferenceProperty(::doublePreferencesKey, default)

  fun longPreference(default: Long? = null): ReadOnlyProperty<IDataStoreOwner, DataStorePreference<Long>> =
    PreferenceProperty(::longPreferencesKey, default)

  fun floatPreference(default: Float? = null): ReadOnlyProperty<IDataStoreOwner, DataStorePreference<Float>> =
    PreferenceProperty(::floatPreferencesKey, default)

  fun booleanPreference(default: Boolean? = null): ReadOnlyProperty<IDataStoreOwner, DataStorePreference<Boolean>> =
    PreferenceProperty(::booleanPreferencesKey, default)

  fun stringPreference(default: String? = null): ReadOnlyProperty<IDataStoreOwner, DataStorePreference<String>> =
    PreferenceProperty(::stringPreferencesKey, default)

  fun stringSetPreference(default: Set<String>? = null): ReadOnlyProperty<IDataStoreOwner, DataStorePreference<Set<String>>> =
    PreferenceProperty(::stringSetPreferencesKey, default)

  class PreferenceProperty<V>(
    private val key: (String) -> Preferences.Key<V>,
    private val default: V? = null,
  ) : ReadOnlyProperty<IDataStoreOwner, DataStorePreference<V>> {
    private var cache: DataStorePreference<V>? = null

    override fun getValue(thisRef: IDataStoreOwner, property: KProperty<*>): DataStorePreference<V> =
      cache ?: DataStorePreference(thisRef.dataStore, key(property.name), default).also { cache = it }
  }

  companion object {
    internal lateinit var application: Application
  }
}
