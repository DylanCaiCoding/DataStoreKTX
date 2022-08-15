@file:Suppress("unused")

package com.dylanc.datastore.flow

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface DataStoreOwner {
  val dataStore: DataStore<Preferences>
    get() = default ?: throw IllegalStateException("DataStoreOwner must be initialized before use")

  val context: Context get() = DataStoreInitializer.application

  companion object {
    var default: DataStore<Preferences>? = null
  }
}

fun DataStoreOwner.intPreferences(default: Int = 0) =
  DataStoreProperty(default) { intPreferencesKey(it) }

fun DataStoreOwner.doublePreferences(default: Double = 0.0) =
  DataStoreProperty(default) { doublePreferencesKey(it) }

fun DataStoreOwner.booleanPreferences(default: Boolean = false) =
  DataStoreProperty(default) { booleanPreferencesKey(it) }

fun DataStoreOwner.floatPreferences(default: Float = 0f) =
  DataStoreProperty(default) { floatPreferencesKey(it) }

fun DataStoreOwner.longPreferences(default: Long = 0) =
  DataStoreProperty(default) { longPreferencesKey(it) }

fun DataStoreOwner.stringPreferences() =
  DataStoreNullableProperty { stringPreferencesKey(it) }

fun DataStoreOwner.stringPreferences(default: String) =
  DataStoreProperty(default) { stringPreferencesKey(it) }

fun DataStoreOwner.stringSetPreferences() =
  DataStoreNullableProperty { stringSetPreferencesKey(it) }

fun DataStoreOwner.stringSetPreferences(default: Set<String>) =
  DataStoreProperty(default) { stringSetPreferencesKey(it) }

class DataStoreProperty<V>(
  private val default: V,
  private val key: (String) -> Preferences.Key<V>
) : ReadOnlyProperty<DataStoreOwner, DataStoreFlow<V>> {
  private var cache: V? = null

  override fun getValue(thisRef: DataStoreOwner, property: KProperty<*>) = object : DataStoreFlow<V> {

    override fun setValue(block: suspend (V) -> V): Flow<Preferences> =
      flow {
        emit(thisRef.dataStore.edit { preferences ->
          val value = block(cache ?: preferences[key(property.name)] ?: default)
          preferences[key(property.name)] = value
          cache = value
        })
      }

    override fun getValue(): Flow<V> =
      thisRef.dataStore.data.map { preferences ->
        cache ?: (preferences[key(property.name)] ?: default).also { cache = it }
      }
  }
}

class DataStoreNullableProperty<V>(
  private val key: (String) -> Preferences.Key<V>
) : ReadOnlyProperty<DataStoreOwner, DataStoreFlow<V?>> {
  private var cache: V? = null

  override fun getValue(thisRef: DataStoreOwner, property: KProperty<*>) = object : DataStoreFlow<V?> {

    override fun setValue(block: suspend (V?) -> V?): Flow<Preferences> =
      flow {
        emit(thisRef.dataStore.edit { preferences ->
          val value = block(cache ?: preferences[key(property.name)])
          if (value != null) {
            preferences[key(property.name)] = value
          } else {
            preferences.remove(key(property.name))
          }
          cache = value
        })
      }

    override fun getValue(): Flow<V?> =
      thisRef.dataStore.data.map { preferences ->
        cache ?: preferences[key(property.name)].also { cache = it }
      }
  }
}