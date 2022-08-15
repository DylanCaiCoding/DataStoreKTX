/*
 * Copyright (c) 2022. Dylan Cai
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

package com.dylanc.datastore.flow

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.startup.Initializer

/**
 * @author Dylan Cai
 */
class DataStoreInitializer : Initializer<Unit> {

  override fun create(context: Context) {
    application = context as Application
    if (DataStoreOwner.default == null) {
      DataStoreOwner.default = context.dataStore
    }
  }

  override fun dependencies() = emptyList<Class<Initializer<*>>>()

  private val Context.dataStore by preferencesDataStore("default")

  companion object {
    internal lateinit var application: Application
  }
}
