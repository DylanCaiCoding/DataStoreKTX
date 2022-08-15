package com.dylanc.datastore.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    DataRepository.id.observeValue(this) {
      findViewById<TextView>(R.id.textView).text = it.toString()
    }

    findViewById<View>(R.id.button).setOnClickListener {
      DataRepository.id.setValue(this, 100)
    }
  }
}