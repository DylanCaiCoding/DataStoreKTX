# DataStoreKTX

English | [中文](README_ZH.md)

[![](https://www.jitpack.io/v/DylanCaiCoding/DataStoreKTX.svg)](https://www.jitpack.io/#DylanCaiCoding/DataStoreKTX-KTX)
[![License](https://img.shields.io/badge/License-Apache--2.0-blue.svg)](https://github.com/DylanCaiCoding/DataStoreKTX/blob/master/LICENSE)

[Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore) is a data storage solution that uses Kotlin Coroutines or RxJava to store data in an asynchronous manner with consistent transaction methods. Its usage is different from other storage solutions such as SharedPreferences and MMKV, making it more special. Therefore, there are not many good DataStore encapsulations available online. I have done a lot of exploration and attempts and finally developed a very satisfactory set of usage methods, which I hope can help everyone.

## Features

- No need to create DataStore or RxDataStore objects;
- Supports both Kotlin Coroutines and RxJava usage;
- Uses property names as key names, eliminating the need to declare a large number of key name constants;
- Ensures type safety and avoids exceptions caused by inconsistent types or key values.

## DataStore vs MMKV

Which one to choose between DataStore and MMKV? It is recommended to read [this article](https://juejin.cn/post/7112268981163016229). If you find the article too long, you can directly read the summary. As for the view of DataStore, it needs to be modified a little bit, because DataStore now supports not only Kotlin Coroutines, but also RxJava usage. Therefore, DataStore is also suitable for Java projects.

If you plan to use MMKV, you can use my other library [MMKV-KTX](https://github.com/DylanCaiCoding/MMKV-KTX). If you choose to use DataStore, then this library is your best choice.

## Basic Usage

Add the following to the root `build.gradle` file:

```groovy
allprojects {
    repositories {
        //...
        maven { url 'https://www.jitpack.io' }
    }
}
```

Add the following dependencies to the module's `build.gradle` file:

```groovy
dependencies {
    implementation 'com.github.DylanCaiCoding.DataStoreKTX:datastore-ktx:1.0.0'
    // Optional
    implementation 'com.github.DylanCaiCoding.DataStoreKTX:datastore-rxjava2:1.0.0'
    implementation 'com.github.DylanCaiCoding.DataStoreKTX:datastore-rxjava3:1.0.0'
}
```

Inherit the `DataStoreOwner` class in a class, and then use the `by xxxxPreference()` function to delegate the property to `DataStore`. For example:

```kotlin
object SettingsRepository : DataStoreOwner(name = "settings") {
  val counter by intPreference()
  val language by stringPreference(default = "zh")
}
```

If you already have a superclass that cannot be inherited, implement `IDataStoreOwner by DataStoreOwner(name)` instead, for example:

```kotlin
object SettingsRepository : BaseRepository(), IDataStoreOwner by DataStoreOwner(name = "settings") {
  //...
}
```

**Make sure that the `name` used is not duplicated so that type safety can be 100% guaranteed!!!**

The following delegation functions that use the property name as the retrieval key value are supported:

- intPreference()
- longPreference()
- booleanPreference()
- floatPreference()
- doublePreference()
- stringPreference()
- stringSetPreference()

The `get()` function that calls this property reads the data by executing `dataStore.data.map {...}`, for example:

```kotlin
// Call in coroutine
val language = SettingsRepository.language.get()
// val language = SettingsRepository.language.getOrDefault()
```

The `set()` function that calls this property saves the data by executing `dataStore.edit {...}`, for example:

```kotlin
// Call in coroutine
SettingsRepository.counter.set(100)
SettingsRepository.counter.set { (this ?: 0) + 1 }
```

This property can also be used as `Flow` or `LiveData`. In this way, a notification callback is made each time the data changes, which can be used to update UI or to write streaming code. For example:

```kotlin
SettingsRepository.counter.asLiveData()
  .observe(this) {
    tvCount.text = (it ?: 0).toString()
  }
```

```kotlin
SettingsRepository.counter.asFlow()
  .map { ... }
```

## Adaptation to RxJava

By default, only Coroutine is supported. You can perform some simple adaptations to extend it to support RxJava. First, add the dependency `datastore-rxjava2` or `datastore-rxjava3` to `build.gradle`.

```groovy
dependencies {
    // Optional
    implementation 'com.github.DylanCaiCoding.DataStoreKTX:datastore-rxjava2:1.0.0'
    implementation 'com.github.DylanCaiCoding.DataStoreKTX:datastore-rxjava3:1.0.0'
}
```

Then change the `DataStoreOwner` class to the `RxDataStoreOwner` class, and it will be adapted. It's recommended to add the `@JvmStatic` annotation to the property, which will make Java code calling this property more concise.

```kotlin
object SettingsRepository : RxDataStoreOwner(name = "settings") {
  @JvmStatic
  val counter by intPreference()
}
```

The new `getAsync()` function call of this property reads the data by executing `rxDataStore.updateDataAsync(prefsIn -> ...)`, and the return value is `Single<T>`, for example:

```java
SettingsRepository.getCounter().getAsync()
    .subscribe(counter -> {
      //...
    });
```

The new `setAsync()` function call of this property writes the data by executing `rxDataStore.data().map(prefs -> ...)`, for example:

```java
SettingsRepository.getCounter().setAsync(100);
    SettingsRepository.getCounter().setAsync((counter, prefsIn) -> counter + 1);
```

It can also be used as a `Flowable`. In this way, a notification callback is made each time the data changes, which can be used to update UI or to write streaming code. For example:

```java
SettingsRepository.getCounter().asFlowable()
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(counter -> tvCounter.setText(String.valueOf(counter)));
```

Coroutine and RxJava usage can be used together, as long as it is the same property, and the read/write functions operate on the same data source.

## Update Log

[Releases](https://github.com/DylanCaiCoding/DataStoreKTX/releases)

## Other Libraries by the Author

| Library | Introduction |
| ------------------------------------------------------------ |-------------------------------------|
| [Longan](https://github.com/DylanCaiCoding/Longan) | Possibly the easiest-to-use Kotlin utility library |
| [LoadingStateView](https://github.com/DylanCaiCoding/LoadingStateView) | Decoupling the title bar or view of loading, loading failure, no data, etc., supporting two-line code integration into the base class |
| [ViewBindingKTX](https://github.com/DylanCaiCoding/ViewBindingKTX) | The most comprehensive ViewBinding tool |
| [MMKV-KTX](https://github.com/DylanCaiCoding/MMKV-KTX) | The most flexible and easy-to-use MMKV tool |
| [Tracker](https://github.com/DylanCaiCoding/Tracker) | A lightweight tracking framework based on Buzzvideo's view tree tracking idea |

## License

```
Copyright (C) 2023. Dylan Cai

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
