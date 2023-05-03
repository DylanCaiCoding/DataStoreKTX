# DataStoreKTX

[English](README.md) | 中文

[![](https://www.jitpack.io/v/DylanCaiCoding/MMKV-KTX.svg)](https://www.jitpack.io/#DylanCaiCoding/DataStoreKTX-KTX)
[![License](https://img.shields.io/badge/License-Apache--2.0-blue.svg)](https://github.com/DylanCaiCoding/DataStoreKTX/blob/master/LICENSE)

[Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore) 是一种数据存储解决方案，由于使用了 Kotlin 协程或者 RxJava 以异步、一致的事务方式存储数据，用法相较于其它存储方案 (SharedPreferences、MMKV) 会更加特别，所以目前网上都没有什么比较好的 DataStore 封装。个人也做了很多摸索和尝试，终于封装出了一套非常满意的用法，希望能帮助到大家。

## Features

- 无需创建 DataStore 或 RxDataStore 对象；
- 支持 Kotlin 协程和 RxJava 用法；
- 用属性名作为键名，无需声明大量的键名常量；
- 可以确保类型安全，避免类型或者 key 值不一致导致的异常；

## DataStore VS MMKV

DataStore 和 MMKV 到底该怎么选？建议看下[扔物线的文章](https://juejin.cn/post/7112268981163016229)，如果觉得文章太长可以直接看总结。最后关于 DataStore 的观点在现在来看要做一点点修改，因为现在 DataStore 不是必须使用 Kotlin 协程了，还支持 RxJava 的用法，所以在 Java 项目也是适合用 DataStore 的。

如果你打算用 MMKV，可以使用个人的另一个库 [MMKV-KTX](https://github.com/DylanCaiCoding/MMKV-KTX)。如果选择使用 DataStore，那么这个库就是你的最佳选择。

## 基础用法

在根目录的 `build.gradle` 添加:

```groovy
allprojects {
    repositories {
        //...
        maven { url 'https://www.jitpack.io' }
    }
}
```

在模块的 `build.gradle` 添加依赖：

```groovy
dependencies {
    implementation 'com.github.DylanCaiCoding.DataStoreKTX:datastore-ktx:1.0.0'
}
```

让一个类继承 `DataStoreOwner` 类，即可在该类使用 `by xxxxPreference()` 函数将属性委托给 `DataStore`，比如：

```kotlin
object SettingsRepository : DataStoreOwner(name = "settings") {
  val counter by intPreference()
  val language by stringPreference(default = "zh")
}
```

如果已经有了父类没法继承，那就实现 `IDataStoreOwner by DataStoreOwner(name)`，比如：

```kotlin
object SettingsRepository : BaseRepository(), IDataStoreOwner by DataStoreOwner(name = "settings") {
  // ...
}
```

**要确保使用过的 `name` 不重复，只有这样才能 100% 确保类型安全！！！**

支持使用以下类型的委托函数，会用属性名作为存取的 key 值：

- intPreference()
- longPreference()
- booleanPreference()
- floatPreference()
- doublePreference()
- stringPreference()
- stringSetPreference()

调用该属性的 `get()` 函数会执行 `dataStore.data.map {...}` 的读取数据，比如：

```kotlin
// 需要在协程中调用
val language = SettingsRepository.language.get()
// val language = SettingsRepository.language.getOrDefault()
```

调用该属性的 `set()` 函数会执行 `dataStore.edit {...}` 的保存数据，比如：

```kotlin
// 需要在协程中调用
SettingsRepository.counter.set(100)
SettingsRepository.counter.set { (this ?: 0) + 1 }
```

也可以作为 `Flow` 或 `LiveData` 使用，这样每当数据发生变化都会有通知回调，可以更新 UI 或流式编程。比如：

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

## 适配 RxJava

默认只支持协程用法，可以做一些简单地适配扩展出 RxJava 用法。首先要在 `build.gradle` 添加 `datastore-rxjava2` 或 `datastore-rxjava3` 依赖。

```groovy
dependencies {
    // 可选
    implementation 'com.github.DylanCaiCoding.DataStoreKTX:datastore-rxjava2:1.0.0'
    implementation 'com.github.DylanCaiCoding.DataStoreKTX:datastore-rxjava3:1.0.0'
}
```

然后把 `DataStoreOwner` 类改为 `RxDataStoreOwner` 类，这样就适配好了。建议给属性添加 `@JvmStatic` 注解，可以让调用该属性的 Java 代码会更加简洁。

```kotlin
object SettingsRepository : RxDataStoreOwner(name = "settings") {
  @JvmStatic
  val counter by intPreference()
}
```

调用该属性新增的 `getAsync()` 函数会执行 `rxDataStore.updateDataAsync(prefsIn -> ...)` 的读取数据，返回值是 `Single<T>`，比如：

```java
SettingsRepository.getCounter().getAsync()
    .subscribe(counter -> {
      // ...
    });
```

调用该属性新增的  `setAsync()` 函数会执行 `rxDataStore.data().map(prefs -> ...)` 的读取数据，比如：

```java
SettingsRepository.getCounter().setAsync(100);
    SettingsRepository.getCounter().setAsync((counter, prefsIn) -> counter + 1);
```

也可以将作为 `Flowable` 使用，这样每当数据发生变化都会有通知回调，可以更新 UI 或流式编程。比如：

```java
SettingsRepository.getCounter().asFlowable()
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(counter -> tvCounter.setText(String.valueOf(counter)));
```

协程用法和 RxJava 用法可以混用，只要是同一个属性，存取函数的都是操作同一个数据源。

## 更新日志

[Releases](https://github.com/DylanCaiCoding/DataStoreKTX/releases)

## 作者其它的库

| 库                                                           | 简介                                  |
| ------------------------------------------------------------ |-------------------------------------|
| [Longan](https://github.com/DylanCaiCoding/Longan)           | 可能是最好用的 Kotlin 工具库                  |
| [LoadingStateView](https://github.com/DylanCaiCoding/LoadingStateView) | 深度解耦标题栏或加载中、加载失败、无数据等视图，支持两行代码集成到基类 |
| [ViewBindingKTX](https://github.com/DylanCaiCoding/ViewBindingKTX) | 最全面的 ViewBinding 工具                 |
| [MMKV-KTX](https://github.com/DylanCaiCoding/MMKV-KTX)       | 最灵活易用的 MMKV 工具                      |
| [Tracker](https://github.com/DylanCaiCoding/Tracker)         | 基于西瓜视频的视图树埋点思路实现的轻量级埋点框架            |

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
