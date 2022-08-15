package com.dylanc.datastore.flow

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.Flow

interface DataStoreFlow<T> {
  fun setValue(block: suspend (T) -> T): Flow<Preferences>

  fun getValue(): Flow<T>

  fun setValue(value: T): Flow<Preferences> = setValue { value }

  fun setValue(owner: LifecycleOwner, block: suspend (T) -> T) =
    setValue(block).asLiveData().observe(owner) {}

  fun setValue(owner: LifecycleOwner, value: T) = setValue(owner) { value }

  fun observeValue(owner: LifecycleOwner, block: (T) -> Unit) =
    getValue().asLiveData().observe(owner) { block(it) }
}