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

package com.dylanc.datastore.sample.kotlin

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

  private var disposable: Disposable? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val btn1 = findViewById<TextView>(R.id.button1)
    val btn2 = findViewById<TextView>(R.id.button2)

    btn1.setOnClickListener {
      lifecycleScope.launch {
        TestRepository.counter.set { (this ?: 0) + 1 }
      }
    }
    btn2.setOnClickListener {
      TestRepository2.counter.setAsync { this + 1 }
    }

    TestRepository.counter.asLiveData()
      .observe(this) {
        btn1.text = (it ?: 0).toString()
      }

    disposable = TestRepository2.counter.asFlowable()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        {
          btn2.text = it.toString()
        },
        {
          it.printStackTrace()
        }
      )
  }

  override fun onDestroy() {
    super.onDestroy()
    disposable?.dispose()
  }
}