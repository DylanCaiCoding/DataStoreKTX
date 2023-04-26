@file:Suppress("unused")

package com.dylanc.datastore.rxjava2

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.rxjava2.RxDataStore
import com.dylanc.datastore.DataStorePreference
import com.dylanc.datastore.DataStorePreferenceImpl
import io.reactivex.Flowable
import io.reactivex.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi

operator fun <V> Preferences.get(preference: RxDataStorePreference<V>) = this[preference.key]

interface RxDataStorePreference<V> : DataStorePreference<V> {
  fun asFlowable(): Flowable<V>

  fun getAsync(): Single<V> = asFlowable().first(default!!)

  fun setAsync(block: V.(preferences: Preferences) -> V?): Single<Preferences>

  fun setAsync(value: V?): Single<Preferences> = setAsync { value }
}

@OptIn(ExperimentalCoroutinesApi::class)
class RxDataStorePreferenceImpl<V>(
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