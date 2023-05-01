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

package com.dylanc.datastore.sample.java;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Dylan Cai
 */
public class MainActivity extends AppCompatActivity {

  @Nullable
  private Disposable disposable;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Button btn1 = findViewById(R.id.button1);
    Button btn2 = findViewById(R.id.button2);

    btn1.setOnClickListener(v ->
        TestRepository.getCounter().setAsync((counter, prefsIn) -> counter + 1));

    btn2.setOnClickListener(v ->
        TestRepository2.getCounter().setAsync((counter, prefsIn) -> counter + 1));

    TestRepository.getCounter().asLiveData().observe(this, counter ->
        btn1.setText(String.valueOf(counter)));

    disposable = TestRepository2.getCounter().asFlowable()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(counter -> btn2.setText(String.valueOf(counter)));
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (disposable != null) {
      disposable.dispose();
    }
  }
}
