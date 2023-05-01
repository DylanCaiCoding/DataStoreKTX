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

package com.dylanc.datastore.rxjava3

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.rxjava3.RxDataStore
import com.dylanc.datastore.DataStoreOwner
import com.dylanc.datastore.IDataStoreOwner
import com.dylanc.datastore.rxjava3.IRxDataStoreOwner.Companion.toRxDataStore
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.rx3.asCoroutineDispatcher
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class RxDataStoreOwner(name: String) : DataStoreOwner(name), IRxDataStoreOwner {
  @Suppress("LeakingThis")
  override val rxDataStore: RxDataStore<Preferences> by dataStore.toRxDataStore()
}

interface IRxDataStoreOwner : IDataStoreOwner {

  val rxDataStore: RxDataStore<Preferences>

  override fun intPreference(default: Int?) =
    RxPreferenceProperty(this, ::intPreferencesKey, default ?: 0)

  override fun doublePreference(default: Double?) =
    RxPreferenceProperty(this, ::doublePreferencesKey, default ?: 0.0)

  override fun longPreference(default: Long?) =
    RxPreferenceProperty(this, ::longPreferencesKey, default ?: 0L)

  override fun floatPreference(default: Float?) =
    RxPreferenceProperty(this, ::floatPreferencesKey, default ?: 0f)

  override fun booleanPreference(default: Boolean?) =
    RxPreferenceProperty(this, ::booleanPreferencesKey, default ?: false)

  override fun stringPreference(default: String?) =
    RxPreferenceProperty(this, ::stringPreferencesKey, default ?: "")

  override fun stringSetPreference(default: Set<String>?) =
    RxPreferenceProperty(this, ::stringSetPreferencesKey, default ?: emptySet())

  class RxPreferenceProperty<V : Any>(
    private val owner: IRxDataStoreOwner,
    private val key: (String) -> Preferences.Key<V>,
    private val default: V
  ) : ReadOnlyProperty<IDataStoreOwner, RxDataStorePreference<V>> {
    private var cache: RxDataStorePreference<V>? = null

    override fun getValue(thisRef: IDataStoreOwner, property: KProperty<*>): RxDataStorePreference<V> =
      cache ?: RxDataStorePreference(thisRef.dataStore , key(property.name), default, owner.rxDataStore ).also { cache = it }
  }

  companion object {
    fun DataStore<Preferences>.toRxDataStore(scheduler: Scheduler = Schedulers.io()) = lazy {
      RxDataStore.create(this, CoroutineScope(scheduler.asCoroutineDispatcher() + Job()))
    }
  }
}