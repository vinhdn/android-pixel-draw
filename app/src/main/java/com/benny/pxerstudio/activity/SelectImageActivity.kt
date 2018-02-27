package com.benny.pxerstudio.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.benny.pxerstudio.R
import kotlinx.android.synthetic.main.activity_select_image.*

class SelectImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_image)
        im100.setOnClickListener {

            createIntentPixel(R.drawable.im100)
        }
        im56.setOnClickListener {

            createIntentPixel(R.drawable.im56)
        }
        im24.setOnClickListener {
            createIntentPixel(R.drawable.im24)
        }
    }

    private fun createIntentPixel(drawable: Int) {
        val intent = Intent(this, PixelActivity::class.java)
        intent.putExtra("drawable", drawable)
        startActivity(intent)
    }
}
