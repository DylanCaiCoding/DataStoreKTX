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

package com.dylanc.datastore.rxjava3

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.rxjava3.RxDataStore
import com.dylanc.datastore.DataStorePreference
import com.dylanc.datastore.DataStorePreferenceImpl
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi

operator fun <V : Any> Preferences.get(preference: RxDataStorePreference<V>) = this[preference.key]

interface RxDataStorePreference<V : Any> : DataStorePreference<V> {
  fun asFlowable(): Flowable<V>

  fun getAsync(): Single<V> = asFlowable().first(default!!)

  fun setAsync(block: V.(preferences: Preferences) -> V?): Single<Preferences>

  fun setAsync(value: V?): Single<Preferences> = setAsync { value }
}

@OptIn(ExperimentalCoroutinesApi::class)
class RxDataStorePreferenceImpl<V : Any>(
  dataStore: DataStore<Preferences>,
  key: Preferences.Key<V>,
  override val default: V,
  private val rxDataStore: RxDataStore<Preferences>
) : DataStorePreferenceImpl<V>(dataStore, key, default), RxDataStorePreference<V> {

  override fun asFlowable(): Flowable<V> =
    rxDataStore.data().map { it[key] ?: default }

  override fun setAsync(block: V.(Preferences) -> V?) =
    rxDataStore.updateDataAsync {
      val preferences = it.toMutablePreferences()
      val value = block(preferences[key] ?: default, preferences)
      if (value == null) {
        preferences.remove(key)
      } else {
        preferences[key] = value
      }
      Single.just(preferences)
    }
}